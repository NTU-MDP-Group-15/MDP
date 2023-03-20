"""
Filename: imgrec_server.py
Version: v1.3

Simple image recognition server, it listens for image taken from RPI and process
the image. Sends back the ID using ZMQ

Online YOLOv5 access:
pip install yolov5 instead of ultralytics

Offline YOLOv5 access:
use torch to load model instead

! Updates (DDMMYY)
010323 - Moved getting most occuring id from RPI to server
150323 - Removed bulleyes recognition + logic for most_occuring_id was wrong...
"""
import os
import cv2
import zmq
import torch
import imagezmq

# import imutils
# import yolov5
from PIL import Image

MIN_CONFIDENCE_THRESHOLD = 0.75         
NON_RED_CONFIDENCE_THRESHOLD = 0.55
NMS_IOU = 0.55

RPI_IP = "192.168.15.1"
IMGREC_PORT = 12348

IMG_ZMQ_IP = "192.168.15.69"
IMG_ZMQ_PORT = 5555
ID_ZMQ_IP = "192.168.15.1"
ID_ZMQ_PORT = 5556


#MODEL_PATH = os.path.join(".", "YOLOv5", "yolov5s.pt")
MODEL_PATH = os.path.join(".", "models","T1_best.pt")   
YOLO_PATH = os.path.join(".","YOLOv5")

NO_OF_PIC = 1

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
        # model.classes = [1,2]
        print("[IMGREC_S/INFO] Loaded model")
        return model

    def get_image_hub(self) -> imagezmq.ImageHub:
        '''
        Opens REQ/REP imagezmq socket to receive Images
        '''
        # return imagezmq.ImageHub(open_port=self.img_zmq_address, REQ_REP=False)       # PUB/SUB
        return imagezmq.ImageHub(open_port=self.img_zmq_address)
    
    def get_id_pub(self) -> zmq.Context:
        '''
        Opens PUB/SUB zmq socket to send ID 
        '''
        context = zmq.Context()
        socket = context.socket(zmq.PUB)
        socket.connect(self.id_zmq_address)
        return socket
    
    def get_file_count(self) -> int:
        _, _, files = next(os.walk(IMG_DIR))
        return len(files)    
        
    def __call__(self):
        print("[IMGREC_S/INFO] Ready and listening for frames")
        try:
            while True:
                try:
                    rpi_name, frame = self.image_hub.recv_image()
                    self.image_hub.send_reply(b'OK')      # Required in REQ/REP
                    cv2.waitKey(1)
                    print("[IMGREC_S/INFO] Received frame")
                except KeyboardInterrupt:
                    self.id_array = list()
                    continue
                
                #cv2.imshow(rpi_name, frame)
                #cv2.waitKey(1)
                frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                results = self.model(frame)
                
                results.save(save_dir=IMG_DIR)
                # results.show()            # show image with box
                
                pd = results.pandas().xyxy[0]
                
                # Get highest confidence ID based off all detection
                highest_conf_id = self.get_highest_confidence_id(pd)
                
                # Add id to array
                self.id_array.append(highest_conf_id)
                
                if len(self.id_array) == NO_OF_PIC:
                    # Filter out 99 IDs
                    filtered_list = list(filter((99).__ne__, self.id_array))
                    if len(filtered_list) == 0: 
                        most_occurring_id = 99
                    else:
                        most_occurring_id = int(max(set(filtered_list), key=filtered_list.count))                    

                    self.id_pub.send_string(str(most_occurring_id))
                    self.id_array = list()
                    
        except KeyboardInterrupt:
            print("[IMGREC_S/INFO] KeyboardInterrupt received")
        cv2.destroyAllWindows()
        
    def get_highest_confidence_id(self, pd) -> int:
        '''
        Function filters out "0" detection and returns the highest confidence id
        else return 99 when there is no detection
        '''
        # remove rows with bulleyes id
        i = pd[(pd["name"] == "0")].index
        pd = pd.drop(i)
        
        if len(pd) == 0: return 99
        else:
            highest_confidence = pd['confidence'].max()
            
            for idx, row in pd.iterrows():    
                if row['confidence'] == highest_confidence:
                    return int(row['name'])

if __name__=="__main__":
    im = ImgRecServer()
    im()