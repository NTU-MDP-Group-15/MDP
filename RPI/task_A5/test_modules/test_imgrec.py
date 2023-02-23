'''
Filename: imgrecInterface.py
Version: v0.2

Class for setting up connection sockets for algo
! Updates (DDMMYY)
180223 - Added imagezmq server to send images captured
200223 - Added traceback for except
210223 - Replaced self.disconnected_flag to self.kill_flag
         disconnect_flag is now local variable
220223 - Updated imagezmq server logic
230223 - Updated taking photo logic
'''
import os
import cv2
import time
import socket
import imagezmq
import traceback
import threading
from helper import IMGREC_IN, IMGREC_OUT, IMGREC_PORT

HEIGHT = 400
WIDTH = 600
RPI_IP = "192.168.15.1"
ZMQ_IP = "192.168.15.69"
ZMQ_PORT = 5555

NO_OF_PIC = 5
IMG_FORMAT = "jpg"

#class ImageRecInterface:
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
        
        self.kill_flag = False
        self.stop_vid_flag = False
        self.send_image_flag = False
        # self.idx = self.get_file_count()

        self.rpi_name = socket.gethostname()

    # def run(self):
    #     self.picam = self.get_video_capture()
    #     self.img_sender = self.get_image_sender()
    #     self.connect()
    #     threading.Thread(target=self.listener).start()
    #     # self.send_video()
    
    def __call__(self):
        self.picam = self.get_video_capture()
        self.img_sender = self.get_image_sender()
        self.connect()
        threading.Thread(target=self.listener).start()
        threading.Thread(target=self.send_video).start()
        
    def get_video_capture(self) -> cv2.VideoCapture:
        cap = cv2.VideoCapture(self.capture_index)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
        print("[IMGREC/INFO] Waiting 2 seconds to warm up camera")
        time.sleep(2)
        print("[IMGREC/INFO] Completed")
        assert cap.isOpened()
        return cap
    
    def get_image_sender(self) -> imagezmq.ImageSender:
        # imagezmq.ImageSender(connect_to=self.zmq_address, REQ_REP=False)
        return imagezmq.ImageSender(connect_to=self.zmq_address)
    
    # def get_file_count(self) -> int:
    #     _, _, files = next(os.walk(IMG_DIR))
    #     return len(files)
    
    def connect(self):
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
            else:
                print(f"[IMGREC/INFO] Connection from {self.c_addr}")
                break
        self.s_sock.close()

    def take_picture(self, name="img{idx}.{img_format}"):
        # img_name = name.format(idx=self.idx, img_format=self.img_format)
        for _ in range(self.no_of_pic):
            ret, frame = self.picam.read()
            if ret == True:
                self.img_sender.send_image(self.rpi_name, frame)    
                print(f"[IMGREC/INFO] Sending frames {_}")

    def send_video(self):
        print("[IMGREC_VID/INFO] Starting video thread")
        while not self.kill_flag:
            ret, frame = self.picam.read()
            while self.send_image_flag:
                if ret == True:
                    print("[IMGREC_VID/INFO] Sending image")
                    self.img_sender.send_image(self.rpi_name, frame)
            
                # Break the loop
                else: 
                    print("[IMGREC_VID/INFO] ret = False")
                    break
            
        self.picam.close()
        
    def listener(self) -> "workerThread":
        self.stop_vid_flag = False
        disconnect_flag = False
        
        print("[IMGREC_LISTENER/INFO] Starting listener thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                rcv_data = self.c_sock.recv(1024).decode()
                if rcv_data:
                    if rcv_data == "disconnect": break
                    print(f"[IMGREC_LISTENER/INFO] IMGREC received {rcv_data}")
                    IMGREC_IN.put(rcv_data)
            except KeyboardInterrupt:
                disconnect_flag = True
            except:
                disconnect_flag = True
                traceback.print_exc()
                pass
        print("[IMGREC_LISTENER/INFO] Exiting listener thread")
        self.stop_vid_flag = True
    
if __name__ == "__main__":
    imInt = ImageRecInterface()
    imInt()
