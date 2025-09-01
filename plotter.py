#@author is Claude 3.5 Sonnet
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd

# List of Baybayin character labels
baybayin_labels = ['a', 'b', 'ba', 'be_bi', 'bo_bu', 'd', 'da_ra', 'de_di', 'do_du', 'e_i', 'g', 'ga', 'ge_gi', 'go_gu',
                   'h', 'ha', 'he_hi', 'ho_hu', 'k', 'ka', 'ke_ki', 'ko_ku', 'l', 'la', 'le_li', 'lo_lu', 'm', 'ma',
                   'me_mi', 'mo_mu', 'n', 'na', 'ne_ni', 'ng', 'nga', 'nge_ngi', 'ngo_ngu', 'no_nu', 'o_u', 'p', 'pa',
                   'pe_pi', 'po_pu', 'r', 'ra', 're_ri', 'ro_ru', 's', 'sa', 'se_si', 'so_su', 't', 'ta', 'te_ti',
                   'to_tu', 'w', 'wa', 'we_wi', 'wo_wu', 'y', 'ya', 'ye_yi', 'yo_yu']

# Load the confusion matrix from the CSV file
df_cm = pd.read_csv("cnn_confusion.csv", index_col=0)

# Ensure the DataFrame has the correct labels
df_cm.index = baybayin_labels
df_cm.columns = baybayin_labels

# Set up the matplotlib figure
plt.figure(figsize=(24, 20))

# Create the heatmap
sns.heatmap(df_cm, annot=True, fmt='.0f', cmap="YlGnBu", square=True, cbar=False)

# Modify the plot
plt.title('Confusion Matrix for CNN Baybayin Character Recognition', fontsize=20)
plt.xlabel('Predicted Label', fontsize=16)
plt.ylabel('True Label', fontsize=16)

# Rotate the tick labels and set their alignment
plt.xticks(rotation=90, ha='center', fontsize=8)
plt.yticks(rotation=0, ha='right', fontsize=8)

# Adjust the layout and save the figure
plt.tight_layout()
plt.savefig("cnn_confusion_matrix.png", dpi=300, bbox_inches="tight")
plt.show()

print("Confusion matrix plot saved as 'cnn_confusion_matrix.png'")