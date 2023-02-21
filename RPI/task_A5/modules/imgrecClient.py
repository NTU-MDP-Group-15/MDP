'''
Filename: imgrecClient.py
Version: v0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)
         
'''
import cv2
import imagezmq
import socket
import traceback
import threading

class imgrecClient:
    def __init__(self):
        self.clientSocket = socket.socket()
        self.sender = None
        self.attempts = 0
        self.running = False
        self.sender = imagezmq.ImageSender(connect_to='tcp://192.168.4.14:5555')
        self.cam = cv2.VideoCapture(0)
        # send RPi hostname with each image
        self.rpi_name = socket.gethostname()
        
    def run(self):
        
        while True:
            # Capture live video stream using the camera
            ret, frame = self.cam.read()
            
            try:
                self.sender.send_image(self.rpi_name, frame)
                print("Video stream sent to server!")
            except Exception as e:
                print("Video stream sending failed!")
                break

# Initialize the camera, set up the Raspberry Pi camera
    def disconnect(self):
        self.cam.release()
        cv2.destroyAllWindows()