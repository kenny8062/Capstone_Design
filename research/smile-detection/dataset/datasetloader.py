import os
import cv2
import imutils
import numpy as np

from imutils                   import paths ## oopencv의 부족한 기능을 보완해주는 패키지 
from keras.preprocessing.image import img_to_array


class DatasetLoader:
    """
    Class DatasetLoader reads data and labels from given path
    """
    def __init__(self, data_path):
        """
        Constructor 

        :param data_path: the path of dataset
        """
        self.data = []
        self.labels = []
        self.data_path = data_path

    def load(self, verbose=-1, image_width=28, label_name_position=-3): ## labe_name_position: img_path를 split을 하고 -3인 위치에 label이 있다
                                                                        ## ex) 2021 / 05 / 10 / smile / thomas / jpg 
                                                                        ## 해당 path에서 img를 가져와 전처리 후 forward 한 값과 label 값이 일치하는지 확인
        """
        Function to load the dataset from given path

        :param verbose: verbosity level, will print information each verbose
        :param image_width:  option width to resize images, default is 28
        :param label_name_position: position where is the label name in path
        :return: a tuple of numpy arrays data and labels
        """
        image_paths = sorted(list(paths.list_images(self.data_path)))
        for (i, image_path) in enumerate(image_paths): 
            image = self._loadImage(image_path, image_width) 
            self.data.append(image) 
            label = self._extractLabel(image_path, label_name_position)
            self.labels.append(label) 

            if verbose > 0 and i > 0 and (i + 1) % verbose == 0:
                print("[INFO] processed {}/{}".format(i + 1, len(image_paths)))

        self.data   = np.array(self.data, dtype="float") / 255.0
        self.labels = np.array(self.labels)
        return (self.data, self.labels) ### 준비된 데이터들(images)로 부터 이미지와 label들을 추출하여 저장한 list들을 반환한다. 

    def _loadImage(self, image_path, image_width):
        """
        Internal method to load image using OpenCV

        :param image_path: the path of the image which will be loaded
        :param image_width: the width to resize image
        :return: image loaded with OpenCV
        """
        image = cv2.imread(image_path)
        image = self._preprocess(image, image_width)
        return image

    def _preprocess(self, image, image_width): ### img를 greyscale로 변환후 resize 하고 array 형태로 변환하여 반환하는 전처리 과정
        """
        Internal method to preprocess the image

        :param image: the image to be preprocessed
        :param image_width: the width to resize image
        :return: the image preprocessed
        """
        image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        image = imutils.resize(image, width=image_width)
        image = img_to_array(image)
        return image

    def _extractLabel(self, image_path, label_name_pos):
        """
        Internal method to extract the labels from path

        :param image_path: the path of the image
        :param label_name_pos: position where to extract label from path
        :return: a label String
        """
        label = image_path.split(os.path.sep)[label_name_pos]
        label = "smiling" if label == "positives" else "not_smiling"
        return label
