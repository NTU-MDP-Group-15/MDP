'''
Filename: stmInterface.py
Version: v0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)

-------------------------------------------------------------------
| Command (5bit) |     Action                                     |
|------------------------------------------------------------------
|     00XXX      | stop Movement                                  |
|     01XXX      | move forward for xx distance (straight)        |
|     02XXX      | turn left for XXX angle (forward)              |
|     03XXX      | turn right for XXX angle (forward)             |
|     11XXX      | move backward for XX distance(straight line)   |
|     12XXX      | turn left for xxx angle(backward               |
|     13XXX      | turn right for xxx anlge(backward)             |
|     20001      | STM received command flag                      |
|     20002      | STM completed command flag                     |
-------------------------------------------------------------------
20XXX - finish bit to send to android
"done" (4byte) as acknowledgement
'''

import serial
import threading
import time
#from helper import INPUT

#class STMInterface(threading.Thread):
class STMInterface():
    def __init__(self):
        # super().__init__()
        self.receive_flag = False
        self.complete_flag = False
        self.disconnected_flag = False
        try:
            print("[STM/INFO] Awaiting connection from STM...")
            self.serial = serial.Serial('/dev/ttyUSB0', baudrate=115200, 
                                        bytesize=serial.FIVEBITS,           # maybe FIVEBITS does not exist...
                                        parity=serial.PARITY_NONE, 
                                        stopbits=serial.STOPBITS_ONE, 
                                        timeout=0)
            print("[STM/INFO] Connected to STM via USB 0")
            self.receive()
        except:
            print("[STM/INFO] Failed to connect on USB 0")
            pass
            
    def send(self, command):
        '''
        while not self.disconnected_flag:
            if not INPUT.isEmpty():
                input = INPUT.get()
                self.serial.write(input)
        '''
        
        print(f"[STM/INFO] Sending to STM: {command}")
        command = command.encode()          # Need encoding? ascii/utf-8?
        self.serial.write(command)
        self.serial.flushInput()
        self.receive()
    
    def receive(self):
        while not self.complete_flag:
            try:
                receive_data = int(self.serial.read().lstrip().rstrip())     # might not need lstrip/rstrip
                if (receive_data == 20001):
                    print("[STM/INFO] STM received command")
                    self.receive_flag = True
                elif (receive_data == 20002): self.complete_flag = True
            except:
                print(f"[STM/INFO] Failed to send: {command}")
                pass
    
if __name__ == "__main__":
    stmTest = STMInterface()
    stmTest.send('00001')
