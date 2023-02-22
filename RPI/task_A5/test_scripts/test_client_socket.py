
import socket


class TestClient:
    def __init__(self):
        # self.d = "d"
        print("HELLO")
        
    def __call__(self):
        print("special")
        self.connect()

        while True:
            try:
                data = input("> ").encode() 
                self.c_sock.sendall(data)
            except KeyboardInterrupt:
                self.c_sock.sendall("exit".encode())
                break
        
        
    def connect(self):
        self.c_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.c_sock.connect(("192.168.15.1", 12345))

class Example:
    def __init__(self):
        print("Instance Created")
      
    # Defining __call__ method
    def __call__(self):
        print("Instance is called via special method")
  
        
if __name__=="__main__":
    tc = TestClient()   
    tc()
    # Instance created
    # e = Example()
    
    # __call__ method will be called
    # e()