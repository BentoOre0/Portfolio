import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from PIL import Image
import os

def read_confusion_matrix(file_path):
    """Read confusion matrix from a CSV file."""
    return pd.read_csv(file_path, index_col=0)

def find_misclassifications(confusion_matrix, threshold):
    """Find misclassifications above the given threshold."""
    misclassifications = []
    for true_label in confusion_matrix.index:
        for predicted_label in confusion_matrix.columns:
            if true_label != predicted_label:
                count = confusion_matrix.loc[true_label, predicted_label]
                if count > threshold:
                    misclassifications.append((true_label, predicted_label, count))
    return misclassifications

def analyze_misclassifications(cnn_matrix, svc_matrix, threshold):
    """Analyze misclassifications for CNN and SVC models."""
    cnn_misclassifications = find_misclassifications(cnn_matrix, threshold)
    svc_misclassifications = find_misclassifications(svc_matrix, threshold)

    cnn_set = set((true, pred) for true, pred, _ in cnn_misclassifications)
    svc_set = set((true, pred) for true, pred, _ in svc_misclassifications)

    cnn_only = cnn_set - svc_set
    svc_only = svc_set - cnn_set
    both = cnn_set.intersection(svc_set)

    return cnn_only, svc_only, both

def get_image_path(label, image_dir):
    """Get the path of the first image matching the label."""
    for filename in os.listdir(image_dir):
        if filename.startswith(f"{label}."):
            return os.path.join(image_dir, filename)
    return None

def plot_misclassifications(misclassifications, image_dir, title):
    """Plot the misclassified images with their true and predicted labels."""
    n = len(misclassifications)
    fig, axes = plt.subplots(n, 2, figsize=(10, 5*n))
    fig.suptitle(title, fontsize=16)

    for i, (true_label, pred_label) in enumerate(misclassifications):
        true_img_path = get_image_path(true_label, image_dir)
        pred_img_path = get_image_path(pred_label, image_dir)

        if true_img_path and pred_img_path:
            true_img = Image.open(true_img_path).convert('L')
            pred_img = Image.open(pred_img_path).convert('L')

            axes[i, 0].imshow(true_img, cmap='gray')
            # axes[i, 0].set_title(f"True: {true_label}")
            axes[i, 0].axis('off')

            axes[i, 1].imshow(pred_img, cmap='gray')
            # axes[i, 1].set_title(f"Predicted: {pred_label}")
            axes[i, 1].axis('off')
        else:
            axes[i, 0].text(0.5, 0.5, "Image not found", ha='center', va='center')
            axes[i, 1].text(0.5, 0.5, "Image not found", ha='center', va='center')
            axes[i, 0].axis('off')
            axes[i, 1].axis('off')

    plt.tight_layout()
    plt.show()

def main():
    cnn_file = 'cnn_confusion.csv'
    svc_file = 'svc_confusion_matrix.csv'
    threshold = 9  # Adjust this value to set the threshold for ignoring low misclassifications
    image_dir = r'C:\Users\jerem\imageplayground\archive'  # Update this with the path to your image directory

    cnn_matrix = read_confusion_matrix(cnn_file)
    svc_matrix = read_confusion_matrix(svc_file)

    cnn_only, svc_only, both = analyze_misclassifications(cnn_matrix, svc_matrix, threshold)

    print(f"Misclassifications only in CNN (threshold > {threshold}):")
    for true, pred in cnn_only:
        print(f"  True: {true}, Predicted: {pred}")
    plot_misclassifications(cnn_only, image_dir, "")

    print(f"\nMisclassifications only in SVC (threshold > {threshold}):")
    for true, pred in svc_only:
        print(f"  True: {true}, Predicted: {pred}")
    plot_misclassifications(svc_only, image_dir, "")

    print(f"\nMisclassifications in both CNN and SVC (threshold > {threshold}):")
    for true, pred in both:
        print(f"  True: {true}, Predicted: {pred}")
    plot_misclassifications(both, image_dir, "")

if __name__ == "__main__":
    main()