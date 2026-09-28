package com.codecanvas.visualizer.interactive;

/**
 * Represents a weighted edge between two nodes on the interactive canvas.
 */
public class VisualGraphEdge {

    private int u;
    private int v;
    private int weight;
    private boolean directed;
    private EdgeState state = EdgeState.DEFAULT;

    public VisualGraphEdge(int u, int v, int weight, boolean directed) {
        this.u = u;
        this.v = v;
        this.weight = weight;
        this.directed = directed;
    }

    public String getKey() {
        return directed ? (u + "->" + v) : (Math.min(u, v) + "-" + Math.max(u, v));
    }

    public int getU() { return u; }
    public void setU(int u) { this.u = u; }

    public int getV() { return v; }
    public void setV(int v) { this.v = v; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public boolean isDirected() { return directed; }
    public void setDirected(boolean directed) { this.directed = directed; }

    public EdgeState getState() { return state; }
    public void setState(EdgeState state) { this.state = state; }
}
