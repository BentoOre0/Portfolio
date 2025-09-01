import os
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix, classification_report
from PIL import Image
import tensorflow as tf
from tensorflow.keras import layers, models
import time
import pandas as pd

dataset_path = r"C:\Users\jerem\imageplayground\archive"

baybayin_labels = ['a', 'b', 'ba', 'be_bi', 'bo_bu', 'd', 'da_ra', 'de_di', 'do_du', 'e_i', 'g', 'ga', 'ge_gi', 'go_gu',
                   'h', 'ha', 'he_hi', 'ho_hu', 'k', 'ka', 'ke_ki', 'ko_ku', 'l', 'la', 'le_li', 'lo_lu', 'm', 'ma',
                   'me_mi', 'mo_mu', 'n', 'na', 'ne_ni', 'ng', 'nga', 'nge_ngi', 'ngo_ngu', 'no_nu', 'o_u', 'p', 'pa',
                   'pe_pi', 'po_pu', 'r', 'ra', 're_ri', 'ro_ru', 's', 'sa', 'se_si', 'so_su', 't', 'ta', 'te_ti',
                   'to_tu', 'w', 'wa', 'we_wi', 'wo_wu', 'y', 'ya', 'ye_yi', 'yo_yu']

def load_dataset(dataset_path):
    images = []
    labels = []
    for filename in os.listdir(dataset_path):
        if filename.endswith(".jpg"):
            label = filename.split('.')[0]
            if label in baybayin_labels:
                img_path = os.path.join(dataset_path, filename)
                img = Image.open(img_path).convert('L')  # Convert to grayscale
                img_array = np.array(img) / 255.0  # Normalize pixel values
                images.append(img_array)
                labels.append(baybayin_labels.index(label))
    return np.array(images), np.array(labels)

X, y = load_dataset(dataset_path)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.5, shuffle=True, random_state=42)

# Reshape the data for CNN input
X_train = X_train.reshape(X_train.shape[0], 28, 28, 1)
X_test = X_test.reshape(X_test.shape[0], 28, 28, 1)
num_classes = len(baybayin_labels)
y_train = tf.keras.utils.to_categorical(y_train, num_classes)
y_test = tf.keras.utils.to_categorical(y_test, num_classes)

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



# Custom callback to stop training after the specified time
class TimeoutCallback(tf.keras.callbacks.Callback):
    def __init__(self, timeout_in_seconds):
        super(TimeoutCallback, self).__init__()
        self.timeout_in_seconds = timeout_in_seconds
        self.start_time = None
        self.epoch_start_time = None
        self.time_per_epoch = []

    def on_train_begin(self, logs=None):
        self.start_time = time.time()

    def on_epoch_begin(self, epoch, logs=None):
        self.epoch_start_time = time.time()

    def on_epoch_end(self, epoch, logs=None):
        epoch_time = time.time() - self.epoch_start_time
        self.time_per_epoch.append(epoch_time)

        time_elapsed = time.time() - self.start_time
        time_left = self.timeout_in_seconds - time_elapsed

        if time_left <= 0:
            self.model.stop_training = True
            print(f"\nTimeout reached. Stopping training after {epoch + 1} epochs.")
        else:
            avg_time_per_epoch = sum(self.time_per_epoch) / len(self.time_per_epoch)
            estimated_epochs_left = int(time_left / avg_time_per_epoch)
            print(f"\nEstimated time left: {time_left:.2f} seconds (approximately {estimated_epochs_left} more epochs)")

    def on_train_end(self, logs=None):
        total_time = time.time() - self.start_time
        print(f"\nTotal training time: {total_time:.2f} seconds")

start_time = time.time()
history = model.fit(
    X_train, y_train,
    batch_size=32,
    epochs=100,
    validation_split=0.5,
    callbacks=[TimeoutCallback(4.04)],
    verbose=1
)
end_time = time.time()

test_loss, test_accuracy = model.evaluate(X_test, y_test, verbose=0)
print(f"Test accuracy: {test_accuracy:.4f}")
print(f"Training time: {end_time - start_time:.2f} seconds")

y_pred = model.predict(X_test)
y_pred_classes = np.argmax(y_pred, axis=1)
y_true_classes = np.argmax(y_test, axis=1)

cm = confusion_matrix(y_true_classes, y_pred_classes)
cm_df = pd.DataFrame(cm, index=baybayin_labels, columns=baybayin_labels)
cm_df.to_csv("cnn_confusion.csv")

cr = classification_report(y_true_classes, y_pred_classes, target_names=baybayin_labels, output_dict=True)
cr_df = pd.DataFrame(cr).transpose()
cr_df.to_csv("cnn_metrics.csv")