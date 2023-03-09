'''
Filename: imgrecInterface.py
Version: v1.2

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
'''
import cv2
import time
import socket
import imagezmq
import traceback
import threading
from .helper import IMGREC_IN, IMGREC_PORT

HEIGHT = 480
WIDTH = 640
RPI_IP = "192.168.15.1"
ZMQ_IP = "192.168.15.69"
ZMQ_PORT = 5555

NO_OF_PIC = 5
IMG_FORMAT = "jpg"

class ImageRecInterface:
    def __init__(self, rpi_ip=RPI_IP, imgrec_port=IMGREC_PORT, 
                 zmq_ip=ZMQ_IP, zmq_port=ZMQ_PORT,  
                 no_of_pic=NO_OF_PIC, img_format=IMG_FORMAT, 
                 capture_index=0
                 ):
        
        self.rpi_ip = rpi_ip
        self.imgrec_port = imgrec_port
        self.zmq_ip=zmq_ip
        self.zmq_port=zmq_port
        self.zmq_address = f"tcp://{zmq_ip}:{zmq_port}"
        self.no_of_pic=no_of_pic
        self.img_format = img_format
        self.capture_index = capture_index
        
        # Flags to control behaviours
        self.lock = threading.Lock()
        self.kill_flag = False
        self.send_image_flag = False

        # self.idx = self.get_file_count()
        self.rpi_name = socket.gethostname()
        
    def __call__(self):
        self.connect()
        self.picam = self.get_video_capture()
        self.img_sender = self.get_image_sender()
        self.listen_thread = threading.Thread(target=self.listener).start()
        self.send_thread = threading.Thread(target=self.send_video).start()

    def get_video_capture(self) -> cv2.VideoCapture:
        cap = cv2.VideoCapture(self.capture_index)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
        print("[IMGREC/INFO] Waiting 2 seconds to warm up camera")
        time.sleep(2)
        assert cap.isOpened()
        print("[IMGREC/INFO] Camera completed")
        return cap
    
    def get_image_sender(self) -> imagezmq.ImageSender:
        # imagezmq.ImageSender(connect_to=self.zmq_address, REQ_REP=False)
        return imagezmq.ImageSender(connect_to=self.zmq_address)
    
    # def get_file_count(self) -> int:
    #     _, _, files = next(os.walk(IMG_DIR))
    #     return len(files)
    
    def connect(self) -> None:
        print("[IMGREC/INFO] Setting server socket")
        self.s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s_sock.bind((self.rpi_ip, self.imgrec_port))
        self.s_sock.listen(1)
        print("[IMGREC/INFO] Waiting for connection")
        while True:
            try:
                self.c_sock, self.c_addr = self.s_sock.accept()
            except socket.timeout:
                pass
            except KeyboardInterrupt:
                print("[IMGREC/INFO] Received KeyboardInterrupt")
                self.kill_flag = True
                break
            else:
                print(f"[IMGREC/INFO] Connection from {self.c_addr}")
                break
        self.s_sock.close()
    
    def disconnect(self) -> None:
        print("[IMGREC/INFO] Setting kill_flag to True")
        self.kill_flag = True
        
        #self.listen_thread.join()
        #self.send_thread.join()
        
        if self.c_sock:
            self.c_sock.close()
        
        if self.picam.isOpened():
            self.picam.release()

    def take_picture(self, name="img{idx}.{img_format}"):
        # img_name = name.format(idx=self.idx, img_format=self.img_format)
        for _ in range(self.no_of_pic):
            ret, frame = self.picam.read()
            if ret == True:
                self.img_sender.send_image(self.rpi_name, frame)    
                print(f"[IMGREC/INFO] Sending frames {_}")

    def send_video(self) -> "workerThread":
        print("[IMGREC_VID/INFO] Starting video thread")
        while not self.kill_flag:
            _, _ = self.picam.read()
            if self.send_image_flag:
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
                self.send_image_flag = False
        print("[IMGREC_VID/INFO] Exiting video thread... Releasing cam")
        self.picam.release()
        
    def listener(self) -> "workerThread":
        disconnect_flag = False
        
        print("[IMGREC_LISTENER/INFO] Starting listener thread")
        while not self.kill_flag:
            try:
                rcv_data = self.c_sock.recv(1024).decode()
                if rcv_data:
                    print(f"[IMGREC_LISTENER/INFO] IMGREC received {rcv_data}")
                    IMGREC_IN.put(rcv_data)
            except:
                print("[IMGREC_LISTENER/INFO] EXCEPTION")
                traceback.print_exc()
                disconnect_flag = True
        print("[IMGREC_LISTENER/INFO] Exiting listener thread")