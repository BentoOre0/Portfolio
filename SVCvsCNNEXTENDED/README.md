# SVC vs CNN for Baybayin Character Recognition

A comparative machine learning study investigating how **Support Vector Classifiers (SVCs)** and **Convolutional Neural Networks (CNNs)** perform on handwritten Baybayin character recognition under image distortions.

This project was completed as my **IB Extended Essay in Computer Science**, where I designed experiments to evaluate model robustness to **rotation**, **salt-and-pepper noise**, and **clean inputs** using a 63-class Baybayin dataset from [Kaggle](https://www.kaggle.com/datasets/jamesnogra/baybayn-baybayin-handwritten-images).

## Highlights

* Compared classical machine learning (SVC) against deep learning (CNN)
* Generated controlled datasets with varying rotation angles and noise intensities
* Evaluated models using accuracy, precision, recall, F1-score, and confusion matrices
* Implemented the full experimentation pipeline in Python using **scikit-learn** and **TensorFlow/Keras**

## Key Findings

* **SVC outperformed CNNs on clean images and rotated characters.**
* **CNNs were more robust to increasing image noise.**
* Both models degraded significantly under large rotations, highlighting limitations of the training dataset and feature extraction methods.

## Technologies

`Python` • `TensorFlow/Keras` • `scikit-learn` • `NumPy` • `Pandas` • `Pillow` • `Matplotlib`

## Repository

```
CNN.py            CNN implementation
SVC.py            Support Vector Classifier
Comparison.py     Model comparison experiments
rotate.py         Rotation dataset generation
blur.py           Noise generation
plotter.py        Visualisation utilities
processing.py     Dataset preprocessing
```

## Paper

The complete methodology, experimental design, statistical analysis, discussion, and conclusions are documented in my paper **[here](https://drive.google.com/file/d/1Tz5_n4KKLvXJk2pD9c-VNwKGnvGy7cFN/view?usp=sharing)**.

> **Abstract:** This investigation compares Support Vector Classifiers and Convolutional Neural Networks for Baybayin character recognition under varying levels of rotation and noise. While CNNs are generally regarded as the dominant approach for image classification, this study demonstrates that classical machine learning methods can outperform deep learning models on structured, limited datasets under certain conditions, while CNNs exhibit greater robustness to random image noise.
