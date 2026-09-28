package com.codecanvas.visualizer.interactive;

/**
 * Format of the user custom input structure.
 */
public enum GraphInputType {
    EDGE_LIST,          // u v w per line
    ADJACENCY_MATRIX,   // Matrix of row weights
    ADJACENCY_LIST      // u: v(w) or u -> v(w)
}
