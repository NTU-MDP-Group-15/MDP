import cv2

#cv2.namedWindow("preview")
cap = cv2.VideoCapture(cv2.CAP_V4L2) # video capture source camera (Here webcam of laptop) 
#cap = cv2.VideoCapture(0) # video capture source camera (Here webcam of laptop) 
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

ret, frame = cap.read() # return a single frame in variable `frame`

if ret:
    print("ret is true")
else:
    print("ret is false")
    
#while(True):
#    cv2.imshow('frame',frame) #display the captured image
#    if cv2.waitKey(1) & 0xFF == ord('y'): #save on pressing 'y' 
#        #cv2.imwrite('c1.png',frame)
#        cv2.destroyAllWindows()
#        break
cv2.destroyAllWindows()
cap.release()
