'''
Filename: stmInterface.py
Version: v0.5

Class for setting up connection sockets for algo
! Updates (DDMMYY)
200223 - Added Queue for task A5
         Added logic for BT -> RPI -> STM
         Added traceback for error
210223 - Replaced self.disconnected_flag to self.kill_flag
         disconnect_flag is now local variable
230223 - Added threading locks for receiving
         
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
from .helper import STM_IN, STM_OUT, TAKE_PIC

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
        self.lock = threading.Lock()
        
    def __call__(self):
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
            print(f"[STM/INFO] Attempt connection on port: {self.port}")
            try:
                self.stm = serial.Serial(port=self.port, 
                                         baudrate=self.baud_rate, 
                                         bytesize=self.byte_size,
                                         parity=self.parity, 
                                         stopbits=self.stopbits, 
                                         timeout=self.timeout)
                print(f"[STM/INFO] Connected to STM via {self.port}")
            except:
                print(f"[STM/INFO] Failed connection on port: {self.port}")
                self.port = '/dev/ttyUSB1'
                print(f"[STM/INFO] Attempt connection on port: {self.port}")
                try:
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
                finally: break
            finally: break

    def disconnect(self):
        print(f"[STM/INFO] Disconnecting on {self.port}")
        try:
            self.stm.close()
        except:
            print(f"[STM/Error] Failed to disconnect")
            traceback.print_exc()

    def decode_and_send__instr(self, instr):
        sub_instr_arr = instr.split(',')
        print(f"[STM/INFO] {sub_instr_arr}")
        
        for sub_instr in sub_instr_arr:
            sub_instr = sub_instr.rstrip().encode()
            print(f"[STM/INFO] Sending {sub_instr}")
            self.stm.write(sub_instr)
            self.stm.flush()            # self.stm.flushInput()
            
            while not self.complete_flag: pass      # Might use a lot of resource potential fix -> put time.sleep(0.5)
            self.complete_flag = False
        
        print(f"[STM/INFO] Completed instructions")
        print(f"[STM/INFO] Putting '{TAKE_PIC}' into STM_IN")
        STM_IN.put(TAKE_PIC)

    def listener(self) -> "workerThread":
        disconnect_flag = False
        print("[STM_LISTENER/INFO] Starting listener thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                self.lock.acquire()
                rcv_data = int(self.stm.readline())     # might not need lstrip/rstrip
                self.lock.release()
                
                if rcv_data != b'':
                    print(f"[STM_LISTENER/INFO] received {rcv_data}")
                
                    # sub instr complete
                    if rcv_data == b'Done\x00':
                        self.complete_flag = True
                    # STM_IN.put(rcv_data)
                    
            except KeyboardInterrupt:
                disconnect_flag = True
            except:
                traceback.print_exc()
                pass
            
        print("[STM_LISTENER/INFO] Exiting listener thread")

    def sender(self) -> "workerThread":
        disconnect_flag = False
        print("[STM_SENDER/INFO] Starting sender thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                if not STM_OUT.empty():
                    send_data = STM_OUT.get().encode()
                    # print(f"[STM_SENDER/INFO] Sending to STM: {send_data}")
                    self.decode_and_send__instr(send_data)
                    # self.stm.write(send_data)
                    # self.stm.flush()        # self.stm.flushInput()
            except:
                traceback.print_exc()
        print("[STM_SENDER/INFO] Exiting sender thread")
    
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
