'''
Filename: logic.py
Version: v0.1

Contains all logic processing for various interface's queue

! Updates (DDMMYY)
240223 - Logic to handle data process
'''

import threading

from .helper import STM_IN, ANDROID_IN, ALGO_IN, IMGREC_IN, \
                    STM_OUT, ANDROID_OUT, ALGO_OUT, IMGREC_OUT, \
                    TAKE_PIC, NO_OF_PIC
                    
class dataHandling(threading.Thread):
    def __init__(self):
        super().__init__()
        self.id_array = list()
    
    def run(self) -> None:
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
                print("[DH/INFO] Keyboard Interrupt received")
                break
    
    def stm_data(self) -> None:
        """
        STM_IN will contain TAKE_PIC = "PIC"
        """
        data = STM_IN.get()
        print(f"[DH/INFO] STM_IN: {data}")
        if data == TAKE_PIC:
            # self.im_int.take_picture()
            # Flag set to true to start sending image
            self.im_int.send_image_flag = True
        # print(f"[DH/INFO] Putting '{data}' into ")
    
    def android_data(self) -> None:
        #to, data = STM_IN.get()
        #if to == "STM": STM_OUT.put(data)
        #if to == "ALGO": ALGO_OUT.put(data)
        #if to == "IMGREC": IMGREC_OUT.put(data)
        data = ANDROID_IN.get()
        print(f"[DH/INFO] ANDROID_IN: {data}")
        print(f"[DH/INFO] Putting '{data}' into STM_OUT")
        STM_OUT.put(data)
        
    def algo_data(self) -> None:
        to, data = ALGO_IN.get()
        if to == "ANDROID": ANDROID_OUT.put(data)
        if to == "STM": STM_OUT.put(data)
        if to == "IMGREC": IMGREC_OUT.put(data)
    
    def imgrec_data(self) -> None:
        """
        IMGREC_IN will always be inferred ID of the picture
        most ocurring ID will be sent to ANDROID_OUT
        """        
        data = IMGREC_IN.get()
        print(f"[DH/INFO] IMGREC_IN: {data}")
        
        self.lock.acquire()
        self.id_array.append(data)
        self.lock.release()
        
        if len(self.id_array) == NO_OF_PIC:
            # Flag set to false to stop sending image
            filtered_list = list(filter(('99').__ne__, self.id_array))
            
            # Completely no detection, retake another 5 frames
            if len(filtered_list) == 0:
                self.im_int.send_image_flag = True
                
            # detection present, find the most occuring ID
            else:
                most_occurring_id = int(max(set(filtered_list), key=filtered_list.count))
                print(f"[DH/INFO] ID: {most_occurring_id}")
                # image is bulleyes
                if most_occurring_id == 0:
                    # TASK1|[02090,01015,03090,11035,16090]
                    # instr = "02090,01015,03090,11035"
                    instr = "20010"
                    STM_OUT.put(instr)
                else:
                    ANDROID_OUT.put(most_occurring_id)
            self.id_array = list()