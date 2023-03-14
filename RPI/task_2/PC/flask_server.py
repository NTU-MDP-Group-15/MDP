import os
#import cv2
import imagezmq

from flask import Flask, render_template, Response

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(CUR_DIR, 'static')

RPI_IP = "192.168.15.1"
LIVE_PORT = 5557
LIVE_STREAM_ADDRESS = f"tcp://{RPI_IP}:{LIVE_PORT}"

app = Flask(__name__)

# Define the path to the directory containing your images
app.config['UPLOAD_FOLDER'] = IMG_DIR

@app.route('/')
def index():
    image_names = list()
    
    _, dirs, _ = next(os.walk(IMG_DIR))
    
    for dir in dirs:
        _, _, files = next(os.walk(os.path.join(IMG_DIR,dir)))
        for file in files:
            image_names.append(dir+'/'+file)

    # Render the HTML template, passing in the image filenames
    return render_template('index.html', image_names=image_names)

# @app.route('/video_feed')
# def video_feed():
#     return Response(sendImagesToWeb(), mimetype='multipart/x-mixed-replace; boundary=frame')

# def sendImagesToWeb():
#     # When we have incoming request, create a receiver and subscribe to a publisher
#     receiver = imagezmq.ImageHub(open_port=LIVE_STREAM_ADDRESS, REQ_REP = False)
#     while True:
#         # Pull an image from the queue
#         _, frame = receiver.recv_image()
#         ret, buffer = cv2.imencode('.jpg', frame)
#         frame = buffer.tobytes()
#         # cv2.imshow("HELLO", frame)
        
#         yield (b'--frame\r\n'
#                b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')  # concat frame one by one and show result


if __name__ == '__main__':
    app.run(debug=True)
    