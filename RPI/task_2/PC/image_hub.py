import cv2
import imagezmq
image_hub = imagezmq.ImageHub(open_port="tcp://192.168.15.69:5555")

while True:  # show streamed images until Ctrl-C
    rpi_name, image = image_hub.recv_image()
    cv2.imshow(rpi_name, image) # 1 window for each RPi
    cv2.waitKey(1)
    image_hub.send_reply(b'OK')