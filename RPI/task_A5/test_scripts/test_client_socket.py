
import socket

def main():
    c_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    c_sock.connect(("192.168.15.1", 12345))
    
    while True:
        try:
            data = input("> ").encode() 
            c_sock.sendall(data)
        except KeyboardInterrupt:
            c_sock.sendall("exit".encode())
            break
            
if __name__=="__main__":
    main()