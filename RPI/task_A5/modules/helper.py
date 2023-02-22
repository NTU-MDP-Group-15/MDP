'''
Filename: helper.py
Version: 0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)
070223 - Basic helper functions and global variables for memory sharing between threads
'''
import os
import queue
# import pickle

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(os.path.split(CUR_DIR)[0], "photos")


# STM_OUT will only contain instructions in strings "01050,11030..."
# STM_IN will contain "PIC"
STM_IN, STM_OUT = queue.Queue(), queue.Queue() 
ANDROID_IN, ANDROID_OUT = queue.Queue(), queue.Queue() 
ALGO_IN, ALGO_OUT = queue.Queue(), queue.Queue() 
IMGREC_IN, IMGREC_OUT = queue.Queue(), queue.Queue() 

IP_ADDRESS = "192.168.15.1"
ALGO_PORT = 12345
STM_PORT = 12346
BT_PORT = 12347
IMGREC_PORT = 12348

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