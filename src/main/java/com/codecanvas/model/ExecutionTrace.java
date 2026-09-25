package com.codecanvas.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents one recorded algorithm execution (e.g. a sort or search run),
 * either persisted locally in SQLite or fetched remotely as JSON.
 */
public class ExecutionTrace {

    private final IntegerProperty traceId = new SimpleIntegerProperty(this, "traceId");
    private final StringProperty algorithmName = new SimpleStringProperty(this, "algorithmName");
    private final IntegerProperty comparisons = new SimpleIntegerProperty(this, "comparisons");
    private final StringProperty status = new SimpleStringProperty(this, "status");

    public ExecutionTrace() {
    }

    public ExecutionTrace(int traceId, String algorithmName, int comparisons, String status) {
        setTraceId(traceId);
        setAlgorithmName(algorithmName);
        setComparisons(comparisons);
        setStatus(status);
    }

    public int getTraceId() { return traceId.get(); }
    public void setTraceId(int v) { traceId.set(v); }
    public IntegerProperty traceIdProperty() { return traceId; }

    public String getAlgorithmName() { return algorithmName.get(); }
    public void setAlgorithmName(String v) { algorithmName.set(v); }
    public StringProperty algorithmNameProperty() { return algorithmName; }

    public int getComparisons() { return comparisons.get(); }
    public void setComparisons(int v) { comparisons.set(v); }
    public IntegerProperty comparisonsProperty() { return comparisons; }

    public String getStatus() { return status.get(); }
    public void setStatus(String v) { status.set(v); }
    public StringProperty statusProperty() { return status; }

    @Override
    public String toString() {
        return algorithmName.get() + " [" + comparisons.get() + " ops] - " + status.get();
    }
}
