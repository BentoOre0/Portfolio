from PIL import Image
import numpy as np
import os

def rotate_baybayin(input_path, output_path, angle):
    try:
        with Image.open(input_path) as img:
            img_array = np.array(img)
            if len(img_array.shape) == 2:
                height, width = img_array.shape
                channels = 1
            else:
                height, width, channels = img_array.shape
            diagonal = int(np.ceil(np.sqrt(width ** 2 + height ** 2)))
            pad_size = (diagonal - width) // 2
            if channels == 1:
                padded_img = np.pad(img_array, pad_size, constant_values=255)
            else:
                padded_img = np.pad(img_array, ((pad_size, pad_size), (pad_size, pad_size), (0, 0)),
                                    constant_values=255)
            rotated_padded = Image.fromarray(padded_img).rotate(angle, resample=Image.BICUBIC, expand=False)
            rotated_cropped = rotated_padded.crop((pad_size, pad_size, pad_size + width, pad_size + height))
            rotated_cropped.save(output_path)
    except Exception as e:
        print(f"Error processing {input_path}: {str(e)}")

def process_folder(input_folder, rotation_angles):
    parent_dir = os.path.dirname(input_folder)
    input_folder_name = os.path.basename(input_folder)

    for angle in rotation_angles:
        if angle < 0:
            angle_label = f"neg_{abs(angle)}"
        else:
            angle_label = f"pos_{angle}"

        output_dir_name = f"{input_folder_name}_rotated_{angle_label}"
        base_output_dir = os.path.join(parent_dir, output_dir_name)

        if not os.path.exists(base_output_dir):
            os.makedirs(base_output_dir)

        for file_name in os.listdir(input_folder):
            input_file_path = os.path.join(input_folder, file_name)

            # Skip directories
            if os.path.isdir(input_file_path):
                continue

            # Keep the original file name and extension
            output_file_path = os.path.join(base_output_dir, file_name)

            try:
                rotate_baybayin(input_file_path, output_file_path, angle)
                print(f"Rotated {file_name} by {angle} degrees and saved to {output_file_path}")
            except Exception as e:
                print(f"Error processing {file_name}: {str(e)}")

# Usage example
file_path = r"C:\Users\jerem\imageplayground\archive"
# rotation_angles = [-15, -10, -5, 0, 5, 10, 15] + [-30,-25,-20,20,25,30]
# rotation_angles =
# rotation_angles = [-90,-85,-80,-75,-70,-65,-60,60,65,70,75,70,85,90]
rotation_angles = [80]
try:
    process_folder(file_path, rotation_angles)
except Exception as e:
    print(f"An error occurred: {str(e)}")