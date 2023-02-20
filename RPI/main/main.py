"""
Filename: main.py
Version: 0.1

Starts all relevant threads required
! Updates (DDMMYY)
"""
import modules 
import threading

from modules.helper import STM_IN, ANDROID_IN, ALGO_IN, IMGREC_IN, \
                           STM_OUT, ANDROID_OUT, ALGO_OUT, IMGREC_OUT

class MDPPi(threading.Thread):
    def __init__(self):
        super().__init__()
        self.im_int = modules.ImageRecInterface()
        self.stm_int = modules.STMInterface()
        self.bt_int = modules.BTServerInterface(name="MDP-Pi BT Server")          # thread
        #self.algoint = modules.AlgoServerInterface(name="MDP-Pi Algo Server")    # thread

    def run(self):
        self.stm_int.start()
        self.im_int.start()

        print("[PI/INFO] All devices connected successfully")
        pi_worker = threading.Thread(target=self.thread_proc)
        pi_worker.start()
        
    def thread_proc(self):        
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
                self.stm_int.disconnected_flag = True
                # self.im_int.disconnected_flag = True
                break
    
    def stm_data(self):
        # data retrieved from queue is assumed to be in (TO, DATA)
        to, data = STM_IN.get()
        if to == "ANDROID": ANDROID_OUT.put(data)
        if to == "ALGO": ALGO_OUT.put(data)
        if to == "IMGREC": IMGREC_OUT.put(data)
    
    def android_data(self):
        to, data = STM_IN.get()
        if to == "STM": STM_OUT.put(data)
        if to == "ALGO": ALGO_OUT.put(data)
        if to == "IMGREC": IMGREC_OUT.put(data)
    
    def algo_data(self):
        to, data = STM_IN.get()
        if to == "ANDROID": ANDROID_OUT.put(data)
        if to == "STM": STM_OUT.put(data)
        if to == "IMGREC": IMGREC_OUT.put(data)
    
    def imgrec_data(self):
        to, data = STM_IN.get()
        if to == "ANDROID": ANDROID_OUT.put(data)
        if to == "ALGO": ALGO_OUT.put(data)
        if to == "STM": STM_OUT.put(data)
        
def test():    
    #args = arg_parser(test_arg="2")
    #max_client = args.max_client
    im_int = modules.ImageRecInterface()
    stm_int = modules.STMInterface()    
    #btint = modules.BTServerInterface("MDP-Pi BT Server")
    #algoint = modules.AlgoServerInterface("MDP-Pi Algo Server")    # thread
    #algoint.start()
    #btint.start()
    im_int.start()
    stm_int.start()
    
if __name__ == '__main__':
    pi = MDPPi()
    pi.start()
