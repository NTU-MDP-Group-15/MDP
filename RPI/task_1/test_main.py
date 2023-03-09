"""
Filename: main.py
Version: v1.1

Starts all relevant threads required for MDP

main.py 
    workerThread:
        - thread_proc: contains logic for data processing between
                       different interfaces

imgrecInterface.py 
    workerThread:
        - listener

stmInterface.py
    workerThread:
        - listener
        - sender

btInterface.py 
    workerThread:
        - listener
        - sender                 

! Updates (DDMMYY)
200223 - Added logic for BT -> RPI -> STM
210223 - Removed threading on MDPPi
230223 - Logic for task A5
         Updated logic for sending of pictures
260223 - Updated to taskv1
       - Added logic between ANDROID <-> RPI, RPI <-> ALGO
270223 - Moved thread_proc to dataHandler.py
060323 - Converted STM to process

Order of connection STM -> IMGREC -> ALGO -> BT

How data should be moved:
Step 1: RPI receives from Android the obstacle -> sends to algo laptop, ANDROID_IN -> ALGO_OUT
Step 2: RPI receives from Algo the shortest path -> sends to Android & buffer instructions ALGO_IN -> ANDROID_OUT, STM_OUT
Step 3: RPI waits for Android "Start" before sending buffered instructions to STM, ANDROID_IN -> None 
Step 4: RPI sends instructions to STM
     4a: waits for acknowledgement when sub instruction is done, and send the next sub instruction
     4b: when all sub instructions (aka full instructions done), take photo for imgrec and sends to Android, IMGREC_IN -> ANDROID_OUT
Repeat step 4.

"""
import re
import modules 
import time
import threading

from modules.helper import STM_IN, ANDROID_IN, ALGO_IN, IMGREC_IN, \
                           STM_OUT, ANDROID_OUT, ALGO_OUT, IMGREC_OUT, \
                           TAKE_PIC, MOVEMENT_DICT

class MDPPi:
    def __init__(self):
        self.stm_int = modules.STMInterface()
        self.bt_int = modules.BTServerInterface(name="MDP-Pi BT Server")          # thread
        self.algo_int = modules.AlgoServerInterface(name="MDP-Pi Algo Server")    # thread
        self.im_int = modules.ImageRecInterface()
        self.dh = modules.DataHandler(stm_int=self.stm_int, 
                                      im_int=self.im_int,
                                    #   algo_int=self.algo_int,
                                    #   bt_int=self.bt_int
                                      )
                                      
    def __call__(self):
        self.stm_int()          # Connect stm first
        #self.im_int()
        #self.algo_int()
        #self.bt_int()
        
        print("[PI/INFO] All devices connected successfully")
        print("[PI/INFO] MainPI RUNNING")
        #self.dh()

        input("> ")
        s1 = "[['1', 'B020', 'FR090', 'B010', 'FL090'], ['2', 'B050', 'FR090', 'F020'], ['3', 'B010', 'FR090', 'B020', 'BL090', 'BR090', 'B010', 'FR090'], ['4', 'F010'], ['5', 'B020', 'FL090', 'F030', 'FR090', 'F010', 'BL090', 'F010']]"
        
        #s2 = "[['01', 'F010'], ['02', 'B030', 'FR090', 'F010', 'BL090', 'F010']]"
        
        buffered_instr = list()
        buffer_array = eval(s1)
        for ar in buffer_array:
            STM_OUT.put(self.dh.algo_to_stm(ar))
        
    def kill_all_proc(self):
        self.stm_int.disconnect()
        self.im_int.disconnect()
        self.algo_int.disconnect()
        self.bt_int.disconnect()
        self.dh.kill_thread()
        
if __name__ == '__main__':
    pi = MDPPi()
    pi()
