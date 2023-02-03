import os
from PIL import Image
import imagerec.detect
from imagerec import weights
import time
import sys
from pathlib import Path

from imagerec.helpers import get_path_to

# Set up all the parameters here
best_pt_filename = "best.pt"

detect_folder: Path = get_path_to(imagerec.runs.detect)
model_weights_folder: Path = get_path_to(weights)
best_pt_path: Path = model_weights_folder.joinpath(best_pt_filename)

def get_exp_number_from_foldername(exp_name: str) -> int:
    """ Get the experiment number from the experiment name experiment names are: "exp", "exp2", "exp3", etc """
    int_string = exp_name[3:]
    if int_string == "":
        return 1
    else:
        return int(int_string)

def get_exp_number_from_folderpath(folderpath: Path) -> int:
    """ Get the experiment number from the experiment name experiment paths are: "exp", "exp2", "exp3", etc """
    return get_exp_number_from_foldername(folderpath.name)

def get_latest_exp_folder_from_detect(detectpath: Path) -> Path:
    """Get the latest exp folder. This is the exp with largest integer label"""
    return sorted(detectpath.glob("exp*"), key=get_exp_number_from_folderpath)[-1]

def merge_image():
    predicted_folder = get_latest_exp_folder_from_detect(detect_folder)
    images_names = os.listdir(predicted_folder)

    images = []
    basewidth = 640
    for imageName in images_names:
        imageLocation = predicted_folder.joinpath(imageName)
        img = Image.open(imageLocation)
        wpercent = (basewidth/float(img.size[0]))
        hsize = int((float(img.size[1])*float(wpercent)))
        img = img.resize((basewidth,hsize), Image.ANTIALIAS) # resize each image
        images.append(img)

    size = len(images)
    width = images[0].width
    height = images[0].height
    merged_image = Image.new('RGB', (size*width, height))
    for i in range(size):
        merged_image.paste(images[i], (i*width,0))

    save_path = predicted_folder.joinpath('merged_image.jpg')
    merged_image.save(save_path)


def predict(image_path):
    weight_path = str(best_pt_path)
    os.system(f'python -m imagerec.detect --weights "{weight_path}" --img 640 --source "{image_path}"')
    print('merging image')
    merge_image()

if __name__ == "__main__":
    image_path = sys.argv[1]
    predict(image_path)
