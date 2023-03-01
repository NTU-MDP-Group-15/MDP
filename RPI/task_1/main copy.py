"""
Filename: main.py
Version: v1.0 (BACKUP)

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
import re
import modules 
import threading

from modules.helper import STM_IN, ANDROID_IN, ALGO_IN, IMGREC_IN, \
                           STM_OUT, ANDROID_OUT, ALGO_OUT, IMGREC_OUT, \
                           TAKE_PIC, MOVEMENT_DICT

ID_ARRAY = []

class MDPPi:
    def __init__(self):
        self.stm_int = modules.STMInterface()
        self.im_int = modules.ImageRecInterface()
        # self.algo_int = modules.AlgoServerInterface(name="MDP-Pi Algo Server")    # thread
        # self.bt_int = modules.BTServerInterface(name="MDP-Pi BT Server")          # thread
        self.dh = modules.DataHandler(stm_int=self.stm_int, 
                                      im_int=self.im_int,
                                    #   algo_int=self.algo_int,
                                    #   bt_int=self.bt_int
                                      )
        self.lock = self.threading.Lock()
        self.buffered_instr = list
        self.str_instr = str
        self.no_of_instr = 0
        
    def __call__(self):
        self.stm_int()          # Connect stm first
        self.im_int()
        # self.algo_int.start()
        # self.bt_int.start()

        print("[PI/INFO] All devices connected successfully")
        # self.dh()
        # print("[PI/INFO] MainPI RUNNING")
        pi_worker = threading.Thread(target=self.dataHandler)
        pi_worker.start()
    
    def kill_all_proc(self):
        self.stm_int.kill_flag = True
        self.im_int.kill_flag = True
        # self.algo_int.kill_flag = True
        # self.bt_int.kill_flag = True
        
    def dataHandler(self) -> None:        
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
        STM_IN will contain TAKE_PIC = "PIC"
        """
        data = STM_IN.get()
        print(f"[PI/INFO] STM_IN: {data}")
        if data == TAKE_PIC:
            # self.im_int.take_picture()
            # Flag set to true to start sending image
            self.im_int.send_image_flag = True
        # print(f"[PI/INFO] Putting '{data}' into ")
    
    def android_data(self):
        """
        Function used for data handling from Android
        Data from ANDROID_IN will be in (category, data)     
        """
        # data  = ANDROID_IN.get()
        category, data  = ANDROID_IN.get()
        print(f"[PI/INFO] ANDROID_IN: {category}, {data}")
        # print(f"[PI/INFO] Putting '{data}' into STM_OUT")
        # STM_OUT.put(data)

        # requires some way to identify if its obstacle or results
        
        # Step 1: Send to Algo socket
        if category == "obs": 
            ALGO_OUT.put(data)
        
        # Step 3: starts sending first STM instruction
        if category == "cmd":
            # for instr in self.buffered_instr:
            STM_OUT.put(self.buffered_instr[0])
            self.buffered_instr.pop(0)

    def algo_data(self):
        
        # Step 2: Receives shortest path in ARRAY
        data = ALGO_IN.get()
        
        # DECODING/TRANSLATION OF ALGO INSTR 
        # ['F020', 'FR090', 'FL090'] = 01020,03090,02090
        self.buffered_instr = self.algo_to_stm(data)
        self.no_of_instr = len(self.buffered_instr)
    
    def imgrec_data(self):
        """
        IMGREC_IN will always be inferred ID of the picture
        most ocurring ID will be sent to ANDROID_OUT
        """
        global ID_ARRAY
        
        data = IMGREC_IN.get()
        print(f"[PI/INFO] IMGREC_IN: {data}")
        
        self.lock.acquire()
        ID_ARRAY.append(data)
        self.lock.release()
        
        if len(ID_ARRAY) == self.im_int.no_of_pic:
            filtered_list = list(filter(('99').__ne__, ID_ARRAY))
            
            # Completely no detection, retake another 5 frames
            if len(filtered_list) == 0:
                self.im_int.send_image_flag = True      # can add peeking here
                
            # detection present, find the most occuring ID
            else:
                # Flag set to false to stop sending image
                self.im_int.send_image_flag = False
                
                most_occurring_id = int(max(set(filtered_list), key=filtered_list.count))
                print(f"[PI/INFO] ID: {most_occurring_id}")
            
                # Send nexts buffered instr
                if len(self.buffered_instr) != 0:
                    STM_OUT.put(self.buffered_instr[0])
                    self.buffered_instr.pop(0)
                
                ANDROID_OUT.put(most_occurring_id)
            ID_ARRAY = []
    
    def algo_to_stm(self, data) -> str:
        """
        Args:
            data (list): list of individual movements provided by Algorithm Team
                         ['F010', 'FL090']

        Returns:
            str: movement string "01010,02090..."
        """
        str_instr = str
        mvmt_str = "{mvmt_id}{mvmt_dist}"

        pattern = re.compile(r"([a-zA-Z]+)(\d+)")
        match = pattern.match(data[0])
        str_instr = mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                    mvmt_dist=match.group(2))
        for instr in data[1:]:
            match = pattern.match(instr)
            str_instr +=  "," + mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                                mvmt_dist=match.group(2))

        return str_instr
    
if __name__ == '__main__':
    pi = MDPPi()
    pi()
