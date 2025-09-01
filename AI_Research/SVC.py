import os
import numpy as np
from PIL import Image
from sklearn import metrics, svm
from sklearn.model_selection import train_test_split
import csv

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
                img_array = np.array(img).flatten() / 255.0  # Normalise pixel values
                images.append(img_array)
                labels.append(baybayin_labels.index(label))

    return np.array(images), np.array(labels)

print("Loading data...")
X, y = load_dataset(dataset_path)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.5, random_state=42)
print("Training the SVM model...")
clf = svm.SVC(C=0.01, kernel='poly', degree=6, gamma='scale', decision_function_shape='ovo')

clf.fit(X_train, y_train)
#this is 3.8769930000125896
print("Making predictions...")
predicted = clf.predict(X_test)

print("Generating classification report...")
report = metrics.classification_report(y_test, predicted, target_names=baybayin_labels)
print(f"Classification report:\n{report}\n")

accuracy = metrics.accuracy_score(y_test, predicted)
print(f"Accuracy: {accuracy:.2f}")

with open("accuracy_score.txt", "w") as f:
    f.write(f"Accuracy: {accuracy:.2f}")

print("Generating confusion matrix...")
conf_matrix = metrics.confusion_matrix(y_test, predicted)

with open("svc_confusion_matrix.csv", "w", newline='') as f:
    writer = csv.writer(f)
    writer.writerow([''] + baybayin_labels)
    for i, row in enumerate(conf_matrix):
        writer.writerow([baybayin_labels[i]] + list(row))

print("Confusion matrix saved as 'svc_confusion_matrix.csv'")
print(f"Confusion matrix:\n{conf_matrix}")

