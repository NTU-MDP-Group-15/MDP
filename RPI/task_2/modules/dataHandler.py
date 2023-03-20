"""
Filename: dataHandler.py
Version: v2.1

! Updates (DDMMYY)
150323 - Refactored logic for Task 2
         
"""
import re
import time

from .config import MOVEMENT_DICT, OBSTACLE_ID

PATTERN = re.compile(r"([a-zA-Z]+)(\d+)")

class DataHandler:
    def __init__(self, stm_int = None, im_int = None, bt_int = None):
        self.stm_int = stm_int
        self.im_int = im_int
        self.bt_int = bt_int

    def __call__(self):
        self.dataHandler()
        
    def dataHandler(self) -> None:        
        while True:
            # Step 1: Wait for android "START"
            print("[DH/INFO] Ready & Waiting for start...")
            and_data = self.bt_int.receive()
            try:
                if and_data != "START":
                    break
            except KeyboardInterrupt:
                break
            a_start_time = time.time()

            # Sending a reset case
            print("[DH/INFO] SENDING 30000")
            self.stm_int.write(b"30000")
            time.sleep(0.5)
            
            id = ""
            stage = 0
            
            self.send_command("04000")
            try:
                while stage<2:
                    # Step X: Take pictures and send to ImageRec Server
                    back_counter = 0
                    # cmd = str()
                    
                    while True:
                        self.im_int.send_image_flag = True
                        id = self.im_int.receive()
                        
                        cmd = self.proc_id(stage, id)
                        
                        if cmd != "": break
                        """
                        else:
                            self.send_command("11005")
                            back_counter+=1
                        """
                    """
                    if back_counter>0:
                        print(f"GO BACK back_counter: {back_counter}")
                        dist = back_counter * 5
                        self.send_command(f"01{dist:03}")
                    """
                    self.send_command(cmd)
                    stage += 1
                    self.send_command("04000")

                    if stage==2:
                        self.send_command("04000")
                        self.send_command("09000")
                        self.send_command("10000")
                        
            except KeyboardInterrupt:
                return
            
            print("total runtime: ", time.time() - a_start_time)
            
    def send_command(self, cmd) -> bool:
        '''
        Write & receive data through STM
        '''
        self.stm_int.write(cmd.encode())
        
        while True:
            rcv_data = self.stm_int.readline()
            if rcv_data != b'':
                print(f"[DH_STM/INFO] received {rcv_data}")
                # Done\x00 -> sub instruction completed
                # ready to send next sub intruction, break out of loop
                if rcv_data == b'D':
                    return True
    
    def proc_id(self, stage, id) -> str:
        cmd = str()
        if stage == 0:
            # MOVE RIGHT
            if id == "38":
                cmd = "06000"
            # MOVE LEFT
            if id == "39":
                cmd = "05000"
        if stage == 1:
            # MOVE RIGHT
            if id == "38":
                cmd = "08000"
            # Move LEFT
            if id == "39":
                cmd = "07000"
        return cmd