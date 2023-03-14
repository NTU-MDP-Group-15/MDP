'''
Filename: imgrecInterface.py
Version: v2.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)
180223 - Added imagezmq server to send images captured
200223 - Added traceback for except
210223 - Replaced self.disconnected_flag to self.kill_flag
         disconnect_flag is now local variable
220223 - Updated imagezmq server logic
230223 - Updated taking photo logic
         Removed ImageRecInterface as thread
         send_video now sends exactly 5 frames
060323 - Added disconnect function to close all relevant socket/interface
130323 - changed socket to zmq pub/sub, zmq.sub for RPI, zmq.pub for imgrec PC
150323 - Refactored for task 2!
         added livestreaming (currently disabled)
         uses zmq instead of sockets now
         

'''
import cv2
import zmq
import time
import socket
import imagezmq
import traceback
import threading

from .config import RPI_IP, IMG_ZMQ_IP, ID_ZMQ_IP, \
                    IMG_ZMQ_PORT, ID_ZMQ_PORT, LIVE_ZMQ_PORT, \
                    NO_OF_PIC

HEIGHT = 480
WIDTH = 640

IMG_FORMAT = "jpg"

class ImageRecInterface:
    def __init__(self, rpi_ip=RPI_IP, img_zmq_ip=IMG_ZMQ_IP, id_zmq_ip=ID_ZMQ_IP,
                 img_zmq_port=IMG_ZMQ_PORT, id_zmq_port=ID_ZMQ_PORT, live_zmq_port=LIVE_ZMQ_PORT,
                 no_of_pic=NO_OF_PIC, img_format=IMG_FORMAT, 
                 capture_index=0
                 ):
                
        # MISC
        self.no_of_pic=no_of_pic
        self.img_format = img_format
        self.capture_index = capture_index

        # Ports & IP addresses
        self.rpi_ip = rpi_ip
        self.img_zmq_ip=img_zmq_ip
        self.img_zmq_port=img_zmq_port
        self.id_zmq_ip=id_zmq_ip
        self.id_zmq_port=id_zmq_port
        self.live_zmq_port=live_zmq_port
        
        # Sockets        
        self.s_sock = None
        self.c_sock = None
        self.img_sender = None
        self.id_sub = None
        self.live_sender = None
        
        # Addresses, socket names
        self.rpi_name = socket.gethostname()
        self.live_zmq_address = f"tcp://{self.rpi_ip}:{self.live_zmq_port}"
        self.img_zmq_address = f"tcp://{self.img_zmq_ip}:{self.img_zmq_port}"
        self.id_zmq_address = f"tcp://{self.id_zmq_ip}:{self.id_zmq_port}"
        print(f"[IMGREC/INFO] img_zmq_address: {self.img_zmq_address}")
        print(f"[IMGREC/INFO] id_zmq_address: {self.id_zmq_address}")
        print(f"[IMGREC/INFO] live_zmq_address: {self.live_zmq_address}")
        
        # Start PICAM
        self.picam = self.get_video_capture()

        # Relevant flags to control behaviours
        self.lock = threading.Lock()
        self.kill_flag = False
        self.send_image_flag = False
    
    
    def connect(self) -> bool:
        self.img_sender = self.get_image_sender() 
        self.id_sub = self.get_id_sub()
        self.live_sender = self.get_live_stream_sender()
        return True
        
    def disconnect(self) -> None:
        print("[IMGREC/INFO] Setting kill_flag = True")
        self.kill_flag = True
        if self.s_sock: 
            self.s_sock.close()
        if self.c_sock:
            self.c_sock.close()
        if self.picam.isOpened():
            self.picam.release()
            
    def start_thread(self):
        sv_thread = threading.Thread(target=self.send_video)
        sv_thread.start()
    
    def get_video_capture(self) -> cv2.VideoCapture:
        cap = cv2.VideoCapture(self.capture_index)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
        print("[IMGREC/INFO] Waiting 2 seconds to warm up camera")
        time.sleep(2)
        assert cap.isOpened()
        print("[IMGREC/INFO] Camera READY")
        return cap
    
    def get_image_sender(self) -> imagezmq.ImageSender:
        # imagezmq.ImageSender(connect_to=self.zmq_address, REQ_REP=False)
        return imagezmq.ImageSender(connect_to=self.img_zmq_address)
    
    def get_live_stream_sender(self) -> imagezmq.ImageSender:
        return imagezmq.ImageSender(connect_to=self.live_zmq_address, REQ_REP=False)
        
    def get_id_sub(self) -> zmq.Context:
        context = zmq.Context()
        socket = context.socket(zmq.SUB)
        # accept all topics (prefixed) - default is none
        socket.setsockopt_string(zmq.SUBSCRIBE, "")
        socket.bind(self.id_zmq_address)
        return socket
    
    
    
    def receive(self) -> str:
        while True:
            rcv_data = self.id_sub.recv_string()
            print(f"[IMGREC/INFO] IMGREC received {rcv_data}")
            break
        return rcv_data 
        
    def send_video(self) -> "workerThread":
        print("[IMGREC_VID/INFO] Starting video thread")
        while not self.kill_flag:
            _, l_frame = self.picam.read()
            #self.live_sender.send_image(self.rpi_name, l_frame)
            if self.send_image_flag:
                print(self.no_of_pic)
                for _ in range(self.no_of_pic):
                    ret, frame = self.picam.read()
                    if ret == True:
                        print("[IMGREC_VID/INFO] Sending frame")
                        self.lock.acquire()
                        self.img_sender.send_image(self.rpi_name, frame)
                        self.lock.release()
                    # Break the loop
                    else: 
                        print("[IMGREC_VID/INFO] ret = False")
                        break
                print("[IMGREC_VID/INFO] Setting self.send_image_flag = False")
                self.send_image_flag = False
        print("[IMGREC_VID/INFO] Exiting video thread... Releasing cam")
        self.picam.release()