
import socket
import threading
import time

def myround(x,base=5):
  print('Entering my round',x)
  return base * round(x/base)

class algoInterface:
    def __init__(self, RPI):
        self.RPI = RPI
        self.clientSocket = socket.socket()
    
    def connectAlgo(self):
        self.clientSocket, self.address = self.RPI.serverSocket.accept()
        print("ALGO Connected on: ", self.address)
        #welcomeMessage = "Welcome to Server (ALGO)"
        #self.write(welcomeMessage)

        #start listen threads)
        listenThread = threading.Thread(target = self.read)
        listenThread.start()

    def write(self,message):
        try:
            message = message + '\n'
            self.clientSocket.send(bytes(message,"utf-8"))
            print("Sent to Algo: ", message)
        except Exception as e:
            print("Algo Disconnected! (ALGO WRITE)")
            self.connectAlgo()



    def read(self):
        while True:
            try:
                message = self.clientSocket.recv(1024)
                message = message.decode()
                if message:
                    print("From ALGO:", message)
                if ('Hello' not in message and 'AND' not in message and 'Update' not in message and 'NOTHING' not in message and 'conda' not in message):
                    #message = message[4:]
                    commands = message.split(',')
                    convertedLetters = []
                    for command in commands:
                        print("commandEnter:",command)
                        if (command == '0000'): continue
                        if (command[2]!='0' and (command[3]=='0' or command[3]=='1')):
                            print('Entering myround attempt')
                            roundedValue = str(myround(int(command[2])))
                            print('roundedVal:',roundedValue)
                            if (roundedValue == '0'):
                                print('zero case detected')
                                pass
                            elif roundedValue == '10':
                                convertedLetters.append(self.convertCommandToLetter('010'+command[3]))
                            else: 
                                convertedLetters.append(self.convertCommandToLetter('005'+command[3]))
                        convertedLetters.append(self.convertCommandToLetter(command))
                    print("Full conversion: ", convertedLetters)
                    i = 0
                    for letter in convertedLetters:
                        # Sending Commands to STM
                        print('Sending to STM:', letter)
                        self.RPI.stm.send(letter)

                        # Sending commands to Android
                        
                        messageToAndroid = "COMMAND FOUR DIGIT," + commands[i]
                        self.RPI.android.write(messageToAndroid)
                        i = i+1
                        

                    #after full list of commands is sent, take a picture
                    self.RPI.imrec.take_picture()

                #Update Android with coordinates
                elif("Update Android" in message):
                    self.RPI.android.write(message)
                    
            except Exception as e:
                print("Algo Disconnected! (ALGO READ)")
                self.connectAlgo()




    def convertCommandToLetter(self,command):
        print('command:',command)
        distance = int(command[0:3])
        direction = int(command[3])
        if (direction == 1 and distance == 5): return chr(ord('j')+11)
        if (direction == 0 and distance == 5): return chr(ord('9')+11)
                                                        
        if (direction == 0): charMap = ord('0')
        elif (direction == 1): charMap = ord('a')
        elif (direction == 2): return 'L'
        elif (direction == 3): return 'R'
        else:
            print('ERROR: Unknown direction mapping')
            return '?'
            
        letter = distance//10 + charMap - 1
        print(command,'->',letter,chr(letter))
            
        return chr(letter)

