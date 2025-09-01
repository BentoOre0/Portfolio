from matplotlib import pyplot as plt
import os
import numpy as np
from PIL import Image
from sklearn import metrics, svm
from sklearn.model_selection import train_test_split, GridSearchCV

# Define the path to your dataset
dataset_path = r"C:\Users\jerem\imageplayground\archive"

# List of Baybayin character labels
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
                img_array = np.array(img).flatten() / 255.0  # Normalise pixel values, make life easier
                images.append(img_array)
                labels.append(baybayin_labels.index(label))

    return np.array(images), np.array(labels)


print("loading data")
X, y = load_dataset(dataset_path)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.5, random_state=42)
param_grid = [
    {
        'C': [0.01, 0.1, 1, 10, 100],
        'gamma': ['scale', 'auto', 0.1, 1],
        'kernel': ['rbf', 'sigmoid', 'linear'],
        'decision_function_shape': ['ovo', 'ovr'],
    },

    {
        'C': [0.01, 0.1, 1, 10, 100],
        'gamma': ['scale', 'auto', 0.1, 1],
        'kernel': ['poly'],
        'degree': [3, 4, 5, 6],
        'decision_function_shape': ['ovo', 'ovr'],
    }
]

print("Performing hyperparameter tuning...")
grid_search = GridSearchCV(svm.SVC(), param_grid, cv=3, n_jobs=-1, verbose=2)
grid_search.fit(X_train, y_train)
best_clf = grid_search.best_estimator_
print("Best parameters found:")
print(grid_search.best_params_)
predicted = best_clf.predict(X_test)
_, axes = plt.subplots(nrows=1, ncols=4, figsize=(10, 3))
for ax, image, prediction in zip(axes, X_test, predicted):
    ax.set_axis_off()
    image = image.reshape(28, 28)
    ax.imshow(image, cmap=plt.cm.gray_r, interpolation="nearest")
    ax.set_title(f"Pred: {baybayin_labels[prediction]}")
report = metrics.classification_report(y_test, predicted, target_names=baybayin_labels)
print(f"Classification report for best classifier:\n{report}\n")

accuracy = metrics.accuracy_score(y_test, predicted)
print(f"Accuracy: {accuracy:.2f}")

with open("accuracy_score.txt", "w") as f:
    f.write(f"Accuracy: {accuracy:.2f}")

disp = metrics.ConfusionMatrixDisplay.from_predictions(y_test, predicted, display_labels=baybayin_labels,
                                                       xticks_rotation=90)
np.savetxt("confusion_matrix.txt", disp.confusion_matrix, fmt='%d', delimiter=',')
np.savetxt("confusion_matrix.txt", disp.confusion_matrix, fmt='%d', delimiter=',')
print("Confusion matrix saved as 'confusion_matrix.txt'")
print(f"Confusion matrix:\n{disp.confusion_matrix}")
