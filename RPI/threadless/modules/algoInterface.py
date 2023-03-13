'''
Filename: algoInterface.py
Version: v1.2

Class for setting up connection sockets for algo
! Updates (DDMMYY)
270223 - Removed threading
         Added listener & sender threads
         

'''
import socket 
from .config import RPI_IP, ALGO_PORT

PROTOCOL = socket.SOCK_STREAM   # socket.SOCK_DGRAM
MAX_CLIENT = 5
SERVER_SOCKET_TIMEOUT = 0.1
GENERAL_TIMEOUT = 0
SERVER_EXIT_FLAG = False
BUFFER_SIZE = 8192

class AlgoServerInterface:
    def __init__(self, rpi=None, protocol=PROTOCOL,
                 rpi_ip=RPI_IP, algo_port= ALGO_PORT,
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
    
    def connect(self) -> bool:
        try:
            self.s_sock = socket.socket(socket.AF_INET, self.protocol)
            self.s_sock.bind((self.rpi_ip, self.algo_port))
            self.s_sock.listen(self.max_client)
            
            print(f"[ALGO/INFO] Listening on {self.rpi_ip}:{self.algo_port}")
        except Exception as e:
            print(e)
        else:
            while True:
                try:
                    self.c_sock, self.c_addr = self.s_sock.accept()
                except socket.timeout:
                    pass
                except KeyboardInterrupt:
                    print("[ALGO/INFO] Keyboard interrupt received...")
                    return False
                else:
                    print(f"[ALGO/INFO] Connection from {self.c_addr}")
                    self.s_sock.close()
                    return True
        return False
            
    def disconnect(self):
        if self.c_sock:
            self.c_sock.close()
        if self.s_sock:
            self.s_sock.close()
            
    def send(self, data) -> None:
        print(f"[ALGO/INFO] Sending {data}")
        self.c_sock.sendall(data.encode())
        print(f"[ALGO/INFO] Sent {data}")
    
    def receive(self) -> str:
        while True:
            try:
                data = self.c_sock.recv(1024)
                if data:
                    data = data.decode().rstrip()       # remove any CR or CRLF
                    print(f"[ALGO/INFO] Received {data}" )
                    break
            # except IOError as ioe:
            #     #print(ioe)
            #     pass
            except KeyboardInterrupt:
                break
            except:
                pass
        return data
