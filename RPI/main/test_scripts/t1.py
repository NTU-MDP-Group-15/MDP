import time

def t1_1(OUTPUT):
    for idx in range(5):
        OUTPUT.put(f"t1{idx}")
        
    while 1:
        print("t1")
        time.sleep(1)
