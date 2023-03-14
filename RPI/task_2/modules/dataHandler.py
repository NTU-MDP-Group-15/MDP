"""
Filename: dataHandler.py
Version: v1.4

! Updates (DDMMYY)
270223 - Logic moved from main.py to dataHandler.py
280223 - Update logic for communication between BT to ALGO
010323 - Removed self.id_array, logic to get most occuring is done by
         server.
         RPI just sends frames to imgrec server without waiting for return ID
         self.pattern -> PATTERN
060323 - Added kill_thread() for easier calling
         Added logic for sending to ANDROID(coordinates) or STM(obstacle)
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
            # and_data = self.bt_int.receive()
            and_data = input("[START/exit]> ") 
            if and_data != "START":
                break
            
            print("[DH/INFO] SENDING 30000")
            self.stm_int.write(b"30000")
            time.sleep(0.5)
            
            self.stm_int.write(b"04000")     # move code
            
            id = ""
            obstacle_count = 0
            
            try:
                while id!="0":
                    # Step X: Reached first obstacle to check left/right
                    while True: 
                        rcv_data = self.stm_int.readline()
                        if rcv_data != b'':
                            print(f"[DH_STM/INFO] received {rcv_data}")
                            # Done\x00 -> sub instruction completed
                            # ready to send next sub intruction, break out of loop
                            # Step X: Send done to android
                            if rcv_data == b'D':
                                obstacle_count+=1
                                break
                    
                    # Step X: Take pictures and send to ImageRec Server
                    back_counter = 0
                    while True:
                        self.im_int.send_image_flag = True
                        id = int(self.im_int.receive())
                        
                        if obstacle_count == 1:
                            # Move RIGHT
                            if id == "38":
                                self.stm_int.write(b"06000") #
                                break
                            # Move LEFT
                            elif id == "39":
                                self.stm_int.write(b"05000") #
                                break
                            
                        elif obstacle_count == 2:
                            # Move RIGHT
                            if id == "38":
                                self.stm_int.write(b"08000") #
                                break
                            # Move LEFT
                            elif id == "39":
                                self.stm_int.write(b"07000") #
                                break
                            
                        # unable to determine left/right, move back and rescan                
                        else:
                            if back_counter == 3:
                                break
                            self.stm_int.write(b"11005")
                            back_counter+=1
                        
                    if back_counter>0:
                        dist = back_counter * 5
                        self.stm_int.write(f"01{dist:03}".encode(0))
                                
            except KeyboardInterrupt:
                return
                
    def algo_to_stm(self, ar) -> str:
        """
        Args:
            data (list): list of individual movements provided by Algorithm Team
                         ['1', 'F010', 'FL090']
                         ['2', 'B010', 'BR090']

        Returns:
            str: movement string "01010,02090..."
        """

        mvmt_str = "{mvmt_id}{mvmt_dist}"
        self.obstacle_id_order.append(ar.pop(0))
        # OBSTACLE_ID.put(ar.pop(0))
        
        match = PATTERN.match(ar[0])
        str_instr = mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                    mvmt_dist=match.group(2))
        for instr in ar[1:]:
            match = PATTERN.match(instr)
            str_instr +=  "," + mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                                mvmt_dist=match.group(2))
        return str_instr
