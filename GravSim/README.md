# 🌌 2D Gravity Simulation Project

## 🚀 Introduction
I decided to take a break from competitive programming as I wanted to try something more creative. My interest in combining math and physics led me to particle simulation, which eventually evolved into the simulation of planetary systems inspired by the n-body problem.

## 🔍 Problem Statement
Simulating gravitational interactions between particles involves calculating forces between each pair of particles, leading to O(n^2) complexity. This approach was too slow and could handle no more than a few hundred bodies without performance issues.

## 🛠️ Solution: Barnes-Hut Approximation
The Barnes-Hut algorithm optimizes the simulation by reducing the computational complexity. The key idea is that groups of distant bodies can be approximated by their total mass at the position of their center of mass, significantly reducing the number of calculations needed.

### How It Works
- **Quadtree Structure**: Instead of checking each planet against all others, the space is divided into four quadrants recursively. This quadtree structure helps in grouping distant bodies and approximating them as a single point mass.
- **Force Calculation**: For each particle, forces from nearby particles are calculated directly, while distant particles are approximated.
- **UI**: This done using tkinter and pygames directly.

### User Controls for `main.py`

1. **Mouse Controls:**
   - **Left Click (MOUSEBUTTONDOWN, button 1):**
     - Create a new planet at the mouse position, enters creation mode.
   - **Right Click (MOUSEBUTTONDOWN, button 3):**
     - Create a new planet at the mouse position with random orbital velocity around an existing planet.
   - **Mouse Wheel Scroll (MOUSEBUTTONDOWN, button 4 or 5):**
     - Increase (button 4) or decrease (button 5) the radius of the new planet during creation mode.

2. **Keyboard Controls:**
   - **`r`:** 
     - Toggle resizing mode for the new planet in creation mode.
   - **`g`:**
     - Generate a specified number of random planets.
   - **`e`:**
     - Erase all planets from the simulation.
   - **`f`:**
     - Change the maximum size of randomly generated planets.
   - **`d`:**
     - Toggle the drawing mode for quadtree bounds.
   - **`t`:**
     - Change the Barnes-Hut coefficient θ (affects simulation accuracy and speed). This determines whether something is "far enough".
   - **`s`:**
     - Spin a specified number of bodies in a galaxy around the mouse position.
   - **`x`:**
     - Change the galaxy's radius and the number of bodies in the galaxy.

3. **Dialog Prompts:**
   - **Max Size of Random Planets:**
     - Prompted by pressing `f`.
   - **Number of Planets to Generate:**
     - Prompted by pressing `g`.
   - **Barnes-Hut Coefficient θ:**
     - Prompted by pressing `t`.
   - **Galaxy's Radius:**
     - Prompted by pressing `x`.
   - **Number of Bodies in the Galaxy:**
     - Prompted by pressing `x`.
