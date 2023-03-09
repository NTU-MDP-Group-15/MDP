'''
Filename: btInterface.py
Version: v1.2

Class for setting up bluetooth connection sockets
! Updates (DDMMYY)
200223 - Added Queue
         Logic for task A5 (bulleyes target)
270323 - Added connected_flags
060323 - Added disconnect() for easier calling
'''
import os
import traceback
import bluetooth as bt
import threading
from .helper import ANDROID_IN, ANDROID_OUT

UUID = "94F39D29-7D6D-437D-973B-FBA39E49D4EE"

class BTServerInterface:
    def __init__(self, name):
        os.system("sudo hciconfig hci0 piscan")
        self.name = name
        #self.s_port = bt.PORT_ANY
        self.uuid = UUID

        self.s_sock = None
        self.c_sock = None
        
        # Flags to control behaviour
        self.lock = threading.Lock()
        self.kill_flag = False
        
    def __call__(self):
        # print(f"[BT/INFO] Starting thread {self.name}")
        self.connect()
        self.listener_thread = threading.Thread(target=self.listener)    
        self.sender_thread = threading.Thread(target=self.sender)
        self.listener_thread.start()
        self.sender_thread.start()
            
    def connect(self) -> bool:
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
            self.s_sock.close()
            return True
        except: return False
    
    def disconnect(self) -> None:
        print(f"[BT/INFO] Setting kill_flag to True")
        self.kill_flag = True
        
        self.listener_thread.join()
        self.sender_thread.join()
        if self.c_sock:        
            self.c_sock.close()
        
        if self.s_sock:
            self.s_sock.close()
        
    def listener(self) -> "workerThread":
        disconnect_flag = False
        print("[BT_LISTENER/INFO] Starting listener thread")
        while not self.kill_flag:
            try:
                data = self.c_sock.recv(1024)
                if data:
                    data = data.decode().rstrip()       # remove any CR or CRLF
                    print(f"[BT/INFO] Received {data}" )
                    if data == "exit" or data == "bye":
                        break
                    ANDROID_IN.put(data) 
            except IOError:
                pass
        print("[BT_LISTENER/INFO] Exiting listener thread")
         
    def sender(self) -> "workerThread":
        disconnect_flag = False
        print("[BT_SENDER/INFO] Starting sender thread")
        while not self.kill_flag:
            try:
                if not ANDROID_OUT.empty():
                    send_data = ANDROID_OUT.get()
                    print(f"[BT_SENDER/INFO] Sending to BT: {send_data}")
                    self.c_sock.sendall(send_data)
            except:
                traceback.print_exc()
        print("[BT_SENDER/INFO] Exiting sender thread")

    