'''
Filename: imgrecInterface.py
Version: v0.7

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
'''
import os
import cv2
import time
import socket
import imagezmq
import traceback
import threading
from .helper import IMGREC_IN, IMGREC_OUT, IMGREC_PORT, IMG_DIR

HEIGHT = 480
WIDTH = 640
RPI_IP = "192.168.15.1"
ZMQ_IP = "192.168.15.69"
ZMQ_PORT = 5555

NO_OF_PIC = 5
IMG_FORMAT = "jpg"

# class ImageRecInterface(threading.Thread):
class ImageRecInterface:
    def __init__(self, rpi_ip=RPI_IP, imgrec_port=IMGREC_PORT, 
                 zmq_ip=ZMQ_IP, zmq_port=ZMQ_PORT,  
                 no_of_pic=NO_OF_PIC, img_format=IMG_FORMAT, 
                 capture_index=0
                 ):
        # super().__init__()        # thread
        self.lock = threading.Lock()
        self.rpi_ip = rpi_ip
        self.imgrec_port = imgrec_port
        self.zmq_ip=zmq_ip
        self.zmq_port=zmq_port
        self.zmq_address = f"tcp://{zmq_ip}:{zmq_port}"
        self.no_of_pic=no_of_pic
        self.img_format = img_format
        self.capture_index = capture_index
        
        self.kill_flag = False
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
        assert cap.isOpened()
        print("[IMGREC/INFO] Camera completed")
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
        self.picam.close()
        
    def show_video(self):
        disconnect_flag = False
        while self.picam.isOpened() and not disconnect_flag:
            ret, frame = self.picam.read()
            if ret == True:
                # Display the resulting frame
                cv2.imshow('Frame',frame)        
                #cv2.imwrite('c1.png',frame)
                self.img_sender.send_image("img", frame)
                
                # Press Q on keyboard to  exit
                if cv2.waitKey(25) & 0xFF == ord('q'):
                  break
            # Break the loop
            else: 
                print("[IMGREC/INFO] ret = False")
                break
            
        # When everything done, release the video capture object
        self.picam.release()
        # Closes all the frames
        cv2.destroyAllWindows()
        
    def listener(self) -> "workerThread":
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
