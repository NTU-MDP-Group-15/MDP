import socket
import time
from imutils.video import VideoStream
import imagezmq
import cv2
import imagezmq

cam = cv2.VideoCapture(0)
# rpi_name = socket.gethostname() # send RPi hostname with each image
#picam = VideoStream(usePiCamera=True).start()
#sender = imagezmq.ImageSender(connect_to='tcp://192.168.20.25:5555')
# cam = cv2.VideoCapture(0)
rpi_name = socket.gethostname() # send RPi hostname with each image

cam.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cam.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

time.sleep(2.0)  # allow camera sensor to warm up
i = 0
while True:  # send images as stream until Ctrl-C
     check, frame = cam.read()
     #check, frame = cam.read()
     cv2.imshow(rpi_name,frame)
     #sender.send_image(rpi_name, frame)
     key = cv2.waitKey(1)
     if key == 27:
         name = '/home/pi/Desktop/left_images/left'+ str(i) + '.jpg'
         print(name)
         cv2.imwrite(name, frame)
         i = i+1

# cam.release()
# cv2.destroyAllWindows()