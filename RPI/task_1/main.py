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
import time
import modules 

class MDPPi:
    def __init__(self):
        self.stm_int = modules.STMInterface()
        self.bt_int = modules.BTServerInterface(name="MDP-Pi BT Server")          # thread
        self.algo_int = modules.AlgoServerInterface(name="MDP-Pi Algo Server")    # thread
        self.im_int = modules.ImageRecInterface()
        self.dh = modules.DataHandler(stm_int=self.stm_int, 
                                      im_int=self.im_int,
                                       algo_int=self.algo_int,
                                       bt_int=self.bt_int
                                      )
        
    def __call__(self):
        self.stm_int()          # Connect stm first
        self.im_int()
        self.algo_int()
        self.bt_int()

        print("[PI/INFO] All devices connected successfully")
        self.dh()
        print("[PI/INFO] MainPI RUNNING")
        while True:
            try:
                time.sleep(0.5)
            except KeyboardInterrupt:
                print("[PI/INFO] Killing all processes")
                self.kill_all_proc()
                break
        print("[PI/INFO] Exiting PI")
    
    def kill_all_proc(self):
        self.stm_int.kill_flag = True
        self.im_int.kill_flag = True
        self.algo_int.kill_flag = True
        self.bt_int.kill_flag = True
        self.dh.kill_flag = True
        
if __name__ == '__main__':
    pi = MDPPi()
    pi()
