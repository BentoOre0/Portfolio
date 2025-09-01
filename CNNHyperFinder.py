import os
import numpy as np
from sklearn.model_selection import train_test_split, ParameterGrid
from PIL import Image
import tensorflow as tf
from tensorflow.keras import layers, models
from scikeras.wrappers import KerasClassifier
import time
from tqdm import tqdm

np.random.seed(42)
tf.random.set_seed(42)

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
X_train = X_train.reshape(X_train.shape[0], 28, 28, 1)
X_test = X_test.reshape(X_test.shape[0], 28, 28, 1)
num_classes = len(baybayin_labels)
y_train = tf.keras.utils.to_categorical(y_train, num_classes)
y_test = tf.keras.utils.to_categorical(y_test, num_classes)

#"The TimeoutCallback class was developed with assistance from the Claude AI (Anthropic, 2024)."
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

def create_model(filters=32, kernel_size=3, pool_size=2, dense_units=64, dropout_rate=0.5, learning_rate=0.001):
    model = models.Sequential([
        layers.Input(shape=(28, 28, 1)),
        layers.Conv2D(filters, (kernel_size, kernel_size), activation='relu'),
        layers.MaxPooling2D((pool_size, pool_size)),
        layers.Conv2D(filters * 2, (kernel_size, kernel_size), activation='relu'),
        layers.MaxPooling2D((pool_size, pool_size)),
        layers.Flatten(),
        layers.Dense(dense_units, activation='relu'),
        layers.Dropout(dropout_rate),
        layers.Dense(num_classes, activation='softmax')
    ])

    model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=learning_rate),
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])
    return model


param_grid = {
    'filters': [32, 64],
    'kernel_size': [3, 5],
    'pool_size': [2, 3],
    'dense_units': [64, 128],
    'dropout_rate': [0.3, 0.5],
    'batch_size': [16, 32],
    'epochs': [100],  # Set high, will be limited by timeout class
    'learning_rate': [0.001, 0.0001]
}



# Define the parameter grid
param_combinations = list(ParameterGrid(param_grid))

print(f"Total hyperparameter combinations to test: {len(param_combinations)}")

results = []
pbar = tqdm(total=len(param_combinations), desc="Testing combinations")

for params in param_combinations:
    print(f"\nTesting parameters: {params}")

    model = KerasClassifier(
        model=create_model,
        filters=params['filters'],
        kernel_size=params['kernel_size'],
        pool_size=params['pool_size'],
        dense_units=params['dense_units'],
        dropout_rate=params['dropout_rate'],
        learning_rate=params['learning_rate'],  # Added learning rate
        epochs=params['epochs'],
        batch_size=params['batch_size'],
        verbose=0
    )

    start_time = time.time()
    model.fit(X_train, y_train, callbacks=[TimeoutCallback(3.8769930000125896)], steps_per_epoch=10) # 3.8769930000125896 is time taken to run the SVC algorithm
    end_time = time.time()

    score = model.score(X_test, y_test)
    time_taken = end_time - start_time

    results.append((params, score, time_taken))
    print(f"Accuracy: {score:.4f}, Time: {time_taken:.2f}s")
    pbar.update(1)

pbar.close()

print("\nResults:")
for params, score, time in sorted(results, key=lambda x: x[1], reverse=True):
    print(f"Score: {score:.4f}, Time: {time:.2f}s, Params: {params}")