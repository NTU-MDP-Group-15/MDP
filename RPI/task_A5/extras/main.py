import os
import bluetooth
import threading
from to_algo import*
from to_imrec import*
from to_android import*
from to_stm import*
import socket


class RPI:
    def __init__(self):
        self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.serverSocket.bind(('', 1234))
        self.serverSocket.listen(10)
        # initialise STM
        self.stm = STMInterface(self)

        # Initialise Android Interface
        self.android = androidInterface(self)

        # initialise algo interface
        self.algo = algoInterface(self)

        # initialise imrec interface
        self.imrec = imrecInterface(self)

    def connect(self):
        self.android.connectAndroid()
        self.algo.connectAlgo()
        self.imrec.connectImrec()
rpi = RPI()
rpi.connect()

        


        



