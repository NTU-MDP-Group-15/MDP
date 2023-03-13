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

        self.buffered_instr = list()
        self.str_instr = str()
        self.obstacle_id_order = list()
        self.no_of_instr = 0
        
    def __call__(self):
        self.dataHandler()
        
    def dataHandler(self) -> None:
        # Step 1: Receive obstacle from ANDROID
        and_data = self.bt_int.receive()
        
        # Step 2: Send to Algo
        self.algo_int.send(and_data)
        
        # Step 3: Receive from Algo                
        stm_data, and_data = algo_data.split('AND/')
        stm_data = stm_data.split("STM/")[-1]
        
        # Step 3.1: Buffer STM instructions
        buffer_array = eval(stm_data)
        
        for ar in buffer_array:
            self.buffered_instr.append(self.algo_to_stm(ar))
        # self.no_of_instr = len(self.buffered_instr)
        print("[DH/INFO] Buffered array")
        
        # Step 3.2: Send coordinates to android
        self.bt_int.send(and_data)

        # Step 4: Wait for android "START"
        print("[DH/INFO] Ready & Waiting for start...")
        and_data = self.bt_int.receive()
        print("[DH/INFO] SENDING 30000")
        self.stm_int.write(b"30000")
        time.sleep(0.5)
        
        # Send instructions Steps 5-7
        try:
            while len(self.buffered_instr) > 0:
                # Step 5: Send instruction to STM based on obstacle
                instr = self.buffered_instr.pop(0)
                sub_instr_arr = instr.split(',')
                print(f"SUB_INSTR_ARR: {sub_instr_arr}")
                
                for sub_instr in sub_instr_arr:
                    sub_instr = sub_instr.lstrip().rstrip().encode()
                    self.stm_int.write(sub_instr)
                    
                    # Step 5.1: receive acknowledgement
                    while True: 

                        rcv_data = self.stm_int.readline()
                        if rcv_data != b'':
                            print(f"[DH_STM/INFO] received {rcv_data}")

                            # Done\x00 -> sub instruction completed
                            # ready to send next sub intruction, break out of loop
                            # Step 5.2: Send done to android
                            if rcv_data == b'D':
                                self.bt_int.send("[C11] MOV")
                                break
                
                print(f"[DH_STM/INFO] Completed instructions")
                # Step 6: Take pictures and send to ImageRec Server
                #self.im_int.take_send_picture()
                self.im_int.send_image_flag = True
                '''
                while self.im_int.send_image_flag == True: pass
                self.stm_int.write(b"11005")
                self.im_int.send_image_flag = True
                while self.im_int.send_image_flag == True: pass
                self.stm_int.write(b"01005")
                '''
                id = int(self.im_int.receive())
                obs_id = self.obstacle_id_order.pop(0)
            
                # Step 7: Send ID info to Android
                self.bt_int.send(f"[C9] {obs_id} {id}")
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
