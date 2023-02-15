'''
Filename: imgrecInterface.py
Version: 0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)

'''
import os
import cv2
import socket 
import imagezmq
import threading
from .helper import debug

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(os.path.split(CUR_DIR)[0], "photos")

class ImageRecInterface():
    def __init__(self, img_format="jpg"):
        #self.picam = cv2.VideoCapture(0)
        #self.picam.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
        #self.picam.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)
        self.img_format = img_format
        self.idx = self.get_file_count()
        debug(__name__, "...")
        
    def get_file_count(self, photo_name="img{idx}.{img_format}"):
        _, _, files = next(os.walk(IMG_DIR))
        return len(files)
        
    def take_picture(self, name="img{idx}.{img_format}"):
        img_name = name.format(idx=self.idx, img_format=self.img_format)
        if self.picam.isOpened():
            _, frame = self.picam.read()
            self.picam.release()
            if _ and frame is not None:
                cv2.imwrite(os.path.join(IMG_DIR, img_name), frame)
        
if __name__ == "__main__":
    imInt = ImageRecInterface()
    