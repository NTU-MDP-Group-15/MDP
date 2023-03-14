"""
Filename: imgrec_server.py
Version: v1.2

pip install yolov5 instead of ultralytics 

! Updates (DDMMYY)
010323 - Moved getting most occuring id from RPI to server
"""
import os
import cv2
import zmq
import time
import torch
import imagezmq
import numpy as np
import socket
import traceback

# import imutils
# import yolov5
# from PIL import Image


MIN_CONFIDENCE_THRESHOLD = 0.65         # Change this to ensure no double results
NON_RED_CONFIDENCE_THRESHOLD = 0.55
NMS_IOU = 0.55

RPI_IP = "192.168.15.1"
IMGREC_PORT = 12348

IMG_ZMQ_IP = "192.168.15.69"
IMG_ZMQ_PORT = 5555
ID_ZMQ_IP = "192.168.15.1"
ID_ZMQ_PORT = 5556


#MODEL_PATH = os.path.join(".", "YOLOv5", "yolov5s.pt")     # ./bestv5.pt .\bestv5.pt
MODEL_PATH = os.path.join(".", "bestv5.pt")     # ./bestv5.pt .\bestv5.pt
YOLO_PATH = os.path.join(".","YOLOv5")

NO_OF_PIC = 6

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(CUR_DIR, 'static', "images")

class ImgRecServer:
    def __init__(self, rpi_ip=RPI_IP, imgrec_port=IMGREC_PORT, 
                 img_zmq_ip=IMG_ZMQ_IP, img_zmq_port=IMG_ZMQ_PORT, 
                 id_zmq_ip=ID_ZMQ_IP, id_zmq_port=ID_ZMQ_PORT,
                 model_path=MODEL_PATH, yolo_path=YOLO_PATH,
                 ):
        
        self.id_array = list()        
        self.frame_counter = 0
        
        # Ports & IP addresses
        self.rpi_ip = rpi_ip
        self.imgrec_port = imgrec_port
        self.img_zmq_ip = img_zmq_ip
        self.img_zmq_port = img_zmq_port
        
        self.id_zmq_ip=id_zmq_ip
        self.id_zmq_port=id_zmq_port
        
        self.img_zmq_address = f"tcp://{self.img_zmq_ip}:{self.img_zmq_port}"
        self.id_zmq_address = f"tcp://{self.id_zmq_ip}:{self.id_zmq_port}"
        print(f"[IMGREC/INFO] img_zmq_address: {self.img_zmq_address}")
        print(f"[IMGREC/INFO] id_zmq_address: {self.id_zmq_address}")
        
        # self.img_idx = self.get_file_count()
        self.img_name = "image{img_idx}.{img_format}"
        self.img_format = "jpg"
        
        self.model = self.load_model(model_path, yolo_path)
        input("> ")
        
        self.image_hub = self.get_image_hub()
        self.id_pub = self.get_id_pub()
        # self.c_sock = self.connect_rpi()
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
        return imagezmq.ImageHub(open_port=self.img_zmq_address)
    
    def get_id_pub(self):
        context = zmq.Context()
        socket = context.socket(zmq.PUB)
        socket.connect(self.id_zmq_address)
        return socket
    
    def get_file_count(self) -> int:
        _, _, files = next(os.walk(IMG_DIR))
        return len(files)    
    
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
                
                results.save(save_dir=IMG_DIR)
                # print("SAVING IMAGE")
                # img_name = self.img_name.format(img_idx=self.img_idx, img_format=self.img_format)
                # cv2.imwrite(os.path.join(IMG_DIR, img_name), results)
                
                # self.img_idx += 1
                #results.show()            # show image with box
                
                pd = results.pandas().xyxy[0]
                
                # Get highest confidence ID based off all detection
                highest_conf_id = self.get_highest_confidence_id(pd)
                
                # Add id to array
                self.id_array.append(highest_conf_id)
                
                if len(self.id_array) == NO_OF_PIC:
                    filtered_list = list(filter((99).__ne__, self.id_array))
                    print(filtered_list)
                    if len(filtered_list) == 0: 
                        most_occurring_id = 99
                    else:
                        most_occurring_id = int(max(set(self.id_array), key=self.id_array.count))                    
                    # self.c_sock.sendall(str(most_occurring_id).encode())
                    self.id_pub.send_string(most_occurring_id)
                    self.id_array = list()
                    
            except KeyboardInterrupt:
                print("[IMGREC_S/INFO] KeyboardInterrupt received")
                break
            
            except:
                traceback.print_exc()   
                break  
        cv2.destroyAllWindows()
        
    def get_highest_confidence_id(self, pd) -> int:
        if len(pd) == 0: return 99
        else:
            highest_confidence = pd['confidence'].max()
            for idx, row in pd.iterrows():    
                if row['confidence'] == highest_confidence:
                    return int(row['name'])
                
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