import bluetooth
import threading
import socket
import os
class androidInterface:
    def __init__(self, RPI):
        self.RPI = RPI
        self.clientSocket = socket.socket()
        self.serverSocket = socket.socket()
        self.bd_addr = "48:61:EE:2A:AA:18"
        self.UUID = '0000110d-0000-1000-8000-00805F9B34FB'
        self.port = 1
        self.ANDROID_SOCKET_BUFFER_SIZE = 512
            
    def connectAndroid(self):
        
        os.system('sudo chmod o+rw /var/run/sdp')
        os.system('sudo hciconfig hci0 piscan')
        os.system('sudo usermod -G bluetooth -a pi')
        os.system('sudo chgrp bluetooth /var/run/sdp')
        self.serverSocket=bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.serverSocket.bind(("", self.port))
        self.serverSocket.listen(1)
        bluetooth.advertise_service(
                self.serverSocket, 
                'MDP-Team20',
                service_id=self.UUID,
                service_classes=[self.UUID, bluetooth.SERIAL_PORT_CLASS],
                profiles=[bluetooth.SERIAL_PORT_PROFILE]
            )
        print("Waiting for connection on RFCOMM channel %d" % self.port)
        self.clientSocket, self.address = self.serverSocket.accept()
        print("ANDROID Connected on: ", self.address)
        welcomeMessage = "Welcome to Server (ANDROID)"
        self.write(welcomeMessage)

        #start listen threads
        listenThread = threading.Thread(target = self.read)
        listenThread.start()
    
    def write(self,message):
        try:
            print(message)
            self.clientSocket.send(message.encode())
            print('Sent to Android:', message)
        except Exception as e:
             print("Android Disconnected! (Android WRITE)")
             self.connectAndroid()
    
    def read(self):
        while True:
            try:
                message = self.clientSocket.recv(self.ANDROID_SOCKET_BUFFER_SIZE).strip()
                message = message.decode()

                #Android only sends to algo
                if message:
                    print('From ANDROID:', message)
                    self.RPI.algo.write(message)
            except Exception as e:
                print("Android Disconnected! (Android READ)")
                self.connectAlgo()



    
