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
|     13XXX      | turn right for xxx angle(backward)             |
|     20001      | STM received command flag                      |
|     20002      | STM completed command flag                     |
-------------------------------------------------------------------
20XXX - finish bit to send to android
"done" (4byte) as acknowledgement
Queue for instructions (ack before sending in next)
'''

import serial
import threading
from .helper import STM_IN, STM_OUT

SERIAL_PORT = '/dev/ttyUSB0' 
BAUD_RATE = 115200
BYTESIZE = serial.EIGHTBITS     # or serial.FIVEBITS
PARITY = serial.PARITY_NONE
STOPBITS = serial.STOPBITS_ONE
TIMEOUT = 0

#class STMInterface(threading.Thread):
class STMInterface:
    def __init__(self, port=SERIAL_PORT, baud_rate=BAUD_RATE, byte_size=BYTESIZE, parity=PARITY, 
                 stopbits=STOPBITS, timeout=TIMEOUT):
        # super().__init__()        # threads
        self.port = port
        self.baud_rate = baud_rate
        self.byte_size = byte_size
        self.parity = parity
        self.stopbits = stopbits
        self.timeout = timeout
        self.receive_flag = False
        self.complete_flag = False
        self.disconnected_flag = False
        self.stm = None
        
    def start(self):
        self.connect()
        
        listener_thread = threading.Thread(target=self.listener)
        sender_thread = threading.Thread(target=self.sender)
        listener_thread.start()
        sender_thread.start()
        
        self.disconnect()

    """
    def run(self):
        self.connect()
        listener_thread = threading.Thread(target=self.listener)
        sender_thread = threading.Thread(target=self.sender)
        listener_thread.start()
        sender_thread.start()
        self.disconnect()
    """ 

    def connect(self):
        while True:
            try:
                print("[STM/INFO] Awaiting connection from STM...")
                self.stm = serial.Serial(port=self.port, 
                                         baudrate=self.baud_rate, 
                                         bytesize=self.byte_size,
                                         parity=self.parity, 
                                         stopbits=self.stopbits, 
                                         timeout=self.timeout)
                print(f"[STM/INFO] Connected to STM via {self.port}")
                self.disconnected_flag = True
            except:
                print(f"[STM/ERROR] Failed to connect on {self.port}")
                pass
            
            finally:
                break

    def disconnect(self):
        print(f"[STM/INFO] Disconnecting on {self.port}")
        try:
            self.stm.close()
        except:
            print(f"[STM/Error] Failed to disconnect")

    def send(self, command):
        print(f"[STM/INFO] Sending to STM: {command}")
        command = command.encode()          # Need encoding? ascii/utf-8?
        self.stm.write(command)
        self.stm.flushInput()
        #self.receive()

    def listener(self) -> "workerThread":
        while not self.disconnected_flag:
            try:
                rcv_data = int(self.stm.read().lstrip().rstrip())     # might not need lstrip/rstrip
                print("[STM/INFO] STM received command")
                if (rcv_data == 20001):
                    print("[STM/INFO] STM received command")
                    self.receive_flag = True
                elif (rcv_data == 20002): self.complete_flag = True
                
                STM_IN.put(rcv_data)
            except KeyboardInterrupt:
                self.disconnected_flag = True
            except:
                pass

    def sender(self) -> "workerThread":
        while not self.disconnected_flag:
            try:
                if not STM_OUT.empty():
                    send_data = STM_IN.get_nowait().encode()
                    print(f"[STM/INFO] Sending to STM: {send_data}")
                    self.stm.write(send_data)
                    self.stm.flush()        # self.stm.flushInput()
            except:
                pass

if __name__ == "__main__":
    stmTest = STMInterface()
    #stmTest.send('00001')

