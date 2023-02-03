# How to use the imagerec package

## Install the package in development mode

Before you start, it is better to create a virtual environment
for installation, just in case there is any dependency conflict.
In the root folder containing `setup.py` file, run

```sh
pip install -e .
```

This will make the package `imagerec` available for imports.

## Note the important scripts

1. The main package should look like this:

    ```markdown
        - imagerec (the folder that you just extracted)
            - a lot of stuff but importantly
            - predict.py
            - detect.py
            - infer.py
            - ... and so on
    ```

2. Before running the scripts below, remember to remove any folders or files in the 'runs/detect' folder.
   

3. `predict.py` contains the function that can draw bounding boxes on images,
    generate a merged image and
    save these outputs to `./imagerec/runs/detect/exp{i}` folder, where `i`
    represents the number of times the predict is called. This is suitable for
    week 8 task. To include the function that can draw bounding box, put the
    following in your code:

    ```python
    img_path = 'path_to_image'
    os.system(f'python -m imagerec.predict {img_path}')
    ```

    **Note**: If you want to predict > 1 photo, just put the `path_to_image` as the path to folder containing all the images that you want to predict.

4. `infer.py` contains the fxn that can infer what the object is without
    drawing bounding boxes. This is much faster and suitable for week 9 task.

    To run this function in CLI within python, put the following in your code:

    ```python
    import os
    img_path = 'path_to_image'
    res = os.popen(f'python -m imagerec.infer {img_path}').read()
    print('result is: ', res)
    ```

    Alternatively, to run the code in the usual way ():

    ```python
    from imagerec.infer import infer, get_image_from
    image_path = 'path_to_image'
    image = get_image_from(image_path)
    res = infer(image)
    print('result is: ', res)
    ```

    **Note**: the image should be loaded as RGB PIL Image for the `infer()`
    function to work correctly. The helper function `get_image_from()` helps to
    load the image in the correct format. **Use this function instead of
    `cv2.imread()`.**

## Testing

All the tests are put under `imagerec.tests`. Whenever making any changes to
the model, ensure that all the tests in this folder still pass.

In the root folder containing `setup.py` file, run

```sh
python -m unittest discover imagerec.tests
```

## Acknowledgements

The models and training pipelines are taken from https://github.com/ultralytics/yolov5
Originally training in our own local machines, we made a lot of changes to the code structure.
When the need arose to share the code to collaborate among the group, due to the rush of completing
the project and having to keep developments private during the course of the CZ3004 module, we did
not create a proper fork from the YOLOv5 repo. Now, we have tried our best to keep
the licence intact and made our code public. Do let us know if there is a better way to handle this
moving forward.
