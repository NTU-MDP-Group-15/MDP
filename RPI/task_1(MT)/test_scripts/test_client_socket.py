
import socket

def main():
    c_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    c_sock.connect(("192.168.15.1", 12345))
    
    while True:
        data = c_sock.recv(1024)
        if data:
            print(data)
            
if __name__=="__main__":
    main()