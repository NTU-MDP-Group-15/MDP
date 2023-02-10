import unittest
from pathlib import Path
import imagerec.tests.images as test_images
from imagerec.helpers import get_path_to, get_image_from
from imagerec.infer import infer

class TestInfer(unittest.TestCase):
    def test_infer_all_test_images(self):
        test_images_folder = get_path_to(test_images)

        # Add more test image names and their actual labels here
        image_filename_to_actual_label = {
            "1.jpg": "One",
            "2.jpg": "Two",
            "3.jpg": "Three",
            "4.jpg": "Four",
            "5.jpg": "Five",
            "6.jpg": "Six",
            "7.jpg": "Seven",
            "8.jpg": "Eight",
            "9.jpg": "Nine",

            "A.jpg": "A",
            "B.jpg": "B",
            "C.jpg": "C",
            "D.jpg": "D",
            "E.jpg": "E",
            "F.jpg": "F",
            "G.jpg": "G",
            "H.jpg": "H",

            "S.jpg": "S",
            "T.jpg": "T",
            "U.jpg": "U",
            "V.jpg": "V",
            "W.jpg": "W",
            "X.jpg": "X",
            "Y.jpg": "Y",
            "Z.jpg": "Z",

            "Bullseye.jpg": "Bullseye",
            "Up.jpg": "Up",
            "Down.jpg": "Down",
            "Left.jpg": "Left",
            "Right.jpg": "Right",
            "Stop.jpg": "Stop",
        }

        test_images_files = test_images_folder.glob("*.jpg")
        for test_image_file in test_images_files:
            actual_label = image_filename_to_actual_label.get(test_image_file.name)
            # convert the number to the label
            if actual_label is None:
                self.print_warning(f"File \"{test_image_file}\" has no label.\n"
                                    "Skipping test inference on this file...")
                continue
            image = get_image_from(test_image_file)

            predicted_label = infer(image)[0]

            self.assertEqual(actual_label, predicted_label)
    
    def print_warning(self, message):
        print("!!!!! Note !!!!!")
        print(message)
        print("################")

if __name__ == "__main__":
    unittest.main()
