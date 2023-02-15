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

INPUT, OUTPUT = queue.Queue(), queue.Queue() 
IP_ADDRESS = "192.168.15.1"
ALGO_PORT = 12345
STM_PORT = 12346
BT_PORT = 12347


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