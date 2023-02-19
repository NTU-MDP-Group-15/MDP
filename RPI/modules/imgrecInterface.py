'''
Filename: imgrecInterface.py
Version: 0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)
18/02/23 - Added imagezmq server to send images captured

'''
import os
import cv2
import imagezmq
import threading
from .helper import IMGREC_IN, IMGREC_OUT, IMGREC_PORT

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(os.path.split(CUR_DIR)[0], "photos")
WIDTH = 640
HEIGHT = 480
IP_ADDRESS = ""

#class ImageRecInterface:
class ImageRecInterface(threading.Thread):
    def __init__(self, img_format="jpg"):
        super().__init__()
        self.img_format = img_format
        self.disconnected_flag = False
        #self.idx = self.get_file_count()
        self.picam = cv2.VideoCapture(0)
        self.picam.set(cv2.CAP_PROP_FRAME_WIDTH, WIDTH)
        self.picam.set(cv2.CAP_PROP_FRAME_HEIGHT, HEIGHT)
    
    def run(self):
        self.img_sender = imagezmq.ImageSender(connect_to=f"tcp://{IP_ADDRESS}:{IMGREC_PORT}")
        threading.Thread(target=self.listener).start()
        self.video()
        
        
    def get_file_count(self) -> int:
        _, _, files = next(os.walk(IMG_DIR))
        return len(files)

    def take_picture(self, name="img{idx}.{img_format}"):
        img_name = name.format(idx=self.idx, img_format=self.img_format)
        if self.picam.isOpened():
            _, frame = self.picam.read()
            if _ and frame is not None:
                cv2.imwrite(os.path.join(IMG_DIR, img_name), frame)

    def video(self):
        while self.picam.isOpened() and not self.disconnected_flag:
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
        while not self.disconnected_flag:
            try:
                rcv_data = int(self.stm.read().lstrip().rstrip())     # might not need lstrip/rstrip
                print(f"[IMGREC/INFO] IMGREC received {rcv_data}")
                IMGREC_IN.put(rcv_data)
            except KeyboardInterrupt:
                self.disconnected_flag = True
            except:
                pass
        
                
if __name__ == "__main__":
    imInt = ImageRecInterface()
    imInt.video()