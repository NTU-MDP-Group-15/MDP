import queue
import threading

IMGREC_IN = queue.Queue()



ID_ARRAY = []

class s_test:
    def __init__(self):
        pass
    
    def __call__(self):
        threading.Thread(target=self.thread_proc).start()
    
    def thread_proc(self) -> None:        
        while True:
            try:
                if not IMGREC_IN.empty():
                    self.imgrec_data()
            except KeyboardInterrupt:
                print("[PI/INFO] Keyboard Interrupt received")
                break
            
    def imgrec_data(self):
        global ID_ARRAY
        """
        IMGREC_IN will always be inferred ID of the picture
        most ocurring ID will be sent to ANDROID_OUT
        """
        data = IMGREC_IN.get()
        print(f"[PI/INFO] IMGREC: {data}")
        
        ID_ARRAY.append(data)
        
        if len(ID_ARRAY) == 5:
            ID_ARRAY = list(filter(('99').__ne__, ID_ARRAY))
            most_occurring_id = int(max(set(ID_ARRAY), key=ID_ARRAY.count))
        
            # image is bulleyes
            if most_occurring_id == 0:
                # TASK1|[02090,01015,03090,11035,16090]
                instr = "02090,01015,03090,11035,16090"
                print(instr)
            else:
                print(f"[PI/INFO] ID = {most_occurring_id}")
                print(type(most_occurring_id))
            ID_ARRAY = []

if __name__=="__main__":
    st = s_test()
    st()
    
    while True:
        u_input = input("> ")
        if u_input == "exit":
            break
        
        IMGREC_IN.put(u_input)

