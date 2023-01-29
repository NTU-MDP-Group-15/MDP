# #You may need to install these packages first
import torch
from imagerec.models.experimental import attempt_load
from imagerec import weights
import imagerec
import numpy as np
import os
import cv2
from matplotlib import pyplot as plt
import time
import sys
from imagerec.helpers import get_path_to, get_image_from

# Set all the parameters here
best_pt_filename = "best.pt"

model_weights_folder = get_path_to(weights)
imagerec_path = get_path_to(imagerec)

best_pt_path = model_weights_folder.joinpath(best_pt_filename)
model = torch.hub.load(str(imagerec_path), 'custom', path=str(best_pt_path), source='local') # local model dont need internet

#Function for single prediction
#Expect an RGB image. Do note to convert to RGB if image is opened with opencv
def infer(img) -> str:
    # Do not touch the categories
    category = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', 'Bullseye', 'Down', 'Eight', 'Five', 'Four', 'Left', 'Nine', 'One', 'Right', 'Seven', 'Six', 'Stop', 'Three', 'Two', 'Up']
    output = model(img).pred
    # print(float(output[0][0][4])) # conf score
    return [category[int(output[0][i][-1])] for i in range(len(output[0]))] if len(output[0]) else 'Nothing detected'

# when running this file using `os.popen(python...)`
if __name__ == "__main__":
    image_path = sys.argv[1]
    img = get_image_from(image_path)
    res = infer(img)
    print(res, end="") # do not add \n to result
