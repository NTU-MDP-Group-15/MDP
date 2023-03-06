"""
Filename: dataHandler.py
Version: v1.3

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
import threading
 
from .helper import STM_IN, ANDROID_IN, ALGO_IN, IMGREC_IN, \
                           STM_OUT, ANDROID_OUT, ALGO_OUT, \
                           TAKE_PIC, MOVEMENT_DICT


PATTERN = re.compile(r"([a-zA-Z]+)(\d+)")

class DataHandler:
    def __init__(self, stm_int = None, im_int = None, algo_int = None, bt_int = None):
        self.stm_int = stm_int
        self.im_int = im_int
        self.algo_int = algo_int
        self.bt_int = bt_int

        self.buffered_instr = list()
        self.str_instr = str()
        self.obstacle_id_order = list()
        self.no_of_instr = 0
        
        # Flags to control behaviour
        self.lock = threading.Lock()
        self.kill_flag = False
        
    def __call__(self):
        self.datahandler_thread = threading.Thread(target=self.dataHandler)
        self.datahandler_thread.start()
        
    def kill_thread(self):
        print("[DH/INFO] Setting kill_flag to True")
        self.kill_flag = True
        
        self.datahandler_thread.join()
        
    def dataHandler(self) -> "workerThread":
        print("[DH_THREAD/INFO] Starting DataHandler thread")
        while not self.kill_flag:
            try:
                if not STM_IN.empty():
                    self.stm_data()
                if not ANDROID_IN.empty():
                    self.android_data()
                if not ALGO_IN.empty():
                    self.algo_data()
                if not IMGREC_IN.empty():
                    self.imgrec_data()
            except Exception as e:
                print(e)
        print("[DH_THREAD/INFO] Exiting DataHandler thread")
        
    def stm_data(self) -> None:
        """
        STM_IN will contain TAKE_PIC = "PIC"
        """
        data = STM_IN.get()
        print(f"[DH_STM/INFO] STM_IN: {data}")
        if data == TAKE_PIC:
            # Flag set to true to start sending image
            self.im_int.send_image_flag = True

        # print(f"[DH/INFO] Putting '{data}' into ")
    
    def android_data(self) -> None:
        """
        Function used for data handling from Android
        Data from ANDROID_IN will be in (category, data)     
        """
        data  = ANDROID_IN.get()
        print(f"[DH_AND/INFO] ANDROID_IN: {data}")

        # requires some way to identify if its obstacle or results
        # Step 1: Send to Algo socket
        if data != "START": 
            ALGO_OUT.put(data)
        
        # Step 3: starts sending first STM instruction
        else:
            # for instr in self.buffered_instr:
            STM_OUT.put(self.buffered_instr[0])
            self.buffered_instr.pop(0)

    def algo_data(self) -> None:
        # Step 2: Receives shortest path in ARRAY
        data = ALGO_IN.get()
        
        # DECODING/TRANSLATION OF ALGO INSTR 
        # ['F020', 'FR090', 'FL090'] = 01020,03090,02090
        # STM/[['01', 'FR090', 'FL090'], ['03', 'B020', 'FR090', 'F040'], 
        #      ['02', 'FL090', 'B030', 'FR090', 'F010', 'FL180'], 
        #      ['05', 'FR090', 'B010'], 
        #      ['04', 'F020', 'BL090', 'B050', 'BL090', 'BR090']]
        # AND/[C10] 1,1,0,3,3,-90,5,5,0,5,4,0,5,3,0,7,5,...
        # to = STM, data = "[[...], [...]]"
        to, data = data.split('/')

        if to == "AND":
            ANDROID_OUT.put(data)
        elif to == "STM":
            buffer_array = eval(data)
        
            for ar in buffer_array:
                self.buffered_instr.append(self.algo_to_stm(ar))
            self.no_of_instr = len(self.buffered_instr)
    
    def imgrec_data(self) -> None:
        """
        IMGREC_IN will always be inferred ID of the picture
        ID will be sent to ANDROID_OUT
        """        
        data = IMGREC_IN.get()
        print(f"[DH_IMGREC/INFO] IMGREC_IN: {data}")
        obs_id = self.obstacle_id_order.pop(0)
        print(f"[DH_IMGREC/INFO] Putting into ANDROID_OUT: [C9] {obs_id} {data}")        
        ANDROID_OUT.put(f"[C9] {obs_id} {data}")
        
        # Send nexts buffered instr
        if len(self.buffered_instr) != 0:
            STM_OUT.put(self.buffered_instr[0])
            self.buffered_instr.pop(0)
            
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
        match = PATTERN.match(ar[0])
        str_instr = mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                    mvmt_dist=match.group(2))
        for instr in ar[1:]:
            match = PATTERN.match(instr)
            str_instr +=  "," + mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                                mvmt_dist=match.group(2))

        return str_instr
    
    
    '''
    # old
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
        
        if len(self.id_array) == self.im_int.no_of_pic:
            filtered_list = list(filter(('99').__ne__, self.id_array))
            
            # Completely no detection, retake another 5 frames
            if len(filtered_list) == 0:
                self.im_int.send_image_flag = True      # can add peeking here
                
            # detection present, find the most occuring ID
            else:
                # Flag set to false to stop sending image
                self.im_int.send_image_flag = False
                
                most_occurring_id = int(max(set(filtered_list), key=filtered_list.count))
                print(f"[DH/INFO] ID: {most_occurring_id}")
            
                # Send nexts buffered instr
                if len(self.buffered_instr) != 0:
                    STM_OUT.put(self.buffered_instr[0])
                    self.buffered_instr.pop(0)
                
                ANDROID_OUT.put(f"[C9] 1 {most_occurring_id}")
            self.id_array = list()
    '''

    