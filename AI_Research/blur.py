import os
import numpy as np
from PIL import Image
import random

def add_bw_noise(file_path, intensity=1, seed=420):
    if seed is not None:
        random.seed(seed)
        np.random.seed(seed)
    img = Image.open(file_path).convert('L')  # Convert to grayscale
    img_array = np.array(img)
    noise = np.random.randint(0, 256, size=img_array.shape, dtype=np.uint8)
    mask = np.random.random(size=img_array.shape) < intensity
    img_array[mask] = noise[mask]
    noisy_img = Image.fromarray(img_array)
    return noisy_img

def process_folder(input_folder, noise_intensities):
    parent_dir = os.path.dirname(input_folder)
    input_folder_name = os.path.basename(input_folder)

    for noise in noise_intensities:
        output_dir_name = f"{input_folder_name}_added_{noise:.2f}"
        base_output_dir = os.path.join(parent_dir, output_dir_name)
        os.makedirs(base_output_dir, exist_ok=True)
        for file_name in os.listdir(input_folder):
            input_file_path = os.path.join(input_folder, file_name)
            if os.path.isdir(input_file_path):
                continue
            output_file_path = os.path.join(base_output_dir, file_name)
            try:
                noisy_image = add_bw_noise(input_file_path, noise)
                noisy_image.save(output_file_path)
                print(f"Noise added to {file_name} with factor {noise:.2f} and saved to {output_file_path}")
            except Exception as e:
                print(f"Error processing {file_name}: {str(e)}")

if __name__ == "__main__":
    file_path = r"C:\Users\jerem\imageplayground\archive"
    noise_intensities = list(map(lambda x: x/100, list(range(0,105,5))))
    process_folder(file_path, noise_intensities)