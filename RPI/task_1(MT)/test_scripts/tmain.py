import time
import queue
import threading

OUTPUT = queue.Queue()

KILL_FLAG = False
def t1_1():
    while not KILL_FLAG:
        print("t1")
        time.sleep(1)

def t2_1():
    while not KILL_FLAG:
        print("t2")
        time.sleep(1)
    
    
threading.Thread(target=t1_1).start()
threading.Thread(target=t2_1).start()


print("OI")
while True:
    try:
        time.sleep(0.5)
    except KeyboardInterrupt:
        print("Setting KILL_FLAG = True")
        KILL_FLAG = True
        break