import os
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix, classification_report
from PIL import Image
import tensorflow as tf
from tensorflow.keras import layers, models
import time
from sklearn import metrics, svm
import csv
from CNN import TimeoutCallback

dataset_path = r"C:\Users\jerem\imageplayground\archive"

baybayin_labels = ['a', 'b', 'ba', 'be_bi', 'bo_bu', 'd', 'da_ra', 'de_di', 'do_du', 'e_i', 'g', 'ga', 'ge_gi', 'go_gu',
                   'h', 'ha', 'he_hi', 'ho_hu', 'k', 'ka', 'ke_ki', 'ko_ku', 'l', 'la', 'le_li', 'lo_lu', 'm', 'ma',
                   'me_mi', 'mo_mu', 'n', 'na', 'ne_ni', 'ng', 'nga', 'nge_ngi', 'ngo_ngu', 'no_nu', 'o_u', 'p', 'pa',
                   'pe_pi', 'po_pu', 'r', 'ra', 're_ri', 'ro_ru', 's', 'sa', 'se_si', 'so_su', 't', 'ta', 'te_ti',
                   'to_tu', 'w', 'wa', 'we_wi', 'wo_wu', 'y', 'ya', 'ye_yi', 'yo_yu']

# test_datapaths = [r'C:\Users\jerem\imageplayground\archive_rotated_neg_30',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_25',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_20',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_15',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_10',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_5',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_zero_0',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_5',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_10',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_15',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_20',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_25',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_30']

# test_datapaths = [r'C:\Users\jerem\imageplayground\archive_rotated_neg_55',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_50',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_45',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_40',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_35',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_35',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_40',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_45',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_50',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_55']
#
# test_datapaths = [r'C:\Users\jerem\imageplayground\archive_rotated_neg_90',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_85',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_80',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_75',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_70',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_65',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_neg_60',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_60',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_65',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_70',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_75',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_80',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_85',
#                   r'C:\Users\jerem\imageplayground\archive_rotated_pos_90']

test_datapaths = [r'C:\Users\jerem\imageplayground\archive_added_0.00',
                  r'C:\Users\jerem\imageplayground\archive_added_0.05',
                  r'C:\Users\jerem\imageplayground\archive_added_0.10',
                  r'C:\Users\jerem\imageplayground\archive_added_0.15',
                  r'C:\Users\jerem\imageplayground\archive_added_0.20',
                  r'C:\Users\jerem\imageplayground\archive_added_0.25',
                  r'C:\Users\jerem\imageplayground\archive_added_0.30',
                  r'C:\Users\jerem\imageplayground\archive_added_0.35',
                  r'C:\Users\jerem\imageplayground\archive_added_0.40',
                  r'C:\Users\jerem\imageplayground\archive_added_0.45',
                  r'C:\Users\jerem\imageplayground\archive_added_0.50',
                  r'C:\Users\jerem\imageplayground\archive_added_0.55',
                  r'C:\Users\jerem\imageplayground\archive_added_0.60',
                  r'C:\Users\jerem\imageplayground\archive_added_0.65',
                  r'C:\Users\jerem\imageplayground\archive_added_0.70',
                  r'C:\Users\jerem\imageplayground\archive_added_0.75',
                  r'C:\Users\jerem\imageplayground\archive_added_0.80',
                  r'C:\Users\jerem\imageplayground\archive_added_0.85',
                  r'C:\Users\jerem\imageplayground\archive_added_0.90',
                  r'C:\Users\jerem\imageplayground\archive_added_0.90',
                  r'C:\Users\jerem\imageplayground\archive_added_1.00'
                  ]
# test_datapaths = [r'C:\Users\jerem\imageplayground\archive_added_0.00']

def load_dataset(dataset_path):
    images = []
    labels = []
    for filename in os.listdir(dataset_path):
        if filename.endswith(".jpg"):
            label = filename.split('.')[0]
            if label in baybayin_labels:
                img_path = os.path.join(dataset_path, filename)
                img = Image.open(img_path).convert('L')  # Convert to grayscale
                img_array = np.array(img).flatten() / 255.0  # Normalise pixel values
                images.append(img_array)
                labels.append(baybayin_labels.index(label))
    return np.array(images), np.array(labels)


X, y = load_dataset(dataset_path)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.5, random_state=42)

clf = svm.SVC(C=0.01, kernel='poly', degree=6, gamma='scale', decision_function_shape='ovo')
clf.fit(X_train, y_train)
X_train_cnn = X_train.reshape(X_train.shape[0], 28, 28, 1)
X_test_cnn = X_test.reshape(X_test.shape[0], 28, 28, 1)

num_classes = len(baybayin_labels)
y_train_cnn = tf.keras.utils.to_categorical(y_train, num_classes)
y_test_cnn = tf.keras.utils.to_categorical(y_test, num_classes)

# Define and train CNN model
model = models.Sequential([
    layers.Input(shape=(28, 28, 1)),
    layers.Conv2D(32, (5, 5), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    layers.Flatten(),
    layers.Dense(128, activation='relu'),
    layers.Dropout(0.3),
    layers.Dense(num_classes, activation='softmax')
])

model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
              loss='categorical_crossentropy',
              metrics=['accuracy'])

history = model.fit(
    X_train_cnn, y_train_cnn,
    batch_size=32,
    epochs=100,
    validation_split=0.5,
    callbacks=[TimeoutCallback(4)],
    verbose=1
)

headings = []
SVCACC = []
CNNACC = []

for path in test_datapaths:
    temp = path.split('_')
    value = temp[-1]
    headings.append(value)

    X, y = load_dataset(path)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.5, random_state=42)
    svc_predicted = clf.predict(X_test)
    svc_accuracy = metrics.accuracy_score(y_test, svc_predicted)
    SVCACC.append(svc_accuracy)

    X_test_cnn = X_test.reshape(X_test.shape[0], 28, 28, 1)
    cnn_predicted = model.predict(X_test_cnn)
    cnn_predicted_classes = np.argmax(cnn_predicted, axis=1)
    cnn_accuracy = metrics.accuracy_score(y_test, cnn_predicted_classes)
    CNNACC.append(cnn_accuracy)

with open('noise_accuracies.csv', 'w', newline='') as file:
    writer = csv.writer(file)
    writer.writerow(['Noise', 'SVC Accuracy', 'CNN Accuracy'])
    for heading, svc_acc, cnn_acc in zip(headings, SVCACC, CNNACC):
        writer.writerow([heading, svc_acc, cnn_acc])

print("Results saved to 'noise_accuracies.csv'")
