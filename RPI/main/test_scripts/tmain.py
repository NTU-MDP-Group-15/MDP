import queue
import threading
from t1 import t1_1
from t2 import t2_1

OUTPUT = queue.Queue()
    
    
threading.Thread(target=t1_1, args=(OUTPUT,)).start()
threading.Thread(target=t2_1, args=(OUTPUT,)).start()


