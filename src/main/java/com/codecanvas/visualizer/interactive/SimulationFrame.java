package com.codecanvas.visualizer.interactive;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates an immutable visual snapshot of an algorithm execution step.
 */
public class SimulationFrame {

    private int stepNumber;
    private int totalSteps;
    private String title;
    private String explanation;
    private String auxStructureText = "";

    // Graph state
    private Map<Integer, NodeState> nodeStates = new HashMap<>();
    private Map<Integer, String> nodeSubLabels = new HashMap<>();
    private Map<String, EdgeState> edgeStates = new HashMap<>();

    // Array / Sorting state
    private boolean arrayVisualization = false;
    private int[] arrayData;
    private Map<Integer, NodeState> arrayElementStates = new HashMap<>();
    private int pointerI = -1;
    private int pointerJ = -1;
    private int pointerPivot = -1;

    public SimulationFrame(int stepNumber, String title, String explanation) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.explanation = explanation;
    }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getAuxStructureText() { return auxStructureText; }
    public void setAuxStructureText(String auxStructureText) { this.auxStructureText = auxStructureText; }

    public Map<Integer, NodeState> getNodeStates() { return nodeStates; }
    public void setNodeStates(Map<Integer, NodeState> nodeStates) { this.nodeStates = nodeStates; }

    public Map<Integer, String> getNodeSubLabels() { return nodeSubLabels; }
    public void setNodeSubLabels(Map<Integer, String> nodeSubLabels) { this.nodeSubLabels = nodeSubLabels; }

    public Map<String, EdgeState> getEdgeStates() { return edgeStates; }
    public void setEdgeStates(Map<String, EdgeState> edgeStates) { this.edgeStates = edgeStates; }

    public boolean isArrayVisualization() { return arrayVisualization; }
    public void setArrayVisualization(boolean arrayVisualization) { this.arrayVisualization = arrayVisualization; }

    public int[] getArrayData() { return arrayData; }
    public void setArrayData(int[] arrayData) { this.arrayData = arrayData; }

    public Map<Integer, NodeState> getArrayElementStates() { return arrayElementStates; }
    public void setArrayElementStates(Map<Integer, NodeState> arrayElementStates) { this.arrayElementStates = arrayElementStates; }

    public int getPointerI() { return pointerI; }
    public void setPointerI(int pointerI) { this.pointerI = pointerI; }

    public int getPointerJ() { return pointerJ; }
    public void setPointerJ(int pointerJ) { this.pointerJ = pointerJ; }

    public int getPointerPivot() { return pointerPivot; }
    public void setPointerPivot(int pointerPivot) { this.pointerPivot = pointerPivot; }
}
