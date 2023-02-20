import cv2
import imagezmq
import socket
import threading

class imrecInterface:
    def __init__(self, RPI):
        self.RPI = RPI
        self.clientSocket = socket.socket()
        self.sender = None
        self.attempts = 0
    
    def connectImrec(self):
        self.clientSocket, self.address = self.RPI.serverSocket.accept()
        print("IMREC Connected on: ", self.address)
        welcomeMessage = "Welcome to Server (IMREC)"
        self.write(welcomeMessage)
        listenThread = threading.Thread(target = self.read)
        listenThread.start()
        #self.sender = imagezmq.ImageSender(connect_to='tcp://192.168.20.25:5555')
    
    def read(self):
        while True:
            try:
                message = self.clientSocket.recv(1024)
                message = message.decode('utf-8')

                if message:
                    print("From IMREC:",message)
                    # Send the image result to both android and algo
                    if message == "NOTHING" and self.attempts < 4:
                        print("Executing failure attempt: ", str(self.attempts))
                        self.RPI.stm.send('a')
                        self.take_picture()
                        self.attempts= self.attempts + 1
                        
                    else:
                        while self.attempts > 0:
                            self.RPI.stm.send('0')
                            self.attempts = self.attempts -1
                        
                        print("Attempts remaining:", self.attempts)
                        
                        messageToAndroid = 'TARGET,' + str(1) +',' + message
                        self.RPI.android.write(messageToAndroid)
                        self.RPI.algo.write(message)
                        
            except Exception as e:
                print("IMREC Disconnected! (imrec READ)")
                self.connectImrec()
    def write(self,message):
        self.clientSocket.send(message.encode())
            
            

    def take_picture(self):
        sender = imagezmq.ImageSender(connect_to='tcp://192.168.20.25:5555')
        cam = cv2.VideoCapture(0)
        cam.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        cam.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
        rpi_name = socket.gethostname()
        check, image = cam.read()
        try:
            sender.send_image(rpi_name, image)
            print("Image sent to IMREC server!")
        except Exception as e:
            print("Image sending failed!")