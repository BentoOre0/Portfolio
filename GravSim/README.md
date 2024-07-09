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
