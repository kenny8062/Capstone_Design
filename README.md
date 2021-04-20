# Capstone_Design
종설

multi-tasked cascaded face detection (MTCNN)
face detection, bounding box regressioni(얼굴 위치를 알려주는 박스의 위치를 세세하게 조정하는), face alignment(눈코입의 위치를 알려주는) 3개의 task를 동시에 실현시켜서 multi task

p-net → o-net → r-net

간단하고 빠르게 처리할 수 있지만, bottle neck 현상이 일어날 수 있다. 

참고: https://github.com/ipazc/mtcnn

face detection algorithms 비교
https://towardsdatascience.com/face-detection-models-which-to-use-and-why-d263e82c302c


