'''
Filename: config.py
Version: v1.2

Class for setting up connection sockets for algo
! Updates (DDMMYY)
070223 - Basic helper functions and global variables for memory sharing between threads
230223 - Added protocol variables
060323 - Converted STM_IN/OUT Queue to Multiprocessor Queue
         BACK TO MULTI-THREADING
150323 - Added livestream ports
         moved directory variables
         
-------------------------------------------------------------------
| Command (5bit) |     Action                                     |
|------------------------------------------------------------------
|     00XXX      | stop Movement                                  |
|     01XXX      | move forward for XXX distance (straight)       |
|     02XXX      | turn left for XXX angle (forward)              |
|     03XXX      | turn right for XXX angle (forward)             |
|     11XXX      | move backward for XXX distance(straight line)  |
|     12XXX      | turn left for XXX angle(backward               |
|     13XXX      | turn right for XXX angle(backward)             |
|     20001      | STM received command flag                      |
|     20002      | STM completed command flag                     |
|     DONE       |                                                |
-------------------------------------------------------------------
'''
import os
import queue

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
MAIN_DIR = os.path.split(CUR_DIR)[0]
IMG_DIR = os.path.join(MAIN_DIR, 'static', "images")
MODEL_PATH = os.path.join(MAIN_DIR, "models", "best.pt")     # ./bestv5.pt .\bestv5.pt
#MODEL_PATH = os.path.join(".", "T2_best_2.pt")     # ./bestv5.pt .\bestv5.pt
YOLO_PATH = os.path.join(MAIN_DIR,"YOLOv5")

# STM_OUT will only contain instructions in strings "01050,11030..."
# STM_IN will contain "PIC"
STM_IN, STM_OUT = queue.Queue(), queue.Queue() 
ANDROID_IN, ANDROID_OUT = queue.Queue(), queue.Queue() 
ALGO_IN, ALGO_OUT = queue.Queue(), queue.Queue() 
IMGREC_IN, IMGREC_OUT = queue.Queue(), queue.Queue() 

OBSTACLE_ID = queue.Queue()

RPI_IP = "192.168.15.1"
IMG_ZMQ_IP = "192.168.15.59"
ID_ZMQ_IP = "192.168.15.1"

ALGO_PORT = 12345
STM_PORT = 12346
BT_PORT = 12347
IMGREC_PORT = 12348
IMG_ZMQ_PORT = 5555
ID_ZMQ_PORT = 5556
LIVE_ZMQ_PORT = 5557

TAKE_PIC = "PIC"
NO_OF_PIC = 6
IMG_FORMAT = "jpg"

MOVEMENT_DICT = {
    "F": "01",
    "FL": "02",
    "FR": "03", 
    "B": "11",
    "BL":  "12",
    "BR": "13",
}

IMAGE_DICT = {
    "0": "Bullseye",
    "11": "One",
    "12": "Two",
    "13": "Three",
    "14": "Four",
    "15": "Five",
    "16": "Six",
    "17": "Seven",
    "18": "Eight",
    "19": "Nine",
    "20": "A",
    "21": "B",
    "22": "C",
    "23": "D",
    "24": "E",
    "25": "F",
    "26": "G",
    "27": "H",
    "28": "S",
    "29": "T",
    "30": "U",
    "31": "V",
    "32": "W",
    "33": "X",
    "34": "Y",
    "35": "Z",
    "36": "Up Arrow",
    "37": "Down Arrow",
    "38": "Right Arrow",
    "39": "Left Arrow",
    "40": "Stop",
    "99": "NA"
}

#def debug(where, *msg):
def debug(where, msg):
    if where == "stm" or where == "modules.stmInterface": 
        info = "[STM/INFO]"
    elif where == "algo" or where == "modules.algoInterface": 
        info = "[ALGO_S/INFO]"
    #elif where == "algo_c": info = "[ALGO_C/INFO]"
    elif where == "bt" or where == "modules.btInterface": 
        info = "[BT/INFO]"
    elif where == "imgrec" or where == "modules.imgrecInterface": 
        info = "[IMGREC/INFO]"
    else:
        info = "[DEBUG]"
    print(f"{info} {msg}")
    
    
if __name__ == "__main__":
    debug("stm", "WAITING FOR CONNECTION")