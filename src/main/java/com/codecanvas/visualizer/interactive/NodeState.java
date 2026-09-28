package com.codecanvas.visualizer.interactive;

/**
 * State of a graph vertex or array element during simulation.
 */
public enum NodeState {
    DEFAULT,     // Default unvisited (Blue / Slate)
    ACTIVE,      // Currently exploring / active queue head / inspecting (Orange / Amber)
    VISITED,     // Settled shortest path / visited in traversal / confirmed in MST / sorted (Green)
    DISCARDED    // Cycle detected / rejected edge / pivot / conflict (Red)
}
