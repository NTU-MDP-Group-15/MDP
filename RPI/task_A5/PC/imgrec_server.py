import cv2
import imagezmq
# import imutils
import numpy as np
import torch
import time
import socket
from ultralytics import YOLO
from PIL import Image

MIN_CONFIDENCE_THRESHOLD = 0.55         # Change this to ensure no double results
NON_RED_CONFIDENCE_THRESHOLD = 0.55

class ImgRecServer:
    def __init__(self):
        # Initialize ImageHub
        self.image_hub = imagezmq.ImageHub()
        self.host = "192.168.15.69"
        self.port = 12348
        self.s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s_sock.connect((self.host, self.port))
        # Load trained YOLOv8 pt model
        # self.model = torch.hub.load('ultralytics/yolov8', model_name, pretrained=True)
        self.model = YOLO("best.pt")

    def run(self):
        while True:
            try:
            # Receive image from client and update the timestamp
                (rpi_name, frame) = self.image_hub.recv_image()
                self.image_hub.send_reply(b'OK')
                # Detect objects in the image using YOLOv5
                # results = self.model(frame)
                results = self.model.predict(source=frame, show=True, conf=MIN_CONFIDENCE_THRESHOLD)

                # Predict single image frame
                # im1 = Image.open("bus.jpg")
                # results = model.predict(source=im1, save=True)  # save plotted images

                # Results
                # for result in results:
                # result.boxes.xyxy - box with xyxy format, (N, 4)
                # result.boxes.xywh - box with xywh format, (N, 4)
                # restul.boxes.xyxyn - box with xyxy format but normalized, (N, 4)
                # result.boxes.xywhn - box with xywh format but normalized, (N, 4)
                # result.boxes.conf - confidence score, (N, 1)
                # result.boxes.cls - class, (N, 1)
                
                check = self.processResults(results)
                if check == True:
                    cv2.destroyAllWindows()
                # Show the image
                # cv2.imshow("Received", imutils.resize(frame, width=400))
                key = cv2.waitKey(1)
                
                # Send a reply message to the client

                # Press 'q' to quit
                if cv2.waitKey(1) & key == ord('q'):
                    print("Terminating")
                    break
            except KeyboardInterrupt:
                break
         

        cv2.destroyAllWindows()

    def processResults(self, results):
        # print(results)
        print("Processing Image...")
        
        for result in results:
            print(result.boxes.conf)
            
            if len(result.boxes.conf)!=0:
                box_confidence = torch.IntTensor.item(result.boxes.conf[0])

                for c in result.boxes.cls:
                    recognized_id = self.model.names[int(c)]
                    print("ID: ", recognized_id,"\nConf: ", box_confidence)
                    # Send ID to Rpi Here
                    # FORMAT PC,ObstacleImg,6,4,ID
                    message = "AN,ObstacleImg,6,4,"+ recognized_id
                    print(type(recognized_id))
                    self.soc.sendall(b'%b' % message.encode('utf8'))
                    return True
        return False
                    




        # for result in results:
        #     print(result.boxes.xyxy)   # box with xyxy format, (N, 4))
        #     print(result.)
        # label_list = []

        # # Display the image with object detections
        # results.render()

        # # print results
        # results.print()

        # # Iterate over the detected objects
        # for i, det in enumerate(results.xyxy[0]):
        #     # Extract the object coordinates and label
        #     x1, y1, x2, y2, conf, cls = det.tolist()
        #     label = model.names[int(cls)]

        #     # Compute the object size and distance
        #     w = x2 - x1
        #     h = y2 - y1
        #     z = (fx * w) / (2 * (x2 - cx))
        #     dist = z / 1000.0  # convert to meters

        #     # Draw the bounding box and label on the image
        #     color = (0, 255, 0)
        #     cv2.rectangle(frame, (int(x1), int(y1)), (int(x2), int(y2)), color, 2)
        #     cv2.putText(frame, f'{label} {dist:.2f} m', (int(x1), int(y1 - 10)),
        #                 cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        # df = results.pandas().xyxy[0]
        # print(df)

        # for record in df:
        #     print(record)
        #     # if(float(record['confidence']) > 0.7):
        #     #     label_list.append(record['name'])

        # print(label_list)

        # self.sendResults(label_list)

    # def sendResults(self, label_list):
