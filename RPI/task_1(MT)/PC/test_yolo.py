'''
pip install yolov5 instead of ultralytics 

'''

import cv2
import imagezmq
# import imutils
import numpy as np
import torch
import time
import socket
import yolov5
import re
import traceback

from PIL import Image


MIN_CONFIDENCE_THRESHOLD = 0.55         # Change this to ensure no double results
NON_RED_CONFIDENCE_THRESHOLD = 0.55

IMGREC_PORT = 12348
ZMQ_ADDRESS = "192.168.15.69"
ZMQ_PORT = 5555

def main():
    # Model
    model = torch.hub.load('../yolov5', 'custom', path='./bestv5.pt',
                                    source='local')
    model.conf = 0.60  # confidence threshold (0-1)
    model.iou = 0.45  # NMS IoU threshold (0-1) 
    # model = torch.hub.load('ultralytics/yolov5', 'yolov5s')  # or yolov5m, yolov5l, yolov5x, etc.
    # model = torch.hub.load('ultralytics/yolov5', 'custom', 'path/to/best.pt')  # custom trained model

    # Images
    # im = 'https://ultralytics.com/images/zidane.jpg'  # or file, Path, URL, PIL, OpenCV, numpy, list
    im = "../images/Bullseye.jpg"
    # im = "../images/8.jpg"

    # Inference
    results = model(im)
    results.show()      # shows image with box
    # results.print()  # or .show(), .save(), .crop(), .pandas(), etc.
    
    # results.xyxy[0]  # im predictions (tensor)
    # pd = results.pandas().xyxy[0]["name"][0]  # im predictions (pandas)
    #      xmin    ymin    xmax   ymax  confidence  class    name
    # 0  749.50   43.50  1148.0  704.5    0.874023      0  person
    # 2  114.75  195.75  1095.0  708.0    0.624512      0  person
    # 3  986.00  304.00  1028.0  420.0    0.286865     27     tie
    # print(pd)
    
    # info = results.pandas().xyxy[0]
    # info2 = results.pandas().xyxy[0].to_dict(orient = "records")
    # if len(info2) != 0:
        # for result in info2:
            # information = result['label']   
    pd = results.pandas().xyxy[0]
    print(get_name_with_higest_confidence(pd))
    
    # pred = results.pandas().xyxy[0]
    # for index, row in pred.iterrows():        
    #     print(row['name'], row['confidence'])
    #     if str(row['class']) == "0":
    #         print("PERSON FOUND")
    #     if float(row['confidence']) > 0.7:
    #         print("HIT")
    
    
def get_name_with_higest_confidence(pd):
    if len(pd) == 0: return 99
    highest_confidence = pd["confidence"].max()
    for index, row in pd.iterrows():    
        # print(row['name'])
        if row['confidence'] == highest_confidence:
            return row['name']
    
    
    
main()

def __init__(self, capture_index, model_name):
    """
    Initializes the class with youtube url and output file.
    :param url: Has to be as youtube URL,on which prediction is made.
    :param out_file: A valid output file name.
    """
    self.capture_index = capture_index
    self.model = self.load_model(model_name)
    self.classes = self.model.names
    self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
    print("Using Device: ", self.device)

def get_video_capture(self):
    """
    Creates a new video streaming object to extract video frame by frame to make prediction on.
    :return: opencv2 video capture object, with lowest quality frame available for video.
    """
  
    return cv2.VideoCapture(self.capture_index)

def load_model(self, model_name):
    """
    Loads Yolo5 model from pytorch hub.
    :return: Trained Pytorch model.
    """
    if model_name:
        model = torch.hub.load('ultralytics/yolov5', 'custom', path=model_name, force_reload=True)
    else:
        model = torch.hub.load('ultralytics/yolov5', 'yolov5s', pretrained=True)
    return model

def score_frame(self, frame):
    """
    Takes a single frame as input, and scores the frame using yolo5 model.
    :param frame: input frame in numpy/list/tuple format.
    :return: Labels and Coordinates of objects detected by model in the frame.
    """
    self.model.to(self.device)
    frame = [frame]
    results = self.model(frame)
    labels, cord = results.xyxyn[0][:, -1], results.xyxyn[0][:, :-1]
    return labels, cord

def class_to_label(self, x):
    """
    For a given label value, return corresponding string label.
    :param x: numeric label
    :return: corresponding string label
    """
    return self.classes[int(x)]

def plot_boxes(self, results, frame):
    """
    Takes a frame and its results as input, and plots the bounding boxes and label on to the frame.
    :param results: contains labels and coordinates predicted by model on the given frame.
    :param frame: Frame which has been scored.
    :return: Frame with bounding boxes and labels ploted on it.
    """
    labels, cord = results
    n = len(labels)
    x_shape, y_shape = frame.shape[1], frame.shape[0]
    for i in range(n):
        row = cord[i]
        if row[4] >= 0.3:
            x1, y1, x2, y2 = int(row[0]*x_shape), int(row[1]*y_shape), int(row[2]*x_shape), int(row[3]*y_shape)
            bgr = (0, 255, 0)
            cv2.rectangle(frame, (x1, y1), (x2, y2), bgr, 2)
            cv2.putText(frame, self.class_to_label(labels[i]), (x1, y1), cv2.FONT_HERSHEY_SIMPLEX, 0.9, bgr, 2)

    return frame

def __call__(self):
    """
    This function is called when class is executed, it runs the loop to read the video frame by frame,
    and write the output into a new file.
    :return: void
    """
    cap = self.get_video_capture()
    assert cap.isOpened()
  
    while True:
      
        ret, frame = cap.read()
        assert ret
        
        frame = cv2.resize(frame, (640,640))
        
        start_time = time()
        results = self.score_frame(frame)
        frame = self.plot_boxes(results, frame)
        
        end_time = time()
        fps = 1/np.round(end_time - start_time, 2)
        #print(f"Frames Per Second : {fps}")
         
        cv2.putText(frame, f'FPS: {int(fps)}', (20,70), cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0,255,0), 2)
        
        cv2.imshow('YOLOv5 Detection', frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
  
    cap.release()