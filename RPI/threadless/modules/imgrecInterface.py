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

from .config import RPI_IP, ZMQ_IP, IMGREC_PORT, ZMQ_PORT, NO_OF_PIC, OBSTACLE_ID

HEIGHT = 480
WIDTH = 640

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
        
        self.s_sock = None
        self.c_sock = None
        self.rpi_name = socket.gethostname()
        
        self.picam = self.get_video_capture()
        self.lock = threading.Lock()
        self.kill_flag = False
        self.send_image_flag = False
    
    def start_thread(self):
        sv_thread = threading.Thread(target=self.send_video)
        sv_thread.start()
        #recv_thread = threading.Thread(target=self.receive)
        #recv_thread.start()
    
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
    
    def connect(self) -> bool:
        try:
            self.s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.s_sock.bind((self.rpi_ip, self.imgrec_port))
            self.s_sock.listen(1)
            print("[IMGREC/INFO] Waiting for connection")
        except Exception as e:
            print(e)
        else:
            while True:
                try:
                    self.c_sock, self.c_addr = self.s_sock.accept()
                except socket.timeout:
                    pass
                except KeyboardInterrupt:
                    print("[IMGREC/INFO] Received KeyboardInterrupt")
                    break
                else:
                    print(f"[IMGREC/INFO] Connection from {self.c_addr}")
                    self.img_sender = self.get_image_sender()
                    return True
        return False
    
    def disconnect(self) -> None:
        self.kill_flag = True
        if self.s_sock: 
            self.s_sock.close()
        if self.c_sock:
            self.c_sock.close()
        if self.picam.isOpened():
            self.picam.release()
            
    def receive(self) -> str:
        while True:
            try:
                rcv_data = self.c_sock.recv(1024)
                if rcv_data:
                    rcv_data = rcv_data.decode()
                    print(f"[IMGREC/INFO] IMGREC received {rcv_data}")
                    break
            except KeyboardInterrupt:
                break
            except:
                traceback.print_exc()
        return rcv_data
        
    def send_video(self) -> "workerThread":
        print("[IMGREC_VID/INFO] Starting video thread")
        while not self.kill_flag:
            _, _ = self.picam.read()
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
                self.send_image_flag = False
        print("[IMGREC_VID/INFO] Exiting video thread... Releasing cam")
        self.picam.release()
    
    def take_send_picture(self) -> None:
        picam = self.get_video_capture()
        if picam:
            for _ in range(self.no_of_pic):
                ret, frame = picam.read()
                if ret == True:
                    print("[IMGREC/INFO] Sending frame")
                    self.img_sender.send_image(self.rpi_name, frame)

                # Break the loop
                else: 
                    print("[IMGREC/INFO] ret = False")
                    break
        else:
            print("[IMGREC/INFO] ERROR WITH PICAM")
        picam.release()
