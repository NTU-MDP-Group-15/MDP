'''
Filename: stmInterface.py
Version: v0.2a

Class for setting up connection sockets for algo
! Updates (DDMMYY)
200223 - Added Queue for task A5
         Added logic for BT -> RPI -> STM
         Added traceback for error
210223 - Replaced self.disconnected_flag to self.kill_flag
         disconnect_flag is now local variable
         
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
import traceback
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
        self.kill_flag = False
        self.stm = None
        
    def start(self):
        self.connect()
        listener_thread = threading.Thread(target=self.listener)
        sender_thread = threading.Thread(target=self.sender)
        listener_thread.start()
        sender_thread.start()
        #self.disconnect()

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
            except:
                print(f"[STM/ERROR] Failed to connect on {self.port}")
                self.kill_flag = True     
                traceback.print_exc()
                       
            finally:
                break

    def disconnect(self):
        print(f"[STM/INFO] Disconnecting on {self.port}")
        try:
            self.stm.close()
        except:
            print(f"[STM/Error] Failed to disconnect")
            traceback.print_exc()

    def decode_instr(self, instr):
        sub_instr_arr = instr.split(',')
        
        for sub_instr in sub_instr_arr:
            while not self.complete_flag: pass      # Might use a lot of resource potential fix -> put time.sleep(0.5)
            self.stm.write(sub_instr.rstrip())
            self.complete_flag = False
        
        STM_IN.put("PIC")

    def listener(self) -> "workerThread":
        disconnect_flag = False
        print("[STM/INFO] Starting listener thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                rcv_data = int(self.stm.read().lstrip().rstrip())     # might not need lstrip/rstrip
                print("[STM/INFO] STM received command")
                
                if (rcv_data == 20001):
                    print("[STM/INFO] STM received command")
                    self.receive_flag = True
                    
                # sub instr complete
                elif (rcv_data == 20002): 
                    self.complete_flag = True
                else:
                    STM_IN.put(rcv_data)
            except KeyboardInterrupt:
                disconnect_flag = True
            except:
                pass
        print("[STM/INFO] Exiting listener thread")

    def sender(self) -> "workerThread":
        disconnect_flag = False
        print("[STM/INFO] Starting sender thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                if not STM_OUT.empty():
                    send_data = STM_OUT.get().encode()
                    print(f"[STM/INFO] Sending to STM: {send_data}")
                    self.stm.write(send_data)
                    self.stm.flush()        # self.stm.flushInput()
            except:
                traceback.print_exc()
        print("[STM/INFO] Exiting sender thread")
    
    def send(self, data):
        self.stm.write(data)
        self.stm.flush()        # self.stm.flushInput()
        
        

if __name__ == "__main__":
    import traceback
    stmTest = STMInterface()
    stmTest.start()
    while True:
        try:
            u_input = input(">")
            STM_OUT.put(u_input)
            
        except KeyboardInterrupt:
            break
