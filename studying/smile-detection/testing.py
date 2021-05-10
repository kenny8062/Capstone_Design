from sklearn.preprocessing import LabelEncoder
from keras.utils import np_utils

list = ['smiling', 'smiling', 'non-smiling', 'smiling', 'smiling', 'smiling', 'non-smiling', 
'non-smiling', 'smiling', 'non-smiling', 'non-smiling', 'non-smiling', 'smiling', 'non-smiling', 
'smiling', 'smiling', 'smiling', 'smiling', 'smiling', 'smiling', 'smiling', 'smiling', 
'smiling', 'smiling', 'smiling', 'smiling', 'smiling']

encoder = LabelEncoder().fit(list)
print(encoder.transform(list))  

labels = np_utils.to_categorical(encoder.transform(list))
print(labels)

reshape = labels.sum(axis = 0)
print(reshape)

print(reshape.max()/reshape)


