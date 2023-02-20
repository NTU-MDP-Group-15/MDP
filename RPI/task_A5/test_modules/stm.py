'''
Filename: stm.py
Version: v0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)

-------------------------------------------------------------------
| Command (5bit) |     Action                                     |
|------------------------------------------------------------------
|     00XXX      | Stop Movement                                  |
|     01xXx      | Move forward for xx distance (straight)        |
|     02XXX      | Turn left for XXX angle (forward)              |
|     03XXX      | turn right for XXX angle (forward)             |
|     11XXX      | move backward for XX distance(straight line)   |
|     12XXX      | turn left for xxx angle(backward               |
|     13XXX      | turn right for xxx anlge(backward)             |
-------------------------------------------------------------------

"done" (4byte) as acknowledgement
'''

import serial
import threading
import time

#class STMInterface(threading.Thread):
class STMInterface():
    def __init__(self):
        # super().__init__()
        try:
            print("[STM/INFO] Awaiting connection from STM...")
            self.serial = serial.Serial('/dev/ttyUSB0', baudrate=115200, bytesize=serial.EIGHTBITS, parity=serial.PARITY_NONE, stopbits=serial.STOPBITS_ONE, timeout=3) # requires changing of bytesize
            print("[STM/INFO] Connected to STM via USB 0")
        except:
            print("[STM/INFO] Failed to connect on USB 0")
            pass
            
    def send(self, command):
        print(f"[STM/INFO] Sending Commands to STM: {command}")
        command = str.encode(command)
        self.serial.write(sc)
        self.serial.flushInput()
        
        # Our STM sends two KKs , one when receive command, one when command fully excecuted
        first_k = False
        while True:
            try:
                s = self.serial.read().rstrip()
                s = s.lstrip()
                if s.decode() == 'K':
                    if(first_k):
                        print(s.decode())
                        break
                    first_k = True
            except:
                print("Failed to send command to STM!")
                break

#stmTest = STMInterface()
#stmTest.send('L')
