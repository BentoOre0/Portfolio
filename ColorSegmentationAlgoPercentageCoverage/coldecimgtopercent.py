import cv2
import numpy as np
from sklearn.cluster import KMeans
import os
#copilot, stack overflow used
root_dir = r"C:\Users\jerem\PycharmProjects\ColorSegmentationAlgoPercentageCoverage\Chemistry Data"

#change path
for dir_name, _, file_names in os.walk(root_dir): #brute force all file paths
    for file_name in file_names:
        if file_name.endswith(('.png', '.jpeg', '.jpg')): #file types
            file_path = os.path.join(dir_name, file_name)
            image = cv2.imread(file_path)
            # Resize the image
            scale_percent = 30 # percent of original size
            width = int(image.shape[1] * scale_percent / 100)
            height = int(image.shape[0] * scale_percent / 100)
            dim = (width, height)
            resized = cv2.resize(image, dim, interpolation = cv2.INTER_AREA)


            #Gaussian Filter -> Break Ties, Remove Petri Dish, so it blends with bckground
            resized = cv2.GaussianBlur(resized, (3, 3), 1)
            #Greedy Optimisation - I already know the colours in the end, we move the clusters inwards
            white = np.array([255, 255, 255])
            black = np.array([0, 0, 0])
            yellow = np.array([0, 255, 255])

            for i in range(resized.shape[0]):
                for j in range(resized.shape[1]):
                    #for each pixel, move it closer to the eucliden distance,
                    # This shouldn't impact the "majority colour" from k means neighbours
                    color = resized[i, j]
                    #Euclidean Distance Squared
                    dist_white = np.sum((color - white)**2)
                    dist_black = np.sum((color - black)**2)
                    dist_yellow = np.sum((color - yellow)**2)

                    min_dist = min(dist_white, dist_black, dist_yellow)

                    # Weigthing
                    if min_dist == dist_yellow:
                        midpoint_color = (color + yellow) // 2
                    elif min_dist == dist_white:
                        midpoint_color = (color + white) // 2
                    else:
                        midpoint_color = (color + black) // 2

                    resized[i, j] = midpoint_color

            # Reshape the image to be a list of pixels
            pixels = resized.reshape(-1, 3)

            kmeans = KMeans(n_clusters=3) #get model with 3 clusters
            kmeans.fit(pixels) #do kmeans algorithm to find majority colours
            segmented_image = kmeans.cluster_centers_[kmeans.labels_] #adjust to optimum cluster center
            segmented_image = segmented_image.reshape(resized.shape) #scale back
            segmented_image = np.uint8(segmented_image) #back to rgb values (8bit integers)
            # segmented_image = np.uint8(segmented_image)

            flattened = segmented_image.reshape(-1,3) #Flatten again
            unique_colors, counts = np.unique(flattened, axis=0, return_counts=True) #frequency table

            color_frequency = {tuple(color): count for color, count in zip(unique_colors, counts)} #dictionary
            #filtering greedily
            white = [255, 255, 255]
            distances_to_white = np.sqrt(np.sum((unique_colors - white)**2, axis=1))
            closest_color_to_white = unique_colors[np.argmin(distances_to_white)] #does something lol
            del color_frequency[tuple(closest_color_to_white)] #delete background colour
            yellow = [0, 255, 255]
            distances_to_yellow = np.sqrt(np.sum((np.array(list(color_frequency.keys())) - yellow)**2, axis=1))
            closest_color_to_yellow = list(color_frequency.keys())[np.argmin(distances_to_yellow)]


            YELLOW = color_frequency[closest_color_to_yellow]
            del color_frequency[closest_color_to_yellow]
            closest_color_to_brown = None
            for elem in color_frequency:
                closest_color_to_brown = elem

            BROWN = color_frequency[closest_color_to_brown]

            modded_file_path = os.path.splitext(file_path)[0] + "_modded" + os.path.splitext(file_path)[1]

            #potentially interesting values from "closest_to_yellow" and brown, that's the median colour in a sense

            print(modded_file_path)
            print(YELLOW, BROWN)
            print(f"Percentage Brown {(BROWN / (YELLOW + BROWN)) * 100:.2f}%")
            cv2.imwrite(modded_file_path, segmented_image)

            cv2.imshow('image', segmented_image)
            cv2.waitKey(0)
            cv2.destroyAllWindows()