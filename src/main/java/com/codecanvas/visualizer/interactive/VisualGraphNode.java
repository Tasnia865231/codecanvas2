package com.codecanvas.visualizer.interactive;

/**
 * Represents a draggable, stateful node on the VisuAlgo canvas.
 */
public class VisualGraphNode {

    private int id;
    private String label;
    private double x;
    private double y;
    private double radius = 22.0;
    private NodeState state = NodeState.DEFAULT;
    private String subLabel = "";

    public VisualGraphNode(int id, String label, double x, double y) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
    }

    public boolean contains(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return (dx * dx + dy * dy) <= (radius + 6) * (radius + 6);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public NodeState getState() { return state; }
    public void setState(NodeState state) { this.state = state; }

    public String getSubLabel() { return subLabel; }
    public void setSubLabel(String subLabel) { this.subLabel = subLabel; }
}
