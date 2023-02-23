'''
pip install yolov5 instead of ultralytics 

'''
import os
import cv2
import imagezmq
import imutils
import numpy as np
import torch
import time
import socket
import yolov5
import re
import traceback

from PIL import Image


MIN_CONFIDENCE_THRESHOLD = 0.75         # Change this to ensure no double results
NON_RED_CONFIDENCE_THRESHOLD = 0.55

IMGREC_PORT = 12348
RPI_IP = "192.168.15.1"
ZMQ_IP = "192.168.15.69"
ZMQ_PORT = 5555

MODEL_PATH = os.path.join(".", "bestv5.pt")
YOLO_PATH = os.path.join("..","yolov5")

class ImgRecServer:
    def __init__(self, rpi_ip=RPI_IP, imgrec_port=IMGREC_PORT, 
                 zmq_ip=ZMQ_IP, zmq_port=ZMQ_PORT, 
                 model_path=MODEL_PATH, yolo_path=YOLO_PATH,
                 ):
        
        self.rpi_ip = rpi_ip
        self.imgrec_port = imgrec_port
        self.zmq_ip = zmq_ip
        self.zmq_port = zmq_port
        self.zmq_address = f"tcp://{self.zmq_ip}:{self.zmq_port}"
        
        self.model = self.load_model(model_path, yolo_path)
        self.image_hub = self.get_image_hub()
        self.c_sock = self.connect_rpi()
        
        print("[IMGREC_S/INFO] Finish basic initialisation")

    def load_model(self, model_path, yolo_path):
        '''
        Load trained YOLOv5 model
        '''
        model = yolov5.load(model_path)                      # online load
        # self.model = torch.hub.load(yolo_path, 'custom', path=model_path,
        #                             source='local')         # offline load
        model.conf = MIN_CONFIDENCE_THRESHOLD
        model.iou = NON_RED_CONFIDENCE_THRESHOLD
        print("[IMGREC_S/INFO] Loaded model")
        return model

    def get_image_hub(self):
            # imagezmq.ImageHub(open_port=self.zmq_address, REQ_REP=False)       # PUB/SUB
        return imagezmq.ImageHub(open_port=self.zmq_address)
        
    def connect_rpi(self):
        '''
        Create a socket and connect to RPI
        '''
        c_sock= socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        c_sock.connect((self.rpi_ip, self.imgrec_port))
        print(f"[IMGREC_S/INFO] Connected to {self.rpi_ip}:{self.imgrec_port}")
        return c_sock
        
    def __call__(self):
        print("[IMGREC_S/INFO] Ready")
    
        while True:
            try:
                (rpi_name, frame) = self.image_hub.recv_image()
                self.image_hub.send_reply(b'OK')      # Required in REQ/REP
                # print("[IMGREC_S/INFO] Received frame")
                cv2.imshow(rpi_name, frame)
                cv2.waitKey(1)
                
                results = self.model(frame)
                # results.show()            # show image with box
                pd = results.pandas().xyxy[0]
                id = self.process_data(pd)
                self.c_sock.sendall(str(id).encode())
                
            except KeyboardInterrupt:
                print("[IMGREC_S/INFO] KeyboardInterrupt received")
                self.c_sock.sendall(b"disconnect")
                cv2.destroyAllWindows()
                break
            except:
                self.c_sock.sendall(b"disconnect")
                cv2.destroyAllWindows()
                traceback.print_exc()   
                break  
            
    def process_data(self, pd)  -> int:
        if len(pd)==0: return 99
        else:
            highest_confidence = pd["confidence"].max()
            for index, row in pd.iterrows():    
                if row['confidence'] == highest_confidence:
                    return row['name']

if __name__=="__main__":
    im = ImgRecServer()
    im()