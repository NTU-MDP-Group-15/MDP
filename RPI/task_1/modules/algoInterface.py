'''
Filename: algoInterface.py
Version: v1.0

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
# SERVER_IP = "192.168.15.1"
MAX_CLIENT = 5
SERVER_SOCKET_TIMEOUT = 0.1
GENERAL_TIMEOUT = 1
SERVER_EXIT_FLAG = False
BUFFER_SIZE = 8192

# class AlgoServerInterface(threading.Thread):
class AlgoServerInterface:
    def __init__(self, name, protocol=PROTOCOL,
                 rpi_ip=SERVER_IP, algo_port= ALGO_PORT,
                 max_client=MAX_CLIENT
                 ):
        # super().__init__()
        self.name = name
        self.protocol = protocol
        self.rpi_ip = rpi_ip
        self.algo_port = algo_port
        self.max_client = max_client
        self.c_sock = None
        self.c_addr = None        
        self.kill_flag = False
    
    # def run(self):
    #     print(f"[ALGO_S/INFO] Starting {self.name}")
    #     self.setup_server_conn()
    #     print(f"[ALGO_S/INFO] Closing socket...")
    #     self.s_sock.close()
    
    def __call__(self):
        print(f"[ALGO/INFO] Starting {self.name}")
        self.connect()
        listener_thread = threading.Thread(target=self.listener)    
        sender_thread = threading.Thread(target=self.sender)
        listener_thread.start()
        sender_thread.start()
        # print(f"[ALGO/INFO] Closing socket...")
        # self.s_sock.close()
    
    def connect(self):
        try:
        
            self.s_sock = socket.socket(socket.AF_INET, self.protocol)
            self.s_sock.bind((self.rpi_ip, self.algo_port))
            self.s_sock.listen(self.max_client)
            # self.s_sock.setblocking(False)
            self.s_sock.settimeout(SERVER_SOCKET_TIMEOUT)
            print(f"[ALGO_S/INFO] Listening on {self.rpi_ip}:{self.algo_port}")
        except:
            self.kill_flag = True
        while True:
            try:
                self.c_sock, self.c_addr = self.s_sock.accept()
            except socket.timeout:
                pass
            except KeyboardInterrupt:
                print("[ALGO_S/INFO] Keyboard interrupt received...")
                break
            else:
                self.c_sock.settimeout(GENERAL_TIMEOUT)
                print(f"[ALGO_S/INFO] Connection from {self.c_addr}")
                break
            
    def listener(self) -> "workerThread":
        disconnect_flag = False
        print("[ALGO_LISTENER/INFO] Starting listener thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                data = self.c_sock.recv(1024)
                if data:
                    data = data.decode().rstrip()       # remove any CR or CRLF
                    print(f"[ALGO_LISTENER/INFO] Received {data}" )
                    ALGO_IN.put(data) 
            except IOError:
                pass
            except KeyboardInterrupt:
                print("[ALGO_LISTENER/INFO] KeyboardInterrupt received")
                break
        print("[ALGO_LISTENER/INFO] Exiting listener thread")
         
    def sender(self) -> "workerThread":
        disconnect_flag = False
        print("[ALGO_SENDER/INFO] Starting sender thread")
        while not disconnect_flag and not self.kill_flag:
            try:
                if not ALGO_OUT.empty():
                    send_data = ALGO_OUT.get()
                    print(f"[ALGO_SENDER/INFO] Sending to BT: {send_data}")
                    self.c_sock.sendall(send_data.encode())
            except:
                traceback.print_exc()
        print("[ALGO_SENDER/INFO] Exiting sender thread")

if __name__ == "__main__":
    # asi = AlgoServerInterface("AlgoServerSocket")
    # asi.start()
    pass