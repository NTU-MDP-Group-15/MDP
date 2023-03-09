'''
Filename: algoInterface.py
Version: v1.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)
270223 - Removed threading
         Added listener & sender threads
         

'''
import socket 
import threading
import traceback
from .helper import ALGO_IN, ALGO_OUT, SERVER_IP, ALGO_PORT

PROTOCOL = socket.SOCK_STREAM   # socket.SOCK_DGRAM
MAX_CLIENT = 5
SERVER_SOCKET_TIMEOUT = 0.1
GENERAL_TIMEOUT = 0
SERVER_EXIT_FLAG = False
BUFFER_SIZE = 8192

# class AlgoServerInterface(threading.Thread):
class AlgoServerInterface:
    def __init__(self, rpi=None, protocol=PROTOCOL,
                 rpi_ip=SERVER_IP, algo_port= ALGO_PORT,
                 max_client=MAX_CLIENT
                 ):
        # super().__init__()
        self.rpi = rpi
        self.protocol = protocol
        self.rpi_ip = rpi_ip
        self.algo_port = algo_port
        self.max_client = max_client
        self.s_sock = None
        self.c_sock = None
        self.c_addr = None        

        # Flags to control behaviour
        self.lock = threading.Lock()
        self.kill_flag = False
        
    def run(self):
        self.connect()


    def __call__(self):
        print(f"[ALGO/INFO] Starting {self.name}")
        self.connect()
        self.listener_thread = threading.Thread(target=self.listener)    
        self.sender_thread = threading.Thread(target=self.sender)
        self.listener_thread.start()
        self.sender_thread.start()
        # print(f"[ALGO/INFO] Closing socket...")
        # self.s_sock.close()
    
    def connect(self):
        try:
            self.s_sock = socket.socket(socket.AF_INET, self.protocol)
            self.s_sock.bind((self.rpi_ip, self.algo_port))
            self.s_sock.listen(self.max_client)
            
            print(f"[ALGO/INFO] Listening on {self.rpi_ip}:{self.algo_port}")
        except Exception as e:
            print(e)
        
        while True:
            try:
                self.c_sock, self.c_addr = self.s_sock.accept()
            except socket.timeout:
                pass
            except KeyboardInterrupt:
                print("[ALGO/INFO] Keyboard interrupt received...")
                break
            else:
                print(f"[ALGO/INFO] Connection from {self.c_addr}")
                break
            
    def disconnect(self):
        print(f"[ALGO/INFO] Setting kill_flag to True")
        self.kill_flag = True
        
        self.listener_thread.join()
        self.sender_thread.join()
        
        if self.c_sock:
            self.c_sock.close()
        if self.s_sock:
            self.s_sock.close()
        
    def listener(self) -> "workerThread":
        disconnect_flag = False
        print("[ALGO_LISTENER/INFO] Starting listener thread")
        while not self.kill_flag:
            try:
                data = self.c_sock.recv(1024)
                if data:
                    data = data.decode().rstrip()       # remove any CR or CRLF
                    print(f"[ALGO_LISTENER/INFO] Received {data}" )
                    ALGO_IN.put(data) 
            except IOError as ioe:
                #print(ioe)
                pass
        print("[ALGO_LISTENER/INFO] Exiting listener thread")
         
    def sender(self) -> "workerThread":
        disconnect_flag = False
        print("[ALGO_SENDER/INFO] Starting sender thread")
        while not self.kill_flag:
            try:
                if not ALGO_OUT.empty():
                    send_data = ALGO_OUT.get()
                    print(f"[ALGO_SENDER/INFO] Sending to ALGO: {send_data}")
                    self.c_sock.sendall(send_data.encode())
            except:
                traceback.print_exc()
        print("[ALGO_SENDER/INFO] Exiting sender thread")