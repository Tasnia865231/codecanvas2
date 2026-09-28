package com.codecanvas.visualizer.interactive;

/**
 * Output layout types supported by the VisuAlgo canvas visualizer.
 */
public enum GraphLayoutType {
    DEFAULT,    // Circular / balanced geometric layout
    BIPARTITE,  // Two-column bipartite partition
    TREE,       // Hierarchical top-to-bottom tree layout
    DAG         // Left-to-right layered topological layout
}
