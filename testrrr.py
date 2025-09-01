import numpy as np
import pandas as pd
from scipy import stats
from scipy.optimize import curve_fit
import matplotlib.pyplot as plt
from sklearn.metrics import r2_score
from sklearn.preprocessing import PolynomialFeatures
from sklearn.linear_model import LinearRegression

# Load the data
data = pd.read_csv('chem_data_surfacearea.csv')
x = data['Surface Area (m^2)'].values
y = data['Mass Change (g)'].values

# Define functions for different curve fits
def linear(x, a, b):
    return a * x + b

def quadratic(x, a, b, c):
    return a * x**2 + b * x + c

def cubic(x, a, b, c, d):
    return a * x**3 + b * x**2 + c * x + d

def exponential(x, a, b):
    return a * np.exp(b * x)

def logarithmic(x, a, b):
    return a * np.log(x) + b

def power_series(x, a, b):
    return a * x**b

def logistic(x, L, k, x0):
    return L / (1 + np.exp(-k * (x - x0)))

# Fit curves and calculate R²
curves = {
    'Linear': (linear, stats.linregress(x, y)),
    'Quadratic': (quadratic, np.polyfit(x, y, 2)),
    'Cubic': (cubic, np.polyfit(x, y, 3)),
    'Exponential': (exponential, curve_fit(exponential, x, y)[0]),
    'Logarithmic': (logarithmic, curve_fit(logarithmic, x, y)[0]),
    'Power Series': (power_series, curve_fit(power_series, x, y)[0]),
    'Logistic': (logistic, curve_fit(logistic, x, y, p0=[max(y), 1, np.median(x)])[0])
}

r_squared = {}
for name, (func, params) in curves.items():
    if name == 'Linear':
        y_pred = func(x, *params[:2])
    elif name in ['Quadratic', 'Cubic']:
        y_pred = np.polyval(params, x)
    else:
        y_pred = func(x, *params)
    r_squared[name] = r2_score(y, y_pred)

# Find the best fit
best_fit = max(r_squared, key=r_squared.get)

print("R² values for different curves:")
for name, r2 in r_squared.items():
    print(f"{name}: {r2:.4f}")

print(f"\nBest fit: {best_fit} (R² = {r_squared[best_fit]:.4f})")

# Get the equation for the best fit
best_func, best_params = curves[best_fit]
if best_fit == 'Linear':
    a, b, _, _, _ = best_params
    equation = f"y = {a:.4f}x + {b:.4f}"
elif best_fit == 'Quadratic':
    a, b, c = best_params
    equation = f"y = {a:.4f}x² + {b:.4f}x + {c:.4f}"
elif best_fit == 'Cubic':
    a, b, c, d = best_params
    equation = f"y = {a:.4f}x³ + {b:.4f}x² + {c:.4f}x + {d:.4f}"
elif best_fit == 'Exponential':
    a, b = best_params
    equation = f"y = {a:.4f} * e^({b:.4f}x)"
elif best_fit == 'Logarithmic':
    a, b = best_params
    equation = f"y = {a:.4f} * ln(x) + {b:.4f}"
elif best_fit == 'Power Series':
    a, b = best_params
    equation = f"y = {a:.4f} * x^{b:.4f}"
else:  # Logistic
    L, k, x0 = best_params
    equation = f"y = {L:.4f} / (1 + e^(-{k:.4f} * (x - {x0:.4f})))"

print(f"Equation of best fit: {equation}")

# Plot the data and the best-fit curve
plt.figure(figsize=(10, 6))
plt.scatter(x, y, label='Data')
x_smooth = np.linspace(min(x), max(x), 200)
if best_fit in ['Quadratic', 'Cubic']:
    y_smooth = np.polyval(best_params, x_smooth)
else:
    y_smooth = best_func(x_smooth, *best_params)
plt.plot(x_smooth, y_smooth, 'r-', label=f'Best fit ({best_fit})')
plt.xlabel('Surface Area (m²)')
plt.ylabel('Mass Change (g)')
plt.title('Surface Area vs Mass Change')
plt.legend()
plt.grid(True)
plt.show()