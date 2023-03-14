'''
Filename: stmInterface.py
Version: v1.4

Class for setting up connection sockets for algo
! Updates (DDMMYY)
200223 - Added Queue for task A5
         Added logic for BT -> RPI -> STM
         Added traceback for error
210223 - Replaced self.disconnected_flag to self.kill_flag
         disconnect_flag is now local variable
230223 - Added threading locks for receiving
060323 - refactored disconnect() to allow easier calling  
100323 - Changed baud rate to 9600 instead (not sure why but it works better than 115200)
-------------------------------------------------------------------
| Command (5bit) |     Action                                     |
|------------------------------------------------------------------
|     00XXX      | stop Movement                                  |
|     01XXX      | move forward for XXX distance (straight)       |
|     02XXX      | turn left for XXX angle (forward)              |
|     03XXX      | turn right for XXX angle (forward)             |
|     11XXX      | move backward for XXX distance(straight line)  |
|     12XXX      | turn left for XXX angle(backward               |
|     13XXX      | turn right for XXX angle(backward)             |
|     20001      | STM received command flag                      |
|     20002      | STM completed command flag                     |
|     DONE       |                                                |
-------------------------------------------------------------------
Queue for instructions (ack before sending in next)
'''
import time
import serial
import traceback

SERIAL_PORT = '/dev/ttyUSB0' 
#BAUD_RATE = 115200
BAUD_RATE = 9600
BYTESIZE = serial.EIGHTBITS     # or serial.FIVEBITS
PARITY = serial.PARITY_NONE
STOPBITS = serial.STOPBITS_ONE
TIMEOUT = 0

class STMInterface:
    def __init__(self, port=SERIAL_PORT, baud_rate=BAUD_RATE, byte_size=BYTESIZE, parity=PARITY, 
                 stopbits=STOPBITS, timeout=TIMEOUT):
        self.port = port
        self.baud_rate = baud_rate
        self.byte_size = byte_size
        self.parity = parity
        self.stopbits = stopbits
        self.timeout = timeout
        self.stm = None
        
        # Flags to control behaviour
        self.complete_flag = False
        self.receive_flag = False
        
    def __call__(self):
        self.connect()
        
    def connect(self) -> bool:
        while True:
            print(f"[STM/INFO] Attempt connection on port: {self.port}")
            try:
                
                self.stm = serial.Serial(port=self.port, 
                                         baudrate=self.baud_rate, 
                                         bytesize=self.byte_size,
                                        # parity=self.parity, 
                                        # stopbits=self.stopbits, 
                                         timeout=self.timeout
                                        )
                print(f"[STM/INFO] Connected to STM via {self.port}")
                return True
            except:
                print(f"[STM/INFO] Failed connection on port: {self.port}")
                self.port = '/dev/ttyUSB1'
                print(f"[STM/INFO] Attempt connection on port: {self.port}")
                try:
                    self.stm = serial.Serial(port=self.port, 
                                             baudrate=self.baud_rate, 
                                             bytesize=self.byte_size,
                                            # parity=self.parity, 
                                            # stopbits=self.stopbits, 
                                             timeout=self.timeout
                                            )
                    print(f"[STM/INFO] Connected to STM via {self.port}")
                    return True
                    break
                except:
                    print(f"[STM/ERROR] Failed to connect on {self.port}")

                    traceback.print_exc()
                    break
        return False

    def disconnect(self):                
        if self.stm:
            try:
                print(f"[STM/INFO] Disconnecting on {self.port}")
                self.stm.close()
            except:
                print(f"[STM/Error] Failed to disconnect")
                traceback.print_exc()

    def reconnect(self):
        self.stm.close()
        self.connect()
        return True
        
    def readline(self) -> bytes:
        rcv_data = self.stm.readline()
        self.stm.flushOutput()
        return rcv_data
    
    def write(self, data) -> None:
        print(f"[STM/INFO] Sending {data}")
        #self.stm.flushInput()
        self.stm.write(data)
        self.stm.flushInput()        # self.stm.flushInput()
        print(f"[STM/INFO] Sent {data}")