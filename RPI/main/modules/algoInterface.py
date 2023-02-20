'''
Filename: algoInterface.py
Version: 0.1

Class for setting up connection sockets for algo
! Updates (DDMMYY)

'''
import socket 
import threading

PROTOCOL = socket.SOCK_STREAM   # socket.SOCK_DGRAM
SERVER_IP = "192.168.15.1"
SERVER_PORT = 12345
MAX_CLIENT = 5
SERVER_SOCKET_TIMEOUT = 0.1
GENERAL_TIMEOUT = 1
SERVER_EXIT_FLAG = False
BUFFER_SIZE = 8192

class AlgoServerInterface(threading.Thread):
    def __init__(self, name):
        super().__init__()
        self.name = name
        self.protocol = PROTOCOL
        self.server_ip = SERVER_IP
        self.server_port = SERVER_PORT
        self.max_client = MAX_CLIENT
    
    def run(self):
        print(f"[ALGO_S/INFO] Starting {self.name}")
        self.setup_server_conn()
        print(f"[ALGO_S/INFO] Closing socket...")
        self.s_sock.close()
        
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
                except socket.timeout:
                    pass
                else:
                    self.c_sock.settimeout(GENERAL_TIMEOUT)
                    print(f"[ALGO_S/INFO] Connection from {self.c_addr}")
                    aci = AlgoClientInterface(self.c_sock, self.c_addr)
                    aci.start()
            except KeyboardInterrupt:
                print("[ALGO_S/INFO] Keyboard interrupt received...")
                SERVER_EXIT_FLAG = True
                break

class AlgoClientInterface(threading.Thread):
    def __init__(self, c_sock, c_addr):
        super().__init__()
        self.c_sock = c_sock
        self.c_addr = c_addr
        self.buffer_size = BUFFER_SIZE

    def run(self):
        print(f"[ALGO_C/INFO] Starting {self.c_addr}")
        self.listen_client_socket()
        print(f"[ALGO_C/INFO] Exiting {self.c_addr}")
        self.c_sock.close()

    def listen_client_socket(self):
        while not SERVER_EXIT_FLAG:
            try:
                data = self.c_sock.recv(self.buffer_size).decode()
                if data:
                    if self.proc_data(data): break
            except socket.timeout:
                pass

    # data passed in argument is decoded data
    def proc_data(self, data):
        if data == "exit": return True
        print(data)
        return False
        
if __name__ == "__main__":
    asi = AlgoServerInterface("AlgoServerSocket")
    asi.start()
    
    
'''
ROBOT/4-15/(5, 11, 0)
9MOVEMENTS/4-15/['F', 'F', 'F', 'F', 'FR', 'B', 'B', 'FL']
'''