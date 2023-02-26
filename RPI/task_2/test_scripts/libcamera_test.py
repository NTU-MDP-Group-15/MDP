#!/usr/bin/python3

# Capture a JPEG while still running in the preview mode. When you
# capture to a file, the return value is the metadata for that image.

import time

from picamera2 import Picamera2, Preview
from libcamera import controls

def capture_still_image(picam2):

    # Preview settings
    #preview_config = picam2.create_preview_configuration(main={"size": (800, 600)})
    #picam2.configure(preview_config)
    #picam2.start_preview(Preview.QTGL)

    # Camera settings syntax before .start()
    #picam2.controls.ExposureTime = 10000
    #picam2.controls.AfMode = controls.AfModeEnum.Continuous
    #picam2.controls.AfMode = controls.AfModeEnum.Continuous
    
    picam2.start()
    time.sleep(2)
    
    # Camera settings syntax after .start()
    #picam2.set_controls({"AfTrigger":1})
    #picam2.set_controls({"AfMode":controls.AfModeEnum.Continuous})
    
    picam2.capture_file("test1.jpg")
    

def capture_still_image_switch(picam2):
    capture_config = picam2.create_still_configuration()
    picam2.start()
    time.sleep(1)
    picam2.switch_mode_and_capture_file(capture_config, "image.jpg")

def capture_print_metadata(picam2):
    picam2.start()
    time.sleep(2)
    metadata = picam2.capture_file("test1.jpg")
    print(metadata)
    
if __name__=="__main__":
    picam2 = Picamera2()
    config = picam2.create_preview_configuration()
    print("config - ", config)
    #for conf in config:
    #    print(conf)
    
    #for cont in picam2.camera_controls:
    #    print(cont)
    
    #capture_still_image(picam2)
    #capture_still_image_switch(picam2)
    #capture_print_metadata(picam2)
    
    picam2.close()
