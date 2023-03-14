"""
Filename: main.py
Version: v3.1

Start relevant interfaces/threads for Task 2               

! Updates (DDMMYY)
130323 - Removed algo 


Order of connection STM -> IMGREC -> BT
"""
import modules 

class MDPPi:
    def __init__(self):
        self.stm_int = modules.STMInterface()
        self.bt_int = modules.BTServerInterface()          
        self.im_int = modules.ImageRecInterface(no_of_pic=3)
        self.dh = modules.DataHandler(stm_int=self.stm_int,
                                      bt_int=self.bt_int,
                                      im_int=self.im_int
                                     )
    def __call__(self):
        if self.connect():
            self.im_int.start_thread()
            print("[PI/INFO] All devices connected successfully")
            print("[PI/INFO] MainPI RUNNING")
            self.dh()

        print("[PI/INFO] Exiting MainPi")
        self.clean_close()
            
    def connect(self) -> bool:
        print("[PI/INFO] CONNECTING STM")
        stm_con = self.stm_int.connect()
        if stm_con: print("[PI/INFO] STM CONNECTED")
        else: print("[PI/INFO] STM NOT CONNECTED")
        
        print("[PI/INFO] CONNECTING IMAGE REC")
        im_con = self.im_int.connect()
        if im_con: print("[PI/INFO] IMAGE REC CONNECTED")
        else: print("[PI/INFO] IMAGE REC NOT CONNECTED")
        
        print("[PI/INFO] CONNECTING ANDROID")
        bt_con = self.bt_int.connect()
        if bt_con: print("[PI/INFO] ANDROID CONNECTED") 
        else: print("[PI/INFO] ANDROID NOT CONNECTED")
        return (stm_con and im_con and bt_con)
        #return True
        
    def clean_close(self) -> None:
        self.stm_int.disconnect()
        self.im_int.disconnect()
        self.bt_int.disconnect()
        
if __name__ == '__main__':
    pi = MDPPi()
    pi()
