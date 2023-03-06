import time

KILL_FLAG = False
def t2_1(OUTPUT):
    while not OUTPUT.empty():
        print(OUTPUT.get())
    while not KILL_FLAG:
        print("t2")
        time.sleep(1)