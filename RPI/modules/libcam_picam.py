'''
Filename: libcam_picam.py
Version: 0.1

Contains functions to take photo
'''
import time

from picamera2 import Picamera2, Preview
from libcamera import controls

def capture_still_image():
    picam2 = Picamera2()
    
    # Preview settings
    #preview_config = picam2.create_preview_configuration(main={"size": (800, 600)})
    #picam2.configure(preview_config)
    #picam2.start_preview(Preview.QTGL)

    # Still image settings
    still_config = picam2.create_still_configuration()
    #still_config["format"] = "XBGR8888"

    # Camera settings syntax before .start()
    #picam2.controls.ExposureTime = 10000
    #picam2.controls.AfMode = controls.AfModeEnum.Continuous
    #picam2.controls.AfMode = controls.AfModeEnum.Continuous
    
    picam2.configure(still_config)
    picam2.start()
    time.sleep(2)
    
    # Camera settings syntax after .start()
    #picam2.set_controls({"AfTrigger":1})
    #picam2.set_controls({"AfMode":controls.AfModeEnum.Continuous})
    
    picam2.capture_file("test1.jpg")
    picam2.close()

def capture_still_image_switch(picam2):
    capture_config = picam2.create_still_configuration()
    picam2.start()
    time.sleep(1)
    picam2.switch_mode_and_capture_file(capture_config, "image.jpg")

def capture_print_metadata(picam2):
    # Capture a JPEG while still running in the preview mode. When you
    # capture to a file, the return value is the metadata for that image.
    picam2.start()
    time.sleep(1)
    metadata = picam2.capture_file("test1.jpg")
    print(metadata)
    
if __name__=="__main__":
    picam2 = Picamera2()
    still_config = picam2.create_still_configuration()
    still_config["format"] = "XBGR8888"
    print("config - ", still_config)
    
    picam2.configure(still_config)
    
    print("camera_controls - ", picam2.camera_controls)
    #for cont in picam2.camera_controls:
    #    print(cont)
    
    #capture_still_image(picam2)
    #capture_still_image_switch(picam2)
    #capture_print_metadata(picam2)
    
    picam2.close()
