import time

def t2_1(OUTPUT):
    while not OUTPUT.empty():
        print(OUTPUT.get())
    while 1:
        print("t2")
        time.sleep(1)