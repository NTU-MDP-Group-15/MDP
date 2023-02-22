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

'''
import os
import cv2
import time
import socket
import imagezmq
import traceback
import threading
from .helper import IMGREC_IN, IMGREC_OUT, IMGREC_PORT

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(os.path.split(CUR_DIR)[0], "photos")
WIDTH = 640
HEIGHT = 480
RPI_IP = "192.168.15.1"
ZMQ_IP = "192.168.15.69"
ZMQ_PORT = 5555

NO_OF_PIC = 5
IMG_FORMAT = "jpg"

#class ImageRecInterface:
class ImageRecInterface(threading.Thread):
    def __init__(self, rpi_ip=RPI_IP, imgrec_port=IMGREC_PORT, 
                 zmq_ip=ZMQ_IP, zmq_port=ZMQ_PORT,  
                 no_of_pic=NO_OF_PIC, img_format=IMG_FORMAT, 
                 capture_index=0
                 ):
        super().__init__()
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
        # self.idx = self.get_file_count()
        
        self.rpi_name = socket.gethostname()
        self.picam = self.get_video_capture()
        self.img_sender = self.get_image_sender()
               
    def run(self):
        self.connect()
        threading.Thread(target=self.listener).start()
        self.send_video()
       
    def get_video_capture(self) -> cv2.VideoCapture:
        print("[IMGREC/INFO] Waiting 2 seconds to warm up camera")
        cap = cv2.VideoCapture(self.capture_index)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
        time.sleep(2)
        print("[IMGREC/INFO] Completed")
        assert cap.isOpened()
        return cap
    
    def get_image_sender(self) -> imagezmq.ImageSender:
        # imagezmq.ImageSender(connect_to=self.zmq_address, REQ_REP=False)
        return imagezmq.ImageSender(connect_to=self.zmq_address)
    
    def get_file_count(self) -> int:
        _, _, files = next(os.walk(IMG_DIR))
        return len(files)
    
    def connect(self):
        self.s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s_sock.bind((self.rpi_ip, self.imgrec_port))
        self.s_sock.listen(1)
        
        while True:
            try:
                self.c_sock, self.c_addr = self.s_sock.accept()
            except socket.timeout:
                pass
            except KeyboardInterrupt:
                print("[IMGREC/INFO] Recevied KeyboardInterrupt")
                self.kill_flag = True
            else:
                print(f"[IMGREC/INFO] Connection from {self.c_addr}")
                break
        self.s_sock.close()
        
        # with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as self.s_sock:
        #     self.s_sock.bind((IP_ADDRESS, IMGREC_PORT))
        #     self.s_sock.listen(1)
            
        #     self.c_sock, self.c_addr = self.s_sock.accept()
        #     print(f"[IMGREC/INFO] Connection from {self.c_addr}")        


    def send_video(self):
        while not self.kill_flag:
            while not self.stop_vid_flag:
                ret, frame = self.picam.read()
                if ret == True:
                    # Display the resulting frame
                    self.img_sender.send_image(self.rpi_name, frame)
                    print("Sending")
                
                # Break the loop
                else: 
                    print("[IMGREC/INFO] ret = False")
                    break

    def take_picture(self, name="img{idx}.{img_format}"):
        # img_name = name.format(idx=self.idx, img_format=self.img_format)
        for _ in range(self.no_of_pic):
            ret, frame = self.picam.read()
            if ret == True:
                self.img_sender.send_image(self.rpi_name, frame)    
    
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
        self.stop_vid_flag = False
        disconnect_flag = False
        
        print("[IMGREC/INFO] Starting listener thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                rcv_data = self.c_sock.recv(1024).decode()
                if rcv_data:
                    if rcv_data == "disconnect": break
                    print(f"[IMGREC/INFO] IMGREC received {rcv_data}")
                    IMGREC_IN.put(rcv_data)
            except KeyboardInterrupt:
                disconnect_flag = True
            except:
                disconnect_flag = True
                traceback.print_exc()
                pass
        print("[IMGREC/INFO] Exiting listener thread")
        self.stop_vid_flag = True

    def sender(self) -> "workerThread":
        disconnect_flag = False
        print("[IMGREC/INFO] Starting sender thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                if not IMGREC_OUT.empty():
                    send_data = IMGREC_OUT.get().encode()
                    print(f"[IMGREC/INFO] Sending to laptop: {send_data}")
                    # self.stm.write(send_data)
                    # self.stm.flush()        # self.stm.flushInput()
            except:
                traceback.print_exc()
        print("[IMGREC/INFO] Exiting sender thread")
    
    
    def old_sender(self) -> "workerThread":
        disconnect_flag = False
        print("[IMGREC/INFO] Starting sender thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                if not IMGREC_OUT.empty():
                    send_data = IMGREC_OUT.get().encode()
                    print(f"[IMGREC/INFO] Sending to laptop: {send_data}")
                    # self.stm.write(send_data)
                    # self.stm.flush()        # self.stm.flushInput()
            except:
                traceback.print_exc()
        print("[IMGREC/INFO] Exiting sender thread")
    
                
if __name__ == "__main__":
    imInt = ImageRecInterface()
    imInt.video()
