import cv2, dlib
import numpy as np
from imutils import face_utils
from keras.models import load_model
import datetime as pydatetime
import datetime as pydatetime
from timeit import default_timer as timer
from datetime import timedelta

IMG_SIZE = (34, 26)

detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor('shape_predictor_68_face_landmarks.dat')

model = load_model('models/2018_12_17_22_58_35.h5')
#model.summary()

def crop_eye(img, eye_points):
  x1, y1 = np.amin(eye_points, axis=0)
  x2, y2 = np.amax(eye_points, axis=0)
  cx, cy = (x1 + x2) / 2, (y1 + y2) / 2

  w = (x2 - x1) * 1.2
  h = w * IMG_SIZE[1] / IMG_SIZE[0]

  margin_x, margin_y = w / 2, h / 2

  min_x, min_y = int(cx - margin_x), int(cy - margin_y)
  max_x, max_y = int(cx + margin_x), int(cy + margin_y)

  eye_rect = np.rint([min_x, min_y, max_x, max_y]).astype(np.int)

  eye_img = gray[eye_rect[1]:eye_rect[3], eye_rect[0]:eye_rect[2]]

  return eye_img, eye_rect

def get_now():
    """
        현재 시스템 시간을 datetime형으로 반환
    """
    return pydatetime.datetime.now()

def get_now_timestamp():
    """
        현재 시스템 시간을 POSIX timestamp float형으로 반환
    """
    return get_now().timestamp()
# main
cap = cv2.VideoCapture(0)
count=0
count_=0
timestamp=[]
while cap.isOpened():
  ret, img_ori = cap.read()

  if not ret:
    break

  img_ori = cv2.resize(img_ori, dsize=(0, 0), fx=0.5, fy=0.5)

  img = img_ori.copy()
  gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
  
  gray = cv2.normalize(gray, None, 0, 255, cv2.NORM_MINMAX) # 히스토그램 스트레칭은 NORM_MINMAX
# 넘파이로 히스토그램 스트레칭 구현 
  gmin = np.min(gray)
  gmax = np.max(gray)
  gray = np.clip((gray - gmin) * 255. / (gmax - gmin), 0, 255).astype(np.uint8)
  

  faces = detector(gray)

  for face in faces:
    
    shapes = predictor(gray, face)
    shapes = face_utils.shape_to_np(shapes)

    eye_img_l, eye_rect_l = crop_eye(gray, eye_points=shapes[36:42])
    eye_img_r, eye_rect_r = crop_eye(gray, eye_points=shapes[42:48])

    eye_img_l = cv2.resize(eye_img_l, dsize=IMG_SIZE)
    eye_img_r = cv2.resize(eye_img_r, dsize=IMG_SIZE)
    eye_img_r = cv2.flip(eye_img_r, flipCode=1)

    cv2.imshow('l', eye_img_l)
    cv2.imshow('r', eye_img_r)

    eye_input_l = eye_img_l.copy().reshape((1, IMG_SIZE[1], IMG_SIZE[0], 1)).astype(np.float32) / 255.
    eye_input_r = eye_img_r.copy().reshape((1, IMG_SIZE[1], IMG_SIZE[0], 1)).astype(np.float32) / 255.

    pred_l = model.predict(eye_input_l)
    pred_r = model.predict(eye_input_r)
    
    if(pred_l<0.0005 or pred_r<0.0005): 
      time=get_now_timestamp()
      timestamp.append(time)
      if(count>0):
        blink=timestamp[count]-timestamp[count-1]
        if(0.09<blink<0.4):
          count_+=1
          print("BINGO",count_)
          last=timestamp.pop(-1)
          timestamp.clear()
          count=0
          timestamp.append(last)
          #print(timestamp)
      count+=1
      
    # visualize
    state_l = 'O %.1f' if pred_l > 0.1 else '- %.1f'
    state_r = 'O %.1f' if pred_r > 0.1 else '- %.1f'

    state_l = state_l % pred_l
    state_r = state_r % pred_r

    cv2.rectangle(img, pt1=tuple(eye_rect_l[0:2]), pt2=tuple(eye_rect_l[2:4]), color=(255,255,255), thickness=2)
    cv2.rectangle(img, pt1=tuple(eye_rect_r[0:2]), pt2=tuple(eye_rect_r[2:4]), color=(255,255,255), thickness=2)

    cv2.putText(img, state_l, tuple(eye_rect_l[0:2]), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255,255,255), 2)
    cv2.putText(img, state_r, tuple(eye_rect_r[0:2]), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255,255,255), 2)

  cv2.imshow('result', img)
  if cv2.waitKey(23) == ord('q'):
    break
#test