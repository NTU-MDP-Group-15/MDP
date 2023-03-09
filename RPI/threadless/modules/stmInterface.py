'''
Filename: stmInterface.py
Version: v1.3

Class for setting up connection sockets for algo
! Updates (DDMMYY)
200223 - Added Queue for task A5
         Added logic for BT -> RPI -> STM
         Added traceback for error
210223 - Replaced self.disconnected_flag to self.kill_flag
         disconnect_flag is now local variable
230223 - Added threading locks for receiving
060323 - refactored disconnect() to allow easier calling  

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
import threading

from .helper import STM_IN, STM_OUT, TAKE_PIC, ANDROID_OUT

SERIAL_PORT = '/dev/ttyUSB0' 
BAUD_RATE = 115200
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
        self.lock = threading.Lock()
        self.kill_flag = False
        self.complete_flag = False
        self.receive_flag = False
        
    def __call__(self):
        self.connect()
        self.ls_thread = threading.Thread(target=self.listen_send)
        self.ls_thread.start()
        
        # listener_thread = threading.Thread(target=self.listener)
        #sender_thread = threading.Thread(target=self.sender)
        #listener_thread.start()
        #sender_thread.start()

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
                break
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
                    break
                except:
                    print(f"[STM/ERROR] Failed to connect on {self.port}")
                    self.kill_flag = True     
                    traceback.print_exc()

    def disconnect(self):
        print(f"[STM/INFO] Setting kill_flag to True")
        self.kill_flag = True
        
        self.ls_thread.join()
        
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
        
    # Combines Listener & Sender thread into one thread
    def listen_send(self) -> "workerThread":
        print("[STM_THREAD/INFO] Starting thread")
        while not self.kill_flag:
            try:
                if not STM_OUT.empty():
                    instr = STM_OUT.get()
                    sub_instr_arr = instr.split(',')
                    print(f"[STM_THREAD/INFO] {sub_instr_arr}")
                    
                    for sub_instr in sub_instr_arr:
                        sub_instr = sub_instr.lstrip().rstrip().encode()
                        print(f"[STM_THREAD/INFO] Sending {sub_instr}")

                        # Send sub instruction to STM
                        self.write(sub_instr)
                        print(f"[STM_THREAD/INFO] Sent {sub_instr}")
                        
                        '''
                        counter=0
                        while True:
                            # Retrieve data from STM
                            self.lock.acquire()
                            rcv_data = self.stm.readline()     # might not need lstrip/rstrip
                            #self.stm.flushOutput()
                            self.lock.release()
                            if rcv_data != b'':
                                print(f"rcv_data {rcv_data}")
                                if rcv_data == b'R\x00\x00\x00L':
                                    break
                            else:
                                counter+=1
                                time.sleep(.1)
                            if counter == 10:
                                #self.reconnect()
                                #print("[STM_THREAD/INFO] Sending 30000")
                                #self.stm.write(b"30000")
                                #self.reconnect()
                                #time.sleep(2)
                                print(f"[STM_THREAD/INFO] Resending {sub_instr}")
                                self.send(sub_instr)
                                print(f"[STM_THREAD/INFO] Resent {sub_instr}")
                                counter=0
                        '''
                        
                        while not self.complete_flag: 
                            # Retrieve data from STM
                            rcv_data = self.readline()
                            self.lock.release()
                            
                            if rcv_data != b'':
                                print(f"[STM_THREAD/INFO] received {rcv_data}")
                                # Done\x00 -> sub instruction completed
                                # ready to send next sub intruction, break out of loop
                                # if rcv_data == b'Done\x00':
                                if rcv_data == b'D\x00\x00\x00D':
                                    ANDROID_OUT.put("Done")
                                    self.complete_flag = True
                        
                        self.complete_flag = False
                    
                    print(f"[STM/INFO] Completed instructions")
                    print(f"[STM/INFO] Putting '{TAKE_PIC}' into STM_IN")
                    STM_IN.put(TAKE_PIC)
            except:
                traceback.print_exc()
        print("[STM_THREAD/INFO] Exiting thread")
        
        
    '''
    def decode_and_send__instr(self, instr) -> None:
        """
        Receives a string of instruction, splits the string into sub instructions
        before sending

        Args:
            instr (str): Strings of instructions - "01020, 02090, ..."
        """
        sub_instr_arr = instr.split(',')
        print(f"[STM/INFO] {sub_instr_arr}")
        
        for sub_instr in sub_instr_arr:
            sub_instr = sub_instr.lstrip().rstrip().encode()
            print(f"[STM/INFO] Sending {sub_instr}")
            time.sleep(0.5)
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
                rcv_data = self.stm.readline()     # might not need lstrip/rstrip
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
                    send_data = STM_OUT.get()
                    #print(f"[STM_SENDER/INFO] Sending to STM: {send_data}")
                    self.decode_and_send__instr(send_data)
                    #self.stm.write(send_data.encode())
                    #self.stm.flush()        # self.stm.flushInput()
            except:
                traceback.print_exc()
        print("[STM_SENDER/INFO] Exiting sender thread")
    '''
    
    def readline(self):
        self.lock.acquire()
        rcv_data = self.stm.readline()
        self.stm.flush()
        self.lock.release()
        return rcv_data
    
    def send(self, data):
        self.lock.acquire()
        self.stm.write(data)
        self.stm.flush()        # self.stm.flushInput()
        self.lock.release()