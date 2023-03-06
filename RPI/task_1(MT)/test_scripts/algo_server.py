import socket


def main():

    str1 = b'START/EXPLORE/(R,1,1,0)/(05,11,09,0)/(06,11,05,0)/(01,08,12,-90)/(04,16,15,-90)/(02,14,13,180)/(03,14,09,90)/(07,08,08,0)/(08,14,07,-90)'
    s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s_sock.bind(("192.168.15.69",12345))
    s_sock.listen(1)
    
    while True:
        try:
            print("WAITING FOR CONNECTION")
            c_sock, c_addr = s_sock.accept()
        
        except socket.timeout:
            pass
        except KeyboardInterrupt:
            break
        else:
            break
    
    
    while True:
        u_input = input("> ")
        c_sock.sendall(str1)
         
main()
