## Percentage Coverage Analysis Project

### Overview
The **Percentage Coverage Analysis** project aims to determine the percentage coverage of the "brown layer" on bananas over time and see how this is affected by different preservatives. This was created as part of a wider science project related to chemistry.

### Data Sources
- **Photos:** Taken personally to document the changes in the banana's brown layer over time.

### Methodologies
Our analysis employs several methodologies:
1. **Image Processing:** Using OpenCV for image reading and resizing.
2. **Gaussian Filtering:** To remove noise caused by the petri dish and other packs, ensuring consistent methodology without manual photo editing.
3. **K-Means Clustering:** An AI algorithm to identify and segment the key colors in the images. (3 clusters since only 3 major clusters background, yellow and brown).
4. **Color Distance Calculation:** To classify and filter out the background and target the relevant brown and yellow layers.

### Key Metrics
- **Coverage Percentage:** The ratio of the brown area to the total area of the brown and yellow layers.
- **Color Frequency:** The distribution of key colors within the images.

### Data Preprocessing
Data preprocessing involves:
- **Blurring:** Applying a Gaussian filter to smooth out the images.
- **Color Adjustment:** Moving each pixel closer to predefined colors (white, black, yellow) to enhance clustering accuracy.

### Tools and Technologies
We utilize a range of tools and technologies, including:
- **Python and OpenCV:** For image processing.
- **Scikit-learn:** For K-Means clustering.
- **NumPy:** For numerical operations and data manipulation.

### Example Analysis
An example analysis includes reading an image, resizing it, applying Gaussian filtering, performing K-Means clustering, and calculating the percentage of the brown layer.

### Challenges and Solutions
**Challenge:** Handling noise and inconsistencies in images due to the petri dish and other factors.
**Solution:** Implementing Gaussian filtering to create a consistent preprocessing methodology.
**Challenge:** Not familiar with data analytics tools
**Solution:** Use AI to draft code and look at documentation for appropriate libraries.

### Applications
The findings from our analysis can be applied in:
- **Scientific Research:** Studying the effect of different preservatives on banana browning.
- **Further Research:** Extending the methodology to analyze other fruits or conditions.

### Future Improvements
Next steps include:
- **Using Neural Networks:** To potentially enhance accuracy and consistency compared to clustering algorithms and blurs.


