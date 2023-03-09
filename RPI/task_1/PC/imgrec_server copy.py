"""
Filename: imgrec_server.py
Version: v1.2

pip install yolov5 instead of ultralytics 

! Updates (DDMMYY)
010323 - Moved getting most occuring id from RPI to server
"""
import os
import cv2
import time
import torch
import imagezmq
import numpy as np
import socket
import traceback

# import imutils
# import yolov5
# from PIL import Image


MIN_CONFIDENCE_THRESHOLD = 0.75         # Change this to ensure no double results
NON_RED_CONFIDENCE_THRESHOLD = 0.55
NMS_IOU = 0.55


IMGREC_PORT = 12349
RPI_IP = "192.168.15.1"
ZMQ_IP = "192.168.15.59"
ZMQ_PORT = 5555

#MODEL_PATH = os.path.join(".", "YOLOv5", "yolov5s.pt")     # ./bestv5.pt .\bestv5.pt
MODEL_PATH = os.path.join(".", "bestv5.pt")     # ./bestv5.pt .\bestv5.pt
YOLO_PATH = os.path.join(".","YOLOv5")
#YOLO_PATH = os.path.join(".","yolov5_1")
NO_OF_PIC = 1

assets_dir = os.path.join('.','assets')

class ImgRecServer:
    def __init__(self, rpi_ip=RPI_IP, imgrec_port=IMGREC_PORT, 
                 zmq_ip=ZMQ_IP, zmq_port=ZMQ_PORT, 
                 model_path=MODEL_PATH, yolo_path=YOLO_PATH,
                 ):
        
        self.id_array = list()        
        self.frame_counter = 0
        self.rpi_ip = rpi_ip
        self.imgrec_port = imgrec_port
        self.zmq_ip = zmq_ip
        self.zmq_port = zmq_port
        self.zmq_address = f"tcp://{self.zmq_ip}:{self.zmq_port}"
        
        self.model = self.load_model(model_path, yolo_path)
        input("> ")
        
        self.image_hub = self.get_image_hub()
        self.c_sock = self.connect_rpi()
        
        print("[IMGREC_S/INFO] Finish basic initialisation")

    def load_model(self, model_path, yolo_path):
        '''
        Load trained YOLOv5 model
        '''
        #model = yolov5.load(model_path)                      # online load
        model = torch.hub.load(yolo_path, 'custom', path=model_path,
                               source='local')         # offline load
        model.conf = MIN_CONFIDENCE_THRESHOLD
        model.iou = NMS_IOU
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
        print("[IMGREC_S/INFO] Ready and listening for frames")
        while True:
            try:
                (rpi_name, frame) = self.image_hub.recv_image()
                self.image_hub.send_reply(b'OK')      # Required in REQ/REP
                print("[IMGREC_S/INFO] Received frame")

                #cv2.imshow(rpi_name, frame)
                #cv2.waitKey(1)
                results = self.model(frame)
                results.save(save_dir=assets_dir)
                #results.show()            # show image with box
                pd = results.pandas().xyxy[0]
                
                # Get highest confidence ID based off all detection
                highest_conf_id = self.get_highest_confidence_id(pd)
                
                # Add id to array
                self.id_array.append(highest_conf_id)
                if len(self.id_array) == NO_OF_PIC:
                    most_occurring_id = int(max(set(self.id_array), key=self.id_array.count))                    
                    self.c_sock.sendall(str(most_occurring_id).encode())
                    self.id_array = list()
                    
            except KeyboardInterrupt:
                print("[IMGREC_S/INFO] KeyboardInterrupt received")
                self.c_sock.sendall(b"disconnect")
                break
            
            except:
                self.c_sock.sendall(b"disconnect")
                traceback.print_exc()   
                break  
        cv2.destroyAllWindows()
        
    def get_highest_confidence_id(self, pd) -> int:
        if len(pd) == 0: return 99
        else:
            highest_confidence = pd['confidence'].max()
            for idx, row in pd.iterrows():    
                if row['confidence'] == highest_confidence:
                    return row['name']
                
    def save_processed_photo(self, frame):
        pass
    
    '''
    # old
    def __call__(self):
        print("[IMGREC_S/INFO] Ready and listening for frames")
        while True:
            try:
                (rpi_name, frame) = self.image_hub.recv_image()
                self.image_hub.send_reply(b'OK')      # Required in REQ/REP
                print("[IMGREC_S/INFO] Received frame")

                # cv2.imshow(rpi_name, frame)
                # cv2.waitKey(1)
                
                results = self.model(frame)
                #results.show()            # show image with box
                pd = results.pandas().xyxy[0]
                id = self.process_data(pd)
                self.c_sock.sendall(str(id).encode())
                
            except KeyboardInterrupt:
                print("[IMGREC_S/INFO] KeyboardInterrupt received")
                self.c_sock.sendall(b"disconnect")
                break
            except:
                self.c_sock.sendall(b"disconnect")
                traceback.print_exc()   
                break  
        cv2.destroyAllWindows()
    
    def process_data(self, pd)  -> int:
        if len(pd)==0: 
            self.id_array.append(99)
        else:
            highest_confidence = pd["confidence"].max()
            for idx, row in pd.iterrows():    
                if row['confidence'] == highest_confidence:
                    self.id_array.append(row['name'])
    '''
    
if __name__=="__main__":
    im = ImgRecServer()
    im()