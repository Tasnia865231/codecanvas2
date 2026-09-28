package com.codecanvas.visualizer.interactive;

/**
 * State of a graph edge during execution.
 */
public enum EdgeState {
    DEFAULT,            // Unvisited default edge (Slate/Gray)
    EXPLORING,          // Currently active / traversing (Orange/Amber)
    SELECTED_MST,       // Tree edge in MST or shortest path relaxation (Green)
    DISCARDED_CYCLE     // Rejected edge / creates cycle (Red)
}
