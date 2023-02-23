"""
Filename: main.py
Version: v0.2a

Starts all relevant threads required for MDP

main.py 
    workerThread:
        - thread_proc: contains logic for data processing between
                       different interfaces

imgrecInterface.py 
    workerThread:
        - listener
        - sender

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
"""
import test_imgrec 
import threading

from helper import STM_IN, ANDROID_IN, ALGO_IN, IMGREC_IN, \
                    STM_OUT, ANDROID_OUT, ALGO_OUT, IMGREC_OUT, \
                    TAKE_PIC

ID_ARRAY = []

class MDPPi:
    def __init__(self):
        self.im_int = test_imgrec.ImageRecInterface()

    def __call__(self):
        self.im_int()
        # print("[PI/INFO] All devices connected successfully")
        print("[PI/INFO] MainPI RUNNING")
        pi_worker = threading.Thread(target=self.thread_proc)
        pi_worker.start()
    
    def kill_all_proc(self):
        self.im_int.kill_flag = True
        # self.bt_int.kill_flag = True
        # self.algo_int.kill_flag = True
        
    def thread_proc(self) -> None:        
        while True:
            try:
                if not STM_IN.empty():
                    self.stm_data()
                if not ANDROID_IN.empty():
                    self.android_data()
                if not ALGO_IN.empty():
                    self.algo_data()
                if not IMGREC_IN.empty():
                    self.imgrec_data()
            except KeyboardInterrupt:
                print("[PI/INFO] Keyboard Interrupt received")
                self.kill_all_proc()
                break
    
    def stm_data(self):
        """
        STM_IN will contain "PIC"
        """
        data = STM_IN.get()
        print(f"[PI/INFO] STM_IN: {data}")
        if data == TAKE_PIC:
            # Flag set to true to start sending image
            self.im_int.send_image_flag = True
            # IMGREC_OUT.put(TAKE_PIC)
            # self.im_int.stop_vid_flag = False
            # self.im_int.take_picture()
        # print(f"[PI/INFO] Putting '{data}' into ")
    
    def android_data(self):
        #to, data = STM_IN.get()
        #if to == "STM": STM_OUT.put(data)
        #if to == "ALGO": ALGO_OUT.put(data)
        #if to == "IMGREC": IMGREC_OUT.put(data)
        data = ANDROID_IN.get()
        print(f"[PI/INFO] ANDROID_IN: {data}")
        print(f"[PI/INFO] Putting '{data}' into STM_OUT")
        STM_OUT.put(data)
        
    def algo_data(self):
        to, data = ALGO_IN.get()
        if to == "ANDROID": ANDROID_OUT.put(data)
        if to == "STM": STM_OUT.put(data)
        if to == "IMGREC": IMGREC_OUT.put(data)
    
    def imgrec_data(self):
        """
        IMGREC_IN will always be inferred ID of the picture
        most ocurring ID will be sent to ANDROID_OUT
        """
        global ID_ARRAY
        
        data = IMGREC_IN.get()
        print(f"[PI/INFO] IMGREC_IN: {data}")
        
        ID_ARRAY.append(data)
        
        if len(ID_ARRAY) == self.im_int.no_of_pic:

            # Flag set to false to stop sending image
            self.im_int.send_image_flag = False
            
            ID_ARRAY = list(filter(('99').__ne__, ID_ARRAY))
            most_occurring_id = int(max(set(ID_ARRAY), key=ID_ARRAY.count))
                    
            print(f"[PI/INFO] ID = {most_occurring_id}")
            # image is bulleyes
            if most_occurring_id == 0:
                # TASK1|[02090,01015,03090,11035,16090]
                instr = "02090,01015,03090,11035"
                print(instr)
                # STM_OUT.put(instr)
            else:
                ANDROID_OUT.put(most_occurring_id)
                pass
            
            ID_ARRAY = []
            
if __name__ == '__main__':
    pi = MDPPi()
    pi()

    while True:
        u_input = input("> ")
        print("putting PIC into STM_IN")
        STM_IN.put(TAKE_PIC)
