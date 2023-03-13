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

UUID = "94F39D29-7D6D-437D-973B-FBA39E49D4EE"

class BTServerInterface:
    def __init__(self, name="MDP-Team15"):
        os.system("sudo hciconfig hci0 piscan")
        self.name = name
        #self.s_port = bt.PORT_ANY
        self.uuid = UUID

        self.s_sock = None
        self.c_sock = None
    
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
        except: return False
        else:
            print(f"[BT/INFO] Accepted connection from {self.c_info}")
            self.s_sock.close()
            return True
    
    def disconnect(self) -> None:
        if self.c_sock:        
            self.c_sock.close()
        
        if self.s_sock:
            self.s_sock.close()
            
    def send(self, data) -> None:
        print(f"[BT/INFO] Sending {data}")
        self.c_sock.sendall(data)
        print(f"[BT/INFO] Sent {data}")
        
    def receive(self) -> str:
        while True:
            try:
                data = self.c_sock.recv(1024)
                if data:
                    data = data.decode().rstrip()       # remove any CR or CRLF
                    print(f"[BT/INFO] Received {data}" )
                    break
            except KeyboardInterrupt:
                break
            except:
                pass
        return data
