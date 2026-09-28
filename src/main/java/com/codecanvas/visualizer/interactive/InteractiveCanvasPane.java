package com.codecanvas.visualizer.interactive;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Modern VisuAlgo-style interactive canvas visualizer component.
 * Supports dynamic graph node dragging, smooth vector rendering of nodes,
 * weighted edges, arrows, distance pills, and bar charts for sorting algorithms.
 */
public class InteractiveCanvasPane extends Pane {

    private final Canvas canvas = new Canvas();
    private InteractiveGraphModel graphModel;
    private SimulationFrame currentFrame;

    private VisualGraphNode draggedNode = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    // Palette
    private static final Color COLOR_DEFAULT = Color.web("#2563eb");     // Royal Blue
    private static final Color COLOR_ACTIVE = Color.web("#f59e0b");      // Amber Gold
    private static final Color COLOR_VISITED = Color.web("#10b981");     // Emerald Green
    private static final Color COLOR_DISCARDED = Color.web("#ef4444");   // Crimson Red
    private static final Color COLOR_EDGE_DEFAULT = Color.web("#64748b");// Slate Edge
    private static final Color COLOR_BG = Color.web("#0f172a");          // Dark Slate Canvas BG

    public InteractiveCanvasPane() {
        getChildren().add(canvas);
        setStyle("-fx-background-color: #0f172a; -fx-background-radius: 8; -fx-border-color: #334155; -fx-border-radius: 8;");

        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        widthProperty().addListener((obs, o, n) -> redraw());
        heightProperty().addListener((obs, o, n) -> redraw());

        setupMouseInteractions();
    }

    private void setupMouseInteractions() {
        setOnMousePressed(e -> {
            if (graphModel == null || (currentFrame != null && currentFrame.isArrayVisualization())) return;
            double mx = e.getX();
            double my = e.getY();
            for (VisualGraphNode node : graphModel.getNodes()) {
                if (node.contains(mx, my)) {
                    draggedNode = node;
                    dragOffsetX = node.getX() - mx;
                    dragOffsetY = node.getY() - my;
                    break;
                }
            }
        });

        setOnMouseDragged(e -> {
            if (draggedNode != null) {
                double newX = Math.max(30, Math.min(getWidth() - 30, e.getX() + dragOffsetX));
                double newY = Math.max(30, Math.min(getHeight() - 30, e.getY() + dragOffsetY));
                draggedNode.setX(newX);
                draggedNode.setY(newY);
                redraw();
            }
        });

        setOnMouseReleased(e -> draggedNode = null);
    }

    public void setGraphModel(InteractiveGraphModel model) {
        this.graphModel = model;
        if (graphModel != null && getWidth() > 0 && getHeight() > 0) {
            graphModel.computeLayout(getWidth(), getHeight());
        }
        redraw();
    }

    public void setSimulationFrame(SimulationFrame frame) {
        this.currentFrame = frame;
        redraw();
    }

    public void redraw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        // Dark background with subtle grid dots
        gc.setFill(COLOR_BG);
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#1e293b", 0.5));
        for (double gx = 20; gx < w; gx += 40) {
            for (double gy = 20; gy < h; gy += 40) {
                gc.fillOval(gx, gy, 2, 2);
            }
        }

        if (currentFrame != null && currentFrame.isArrayVisualization()) {
            drawArrayVisualization(gc, w, h);
            drawArrayLegend(gc, w, h);
            return;
        }

        if (graphModel != null) {
            drawGraphVisualization(gc, w, h);
            drawGraphLegend(gc, w, h);
        }
    }

    // ============================================================ GRAPH RENDERING

    private void drawGraphVisualization(GraphicsContext gc, double w, double h) {
        List<VisualGraphNode> nodes = graphModel.getNodes();
        if (nodes.isEmpty()) return;

        // Ensure nodes have initialized coordinates
        boolean needsLayout = nodes.stream().allMatch(n -> n.getX() == 0 && n.getY() == 0);
        if (needsLayout) {
            graphModel.computeLayout(w, h);
        }

        Map<String, EdgeState> eStates = currentFrame != null ? currentFrame.getEdgeStates() : null;
        Map<Integer, NodeState> nStates = currentFrame != null ? currentFrame.getNodeStates() : null;
        Map<Integer, String> nLabels = currentFrame != null ? currentFrame.getNodeSubLabels() : null;

        // 1. Draw Edges
        for (VisualGraphEdge edge : graphModel.getEdges()) {
            VisualGraphNode uNode = graphModel.getNodeById(edge.getU());
            VisualGraphNode vNode = graphModel.getNodeById(edge.getV());
            if (uNode == null || vNode == null) continue;

            EdgeState state = EdgeState.DEFAULT;
            if (eStates != null) {
                state = eStates.getOrDefault(edge.getKey(), EdgeState.DEFAULT);
            }

            boolean isBidirectional = edge.isDirected() && (edge.getU() != edge.getV()) && graphModel.hasDirectedEdge(edge.getV(), edge.getU());
            drawEdge(gc, uNode, vNode, edge.getWeight(), edge.isDirected(), state, isBidirectional);
        }

        // 2. Draw Nodes
        for (VisualGraphNode node : nodes) {
            NodeState state = NodeState.DEFAULT;
            if (nStates != null && nStates.containsKey(node.getId())) {
                state = nStates.get(node.getId());
            }

            String subLabel = node.getSubLabel();
            if (nLabels != null && nLabels.containsKey(node.getId())) {
                subLabel = nLabels.get(node.getId());
            }

            drawNode(gc, node, state, subLabel);
        }
    }

    private void drawEdge(GraphicsContext gc, VisualGraphNode u, VisualGraphNode v, int weight, boolean directed, EdgeState state, boolean isBidirectional) {
        double x1 = u.getX(), y1 = u.getY();
        double x2 = v.getX(), y2 = v.getY();

        Color strokeColor = switch (state) {
            case EXPLORING -> COLOR_ACTIVE;
            case SELECTED_MST -> COLOR_VISITED;
            case DISCARDED_CYCLE -> COLOR_DISCARDED;
            default -> COLOR_EDGE_DEFAULT;
        };

        double lineWidth = (state == EdgeState.SELECTED_MST || state == EdgeState.EXPLORING) ? 3.5 : 2.0;

        gc.save();
        gc.setStroke(strokeColor);
        gc.setLineWidth(lineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);

        if (state == EdgeState.DISCARDED_CYCLE) {
            gc.setLineDashes(6.0, 4.0);
        }

        // Calculate distance and direction
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) {
            gc.restore();
            return;
        }

        double ux = dx / dist;
        double uy = dy / dist;

        if (isBidirectional) {
            // Antiparallel edges: curve using quadratic Bezier curve bowed to the right of (u -> v)
            // Normal perpendicular vector pointing to the right of direction of travel:
            double nx = -uy;
            double ny = ux;

            double curveOffset = Math.min(42.0, Math.max(26.0, dist * 0.22));

            double midX = (x1 + x2) / 2.0;
            double midY = (y1 + y2) / 2.0;

            // Control point for the quadratic Bezier curve
            double cx = midX + nx * curveOffset;
            double cy = midY + ny * curveOffset;

            // Boundary points for nodes
            double rU = u.getRadius();
            double rV = v.getRadius();

            double dirUx = cx - x1;
            double dirUy = cy - y1;
            double distU = Math.sqrt(dirUx * dirUx + dirUy * dirUy);
            double startX = distU > 0 ? (x1 + (dirUx / distU) * rU) : x1;
            double startY = distU > 0 ? (y1 + (dirUy / distU) * rU) : y1;

            double dirVx = x2 - cx;
            double dirVy = y2 - cy;
            double distV = Math.sqrt(dirVx * dirVx + dirVy * dirVy);
            double targetX = distV > 0 ? (x2 - (dirVx / distV) * rV) : x2;
            double targetY = distV > 0 ? (y2 - (dirVy / distV) * rV) : y2;

            // Draw curved quadratic Bezier path
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.quadraticCurveTo(cx, cy, targetX, targetY);
            gc.stroke();

            // Arrow head for directed edges offset along tangent entering targetX, targetY from cx, cy
            gc.setLineDashes((double[]) null);
            if (directed) {
                drawArrowHead(gc, cx, cy, targetX, targetY, strokeColor);
            }

            // Weight pill positioned at apex of the quadratic curve (t = 0.5)
            double peakX = 0.25 * startX + 0.5 * cx + 0.25 * targetX;
            double peakY = 0.25 * startY + 0.5 * cy + 0.25 * targetY;
            drawWeightPill(gc, weight, peakX, peakY, strokeColor);

        } else {
            // Straight edge
            double r = v.getRadius();
            double targetX = x2 - ux * r;
            double targetY = y2 - uy * r;

            gc.strokeLine(x1, y1, targetX, targetY);

            // Arrow head for directed edges
            gc.setLineDashes((double[]) null);
            if (directed) {
                drawArrowHead(gc, x1, y1, targetX, targetY, strokeColor);
            }

            // Weight pill in the center of the edge
            double midX = (x1 + x2) / 2.0;
            double midY = (y1 + y2) / 2.0;
            drawWeightPill(gc, weight, midX, midY, strokeColor);
        }

        gc.restore();
    }

    private void drawWeightPill(GraphicsContext gc, int weight, double midX, double midY, Color strokeColor) {
        String wText = String.valueOf(weight);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        double pillW = Math.max(22, wText.length() * 8 + 10);
        double pillH = 16;

        gc.setFill(Color.web("#1e293b"));
        gc.setStroke(strokeColor);
        gc.setLineWidth(1.2);
        gc.fillRoundRect(midX - pillW / 2.0, midY - pillH / 2.0, pillW, pillH, 6, 6);
        gc.strokeRoundRect(midX - pillW / 2.0, midY - pillH / 2.0, pillW, pillH, 6, 6);

        gc.setFill(Color.web("#f8fafc"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(wText, midX, midY);
    }

    private void drawArrowHead(GraphicsContext gc, double fromX, double fromY, double toX, double toY, Color color) {
        double arrowLength = 12.0;
        double arrowWidth = 7.0;

        double dx = toX - fromX;
        double dy = toY - fromY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) return;

        double ux = dx / dist;
        double uy = dy / dist;

        double baseX = toX - ux * arrowLength;
        double baseY = toY - uy * arrowLength;

        double leftX = baseX - uy * arrowWidth;
        double leftY = baseY + ux * arrowWidth;

        double rightX = baseX + uy * arrowWidth;
        double rightY = baseY - ux * arrowWidth;

        gc.setFill(color);
        gc.fillPolygon(new double[]{toX, leftX, rightX}, new double[]{toY, leftY, rightY}, 3);
    }

    private void drawNode(GraphicsContext gc, VisualGraphNode node, NodeState state, String subLabel) {
        double x = node.getX();
        double y = node.getY();
        double r = node.getRadius();

        Color fillColor = switch (state) {
            case ACTIVE -> COLOR_ACTIVE;
            case VISITED -> COLOR_VISITED;
            case DISCARDED -> COLOR_DISCARDED;
            default -> COLOR_DEFAULT;
        };

        gc.save();

        // Outer glow on active node
        if (state == NodeState.ACTIVE) {
            gc.setStroke(Color.web("#fef08a", 0.7));
            gc.setLineWidth(5);
            gc.strokeOval(x - r - 3, y - r - 3, (r + 3) * 2, (r + 3) * 2);
        }

        // Node fill
        gc.setFill(fillColor);
        gc.fillOval(x - r, y - r, r * 2, r * 2);

        // Node border
        gc.setStroke(Color.web("#f8fafc"));
        gc.setLineWidth(2.2);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);

        // Node label
        gc.setFill(Color.web("#ffffff"));
        gc.setFont(Font.font("Quicksand", FontWeight.EXTRA_BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(node.getLabel(), x, y);

        // Distance / State subLabel pill
        if (subLabel != null && !subLabel.isBlank()) {
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 10.5));
            double pillW = Math.max(34, subLabel.length() * 6.5 + 10);
            double pillH = 15;
            double pillY = y + r + 11;

            gc.setFill(Color.web("#0f172a", 0.9));
            gc.setStroke(fillColor);
            gc.setLineWidth(1.2);
            gc.fillRoundRect(x - pillW / 2.0, pillY - pillH / 2.0, pillW, pillH, 6, 6);
            gc.strokeRoundRect(x - pillW / 2.0, pillY - pillH / 2.0, pillW, pillH, 6, 6);

            gc.setFill(Color.web("#f8fafc"));
            gc.fillText(subLabel, x, pillY);
        }

        gc.restore();
    }

    private void drawGraphLegend(GraphicsContext gc, double w, double h) {
        gc.save();
        double startX = 14;
        double startY = 14;
        double barW = 390;
        double barH = 26;

        gc.setFill(Color.web("#1e293b", 0.85));
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1);
        gc.fillRoundRect(startX, startY, barW, barH, 8, 8);
        gc.strokeRoundRect(startX, startY, barW, barH, 8, 8);

        drawLegendItem(gc, startX + 10, startY + 13, COLOR_DEFAULT, "Unvisited");
        drawLegendItem(gc, startX + 90, startY + 13, COLOR_ACTIVE, "Active / Testing");
        drawLegendItem(gc, startX + 205, startY + 13, COLOR_VISITED, "Settled / MST");
        drawLegendItem(gc, startX + 310, startY + 13, COLOR_DISCARDED, "Cycle / Skipped");

        gc.restore();
    }

    private void drawLegendItem(GraphicsContext gc, double x, double y, Color color, String text) {
        gc.setFill(color);
        gc.fillOval(x, y - 5, 10, 10);
        gc.setFill(Color.web("#cbd5e1"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + 14, y);
    }

    // ============================================================ ARRAY RENDERING (SORTING & SEARCHING)

    private void drawArrayVisualization(GraphicsContext gc, double w, double h) {
        int[] arr = currentFrame.getArrayData();
        if (arr == null || arr.length == 0) return;

        int n = arr.length;
        int maxVal = Arrays.stream(arr).max().orElse(100);
        maxVal = Math.max(10, maxVal);

        double paddingX = 40.0;
        double availW = w - (paddingX * 2);
        double barSpacing = 8.0;
        double barW = Math.max(28.0, (availW - (n - 1) * barSpacing) / n);
        double maxBarH = h * 0.52;
        double baseY = h * 0.72;

        Map<Integer, NodeState> states = currentFrame.getArrayElementStates();

        for (int i = 0; i < n; i++) {
            double barH = Math.max(20.0, (arr[i] / (double) maxVal) * maxBarH);
            double x = paddingX + i * (barW + barSpacing);
            double y = baseY - barH;

            NodeState state = states.getOrDefault(i, NodeState.DEFAULT);
            Color barColor = switch (state) {
                case ACTIVE -> COLOR_ACTIVE;
                case VISITED -> COLOR_VISITED;
                case DISCARDED -> COLOR_DISCARDED;
                default -> COLOR_DEFAULT;
            };

            // Highlight pivot or comparing
            if (i == currentFrame.getPointerPivot()) {
                barColor = COLOR_DISCARDED;
            }

            gc.save();
            gc.setFill(barColor);
            gc.fillRoundRect(x, y, barW, barH, 6, 6);

            gc.setStroke(Color.web("#f8fafc"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(x, y, barW, barH, 6, 6);

            // Value label above or inside
            gc.setFill(Color.web("#ffffff"));
            gc.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.BOTTOM);
            gc.fillText(String.valueOf(arr[i]), x + barW / 2.0, y - 4);

            // Index label below
            gc.setFill(Color.web("#94a3b8"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setTextBaseline(VPos.TOP);
            gc.fillText("[" + i + "]", x + barW / 2.0, baseY + 6);

            // Pointer indicators (contextual for Binary Search vs Sorting)
            boolean isSearch = currentFrame.getTitle() != null && currentFrame.getTitle().toLowerCase().contains("search");
            if (i == currentFrame.getPointerI()) {
                drawPointerBadge(gc, x + barW / 2.0, baseY + 26, isSearch ? "▲ low" : "▲ i", Color.web("#38bdf8"));
            }
            if (i == currentFrame.getPointerJ()) {
                drawPointerBadge(gc, x + barW / 2.0, baseY + 44, isSearch ? "▲ high" : "▲ j", Color.web("#f59e0b"));
            }
            if (i == currentFrame.getPointerPivot()) {
                drawPointerBadge(gc, x + barW / 2.0, baseY + 62, isSearch ? "▲ mid" : "▲ pivot", Color.web("#ef4444"));
            }

            gc.restore();
        }
    }

    private void drawPointerBadge(GraphicsContext gc, double x, double y, String label, Color color) {
        gc.setFill(color);
        gc.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 10.5));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(label, x, y);
    }

    private void drawArrayLegend(GraphicsContext gc, double w, double h) {
        gc.save();
        double startX = 14;
        double startY = 14;
        double barW = 390;
        double barH = 26;

        gc.setFill(Color.web("#1e293b", 0.85));
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1);
        gc.fillRoundRect(startX, startY, barW, barH, 8, 8);
        gc.strokeRoundRect(startX, startY, barW, barH, 8, 8);

        drawLegendItem(gc, startX + 10, startY + 13, COLOR_DEFAULT, "Unsorted");
        drawLegendItem(gc, startX + 100, startY + 13, COLOR_ACTIVE, "Comparing");
        drawLegendItem(gc, startX + 205, startY + 13, COLOR_VISITED, "Sorted");
        drawLegendItem(gc, startX + 295, startY + 13, COLOR_DISCARDED, "Pivot / Target");

        gc.restore();
    }
}
