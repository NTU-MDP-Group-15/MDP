'''
Filename: algoInterface.py
Version: 0.1

TESTING

Class for setting up connection sockets for algo
! Updates (DDMMYY)

'''
import socket 
import threading

PROTOCOL = socket.SOCK_STREAM   # socket.SOCK_DGRAM
SERVER_IP = "10.91.234.84"
SERVER_PORT = 12345
MAX_CLIENT = 5
SERVER_SOCKET_TIMEOUT = 0.1
GENERAL_TIMEOUT = 1
SERVER_EXIT_FLAG = False
# BUFFER_SIZE = 8192
BUFFER_SIZE = 16384


# class AlgoServerInterface(threading.Thread):
class AlgoServerInterface:
    def __init__(self, name="ALGO_S"):
        # super().__init__()
        self.name = name
        self.protocol = PROTOCOL
        self.server_ip = SERVER_IP
        self.server_port = SERVER_PORT
        self.max_client = MAX_CLIENT
        self.buffer_size = BUFFER_SIZE
    
    # def run(self):
    #     print(f"[ALGO_S/INFO] Starting {self.name}")
    #     self.setup_server_conn()
    #     print(f"[ALGO_S/INFO] Closing socket...")
    #     self.s_sock.close()
        
    def __call__(self) -> any:
        print(f"[ALGO_S/INFO] Starting {self.name}")
        self.setup_server_conn()
        listener_thread = threading.Thread(target=self.listener)
        sender_thread = threading.Thread(target=self.sender)
        listener_thread.start()
        sender_thread.start()
        print(f"[ALGO_S/INFO] Closing socket...")
        # self.s_sock.close()    
        
    def setup_server_conn(self):
        self.s_sock = socket.socket(socket.AF_INET, self.protocol)
        self.s_sock.bind((self.server_ip, self.server_port))
        self.s_sock.listen(self.max_client)
        # self.s_sock.setblocking(False)
        self.s_sock.settimeout(SERVER_SOCKET_TIMEOUT)
        print(f"[ALGO_S/INFO] Listening on {self.server_ip}:{self.server_port}")
        while True:
            try:
                try:
                    self.c_sock, self.c_addr = self.s_sock.accept()
                    print(f"[ALGO_S/INFO] Connection from {self.c_addr}")
                    break
                except socket.timeout:
                    pass
            except KeyboardInterrupt:
                print("[ALGO_S/INFO] Keyboard interrupt received...")
                SERVER_EXIT_FLAG = True
                exit(-1)
            
    def listener(self):
        print("STARTING")
        while True:
            data = self.c_sock.recv(self.buffer_size).decode()
            print(f"[ALGO_Listener/INFO]{data}")
            

    def sender(self):
        while True:
            u_input = input("> ")
            self.c_sock.sendall(u_input.encode())
            
        
if __name__ == "__main__":
    asi = AlgoServerInterface("AlgoServerSocket")
    asi()
    
    
'''
ROBOT/4-15/(5, 11, 0)
9MOVEMENTS/4-15/['F', 'F', 'F', 'F', 'FR', 'B', 'B', 'FL']
'''