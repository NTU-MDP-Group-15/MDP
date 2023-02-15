'''
Filename: btInterface.py
Version: 0.2

Class for setting up bluetooth connection sockets
! Updates (DDMMYY)

'''
import os
import bluetooth as bt
import threading

CLIENT_EXIT_FLAG = True
CLIENT_SOCKET_TIMEOUT = 0.1
LOCK = threading.Lock()
UUID = "94F39D29-7D6D-437D-973B-FBA39E49D4EE"

class BTServerInterface(threading.Thread):
    def __init__(self, name):
        super().__init__()
        os.system("sudo hciconfig hci0 piscan")
        
        self.name = name
        #self.s_port = bt.PORT_ANY
        self.uuid = UUID
        # self.CLIENT_EXIT_FLAG = CLIENT_EXIT_FLAG
        self.CONNECTED = False
        
    def run(self):
        print(f"[BT/INFO] Starting thread {self.name}")
        connected = self.setup_connection()
        if connected: self.listen()
        print("[BT/INFO] Disconnected")
        self.c_sock.close()
        self.s_sock.close()
        print("[BT/INFO] Closing thread")
        exit(1)
        
    def setup_connection(self):
        """
            Function used to advertise device for other nodes to connect
        """
        self.s_sock = bt.BluetoothSocket(bt.RFCOMM)
        self.s_sock.bind(("", bt.PORT_ANY))
        self.s_sock.listen(1)
        self.s_channel = self.s_sock.getsockname()[1]
        bt.advertise_service(self.s_sock, self.name, service_id=self.uuid,
                             service_classes=[self.uuid, bt.SERIAL_PORT_CLASS],
                             profiles=[bt.SERIAL_PORT_PROFILE])
        print(f"[BT/INFO] Waiting for connection on RFCOMM channel {self.s_channel}")
        try:
            self.c_sock, self.c_info = self.s_sock.accept()
            print(f"[BT/INFO] Accepted connection from {self.c_info}")
            return True
        except: return False
        
    def listen(self):
        while True:
            try:
                raw_data = self.c_sock.recv(1024)
                if raw_data:
                    data = raw_data.decode().rstrip()
                    print(f"[BT/INFO] Received {data}" )
                    #self.c_sock.send("Pi: received\n")
                    #print("[BT/INFO] sending receive")
                    if data == "exit" or data == "bye":
                        break
                    
                    '''
                    if data == "photo":
                        print("[INFO] Taking photo in progress")
                        lp2.capture_still_image()
                        print("[INFO] Done photo taking")
                        self.c_sock.send(b"PI: done")
                    '''
            except IOError:
                pass
            except KeyboardInterrupt:
                print("[BT/INFO] KeyboardInterrupt received")
                break
    
    def send(self):
        self.c_sock.send("Pi: received\n")
        print("[BT/INFO] sending receive")
    
class BTClientThread(threading.Thread):
    def __init__(self, name):
        super().__init__()
        self.name = name
        
    def run(self):
        print(self.name)
    
    def thread_proc(self):
        pass

