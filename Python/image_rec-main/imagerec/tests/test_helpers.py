import unittest
from imagerec.helpers import get_path_to, get_image_from, convert_bgr_ndarray_to_rgb_image
from PIL import ImageChops, Image
import cv2
from imagerec.tests import images as test_images
from pathlib import Path

class TestHelperFunctions(unittest.TestCase):
    def test_get_path_to(self):
        test_images_folder = get_path_to(test_images)

        self.assertTrue(isinstance(test_images_folder, Path) and test_images_folder.exists())

    def test_get_images_from(self):
        test_images_folder = get_path_to(test_images)
        test_image_path = test_images_folder.joinpath("1.jpg")

        image = get_image_from(test_image_path)

        self.assertTrue(isinstance(image, Image.Image))
        self.assertEqual(image.mode, "RGB")

    def test_convert_bgr_ndarray_to_rgb_image(self):
        test_images_folder = get_path_to(test_images)
        test_image_path = test_images_folder.joinpath("1.jpg")
        rgb_image = get_image_from(test_image_path)

        # open using cv2
        bgr_array = cv2.imread(str(test_image_path))

        converted_rgb_image = convert_bgr_ndarray_to_rgb_image(bgr_array)

        self.assertTrue(isinstance(converted_rgb_image, Image.Image))
        self.assertEqual(converted_rgb_image.mode, "RGB")
        # check that the two images are the same
        image_diff = ImageChops.difference(rgb_image, converted_rgb_image)
        self.assertTrue(image_diff.getbbox() is None)

if __name__ == '__main__':
    unittest.main()
