from flask import Flask, render_template, url_for
import os
import glob

app = Flask(__name__)

# Define the path to the directory containing your images

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(CUR_DIR, 'static')
# IMG_DIR = os.path.join('static', 'images')
app.config['UPLOAD_FOLDER'] = IMG_DIR

@app.route('/')
def index():
    image_names = list()
    
    _, dirs, _ = next(os.walk(IMG_DIR))
    
    for dir in dirs:
        _, _, files = next(os.walk(os.path.join(IMG_DIR,dir)))
        for file in files:
            image_names.append(dir+'/'+file)
    # Find all the image files in the image directory
    # image_names = os.listdir(app.config['UPLOAD_FOLDER'])

    # Render the HTML template, passing in the image filenames
    return render_template('index.html', image_names=image_names)

if __name__ == '__main__':
    app.run(debug=True)
    # image_names = os.listdir(app.config['UPLOAD_FOLDER'])
    # print(image_names)
    # image_names = list()
    
    # _, dirs, _ = next(os.walk(IMG_DIR))
    
    # for dir in dirs:
    #     _, _, files = next(os.walk(os.path.join(IMG_DIR,dir)))
    #     for file in files:
    #         image_names.append(os.path.join(dir, file))
            
    # print(image_names)
    
    