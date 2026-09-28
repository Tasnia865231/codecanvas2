package com.codecanvas.visualizer.interactive;

import java.util.*;

/**
 * Backing domain model for the interactive VisuAlgo graph canvas.
 * Handles parsing custom user formats, exporting text representations,
 * presets, and multi-layout coordinate computation.
 */
public class InteractiveGraphModel {

    private final List<VisualGraphNode> nodes = new ArrayList<>();
    private final List<VisualGraphEdge> edges = new ArrayList<>();

    private GraphLayoutType layoutType = GraphLayoutType.DEFAULT;
    private GraphInputType inputType = GraphInputType.EDGE_LIST;
    private boolean oneIndexed = false;
    private boolean directed = true;

    public InteractiveGraphModel() {
        loadDefaultPreset(false);
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    // ============================================================ PRESETS

    public void loadDefaultPreset(boolean oneIndexed) {
        this.oneIndexed = oneIndexed;
        clear();
        int n = 6;
        for (int i = 0; i < n; i++) {
            nodes.add(new VisualGraphNode(i, formatNodeLabel(i, oneIndexed), 0, 0));
        }
        // Directed weighted graph
        addEdge(0, 1, 4, true);
        addEdge(0, 2, 2, true);
        addEdge(1, 2, 5, true);
        addEdge(2, 1, 3, true); // Antiparallel edge (1 <-> 2) for curved bidirectional edge rendering
        addEdge(1, 3, 10, true);
        addEdge(2, 4, 3, true);
        addEdge(4, 3, 4, true);
        addEdge(3, 5, 11, true);
        addEdge(4, 5, 8, true);
    }

    public void loadBipartitePreset(boolean oneIndexed) {
        this.oneIndexed = oneIndexed;
        this.layoutType = GraphLayoutType.BIPARTITE;
        clear();
        int n = 6;
        for (int i = 0; i < n; i++) {
            nodes.add(new VisualGraphNode(i, formatNodeLabel(i, oneIndexed), 0, 0));
        }
        // Partition: Set A = {0, 1, 2}, Set B = {3, 4, 5}
        addEdge(0, 3, 3, false);
        addEdge(0, 4, 6, false);
        addEdge(1, 3, 2, false);
        addEdge(1, 5, 5, false);
        addEdge(2, 4, 7, false);
        addEdge(2, 5, 4, false);
    }

    public void loadTreePreset(boolean oneIndexed) {
        this.oneIndexed = oneIndexed;
        this.layoutType = GraphLayoutType.TREE;
        clear();
        int n = 7;
        for (int i = 0; i < n; i++) {
            nodes.add(new VisualGraphNode(i, formatNodeLabel(i, oneIndexed), 0, 0));
        }
        // Tree edges from root 0
        addEdge(0, 1, 5, true);
        addEdge(0, 2, 8, true);
        addEdge(1, 3, 3, true);
        addEdge(1, 4, 7, true);
        addEdge(2, 5, 4, true);
        addEdge(2, 6, 6, true);
    }

    public void loadDagPreset(boolean oneIndexed) {
        this.oneIndexed = oneIndexed;
        this.layoutType = GraphLayoutType.DAG;
        clear();
        int n = 6;
        for (int i = 0; i < n; i++) {
            nodes.add(new VisualGraphNode(i, formatNodeLabel(i, oneIndexed), 0, 0));
        }
        // DAG from source 0 to sink 5
        addEdge(0, 1, 3, true);
        addEdge(0, 2, 6, true);
        addEdge(1, 2, 2, true);
        addEdge(1, 3, 4, true);
        addEdge(2, 3, 1, true);
        addEdge(2, 4, 4, true);
        addEdge(3, 4, 2, true);
        addEdge(3, 5, 5, true);
        addEdge(4, 5, 3, true);
    }

    public void loadRandomPreset(int nodeCount, boolean oneIndexed) {
        this.oneIndexed = oneIndexed;
        clear();
        int n = Math.max(4, Math.min(10, nodeCount));
        for (int i = 0; i < n; i++) {
            nodes.add(new VisualGraphNode(i, formatNodeLabel(i, oneIndexed), 0, 0));
        }
        Random rng = new Random();
        // Ensure connected backbone
        for (int i = 0; i < n - 1; i++) {
            addEdge(i, i + 1, rng.nextInt(9) + 1, directed);
        }
        // Add additional random edges
        for (int i = 0; i < n; i++) {
            int target = rng.nextInt(n);
            if (target != i) {
                addEdge(i, target, rng.nextInt(9) + 1, directed);
            }
        }
    }

    public void addEdge(int u, int v, int weight, boolean directed) {
        for (VisualGraphEdge e : edges) {
            if (e.getU() == u && e.getV() == v) {
                e.setWeight(weight);
                return;
            }
        }
        edges.add(new VisualGraphEdge(u, v, weight, directed));
    }

    public boolean hasDirectedEdge(int u, int v) {
        for (VisualGraphEdge e : edges) {
            if (e.isDirected() && e.getU() == u && e.getV() == v) {
                return true;
            }
        }
        return false;
    }

    // ============================================================ PARSING

    public boolean parseCustomInput(String text, GraphInputType type, boolean oneIndexed, boolean directed) {
        if (text == null || text.isBlank()) return false;
        this.inputType = type;
        this.oneIndexed = oneIndexed;
        this.directed = directed;

        try {
            switch (type) {
                case EDGE_LIST -> parseEdgeList(text, oneIndexed, directed);
                case ADJACENCY_MATRIX -> parseAdjacencyMatrix(text, oneIndexed, directed);
                case ADJACENCY_LIST -> parseAdjacencyList(text, oneIndexed, directed);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("Failed to parse custom graph input: " + ex.getMessage());
            return false;
        }
    }

    private void parseEdgeList(String text, boolean oneIndexed, boolean directed) {
        clear();
        Set<Integer> vertexSet = new TreeSet<>();
        List<int[]> parsedEdges = new ArrayList<>();

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

            String[] tokens = line.split("[,\\s]+");
            if (tokens.length >= 2) {
                int rawU = Integer.parseInt(tokens[0].trim());
                int rawV = Integer.parseInt(tokens[1].trim());
                int weight = (tokens.length >= 3) ? Integer.parseInt(tokens[2].trim()) : 1;

                int u = oneIndexed ? (rawU - 1) : rawU;
                int v = oneIndexed ? (rawV - 1) : rawV;

                vertexSet.add(u);
                vertexSet.add(v);
                parsedEdges.add(new int[]{u, v, weight});
            }
        }

        for (int vId : vertexSet) {
            nodes.add(new VisualGraphNode(vId, formatNodeLabel(vId, oneIndexed), 0, 0));
        }
        for (int[] e : parsedEdges) {
            addEdge(e[0], e[1], e[2], directed);
        }
    }

    private void parseAdjacencyMatrix(String text, boolean oneIndexed, boolean directed) {
        clear();
        String[] lines = text.split("\\r?\\n");
        List<List<Integer>> matrix = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] tokens = line.split("[,\\s]+");
            List<Integer> row = new ArrayList<>();
            for (String tok : tokens) {
                if (!tok.isBlank()) {
                    row.add(Integer.parseInt(tok.trim()));
                }
            }
            if (!row.isEmpty()) matrix.add(row);
        }

        int n = matrix.size();
        for (int i = 0; i < n; i++) {
            nodes.add(new VisualGraphNode(i, formatNodeLabel(i, oneIndexed), 0, 0));
        }

        for (int i = 0; i < n; i++) {
            List<Integer> row = matrix.get(i);
            for (int j = 0; j < Math.min(n, row.size()); j++) {
                int w = row.get(j);
                if (w > 0) {
                    if (!directed && i > j) continue;
                    addEdge(i, j, w, directed);
                }
            }
        }
    }

    private void parseAdjacencyList(String text, boolean oneIndexed, boolean directed) {
        clear();
        Set<Integer> vertexSet = new TreeSet<>();
        List<int[]> parsedEdges = new ArrayList<>();

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("[:->]+", 2);
            if (parts.length >= 1) {
                int rawU = Integer.parseInt(parts[0].trim());
                int u = oneIndexed ? (rawU - 1) : rawU;
                vertexSet.add(u);

                if (parts.length == 2 && !parts[1].isBlank()) {
                    String targets = parts[1].trim();
                    // Supports "1(4) 2(2)" or "1 2"
                    String[] tokens = targets.split("[,\\s]+");
                    for (String tok : tokens) {
                        if (tok.isBlank()) continue;
                        if (tok.contains("(") && tok.contains(")")) {
                            int open = tok.indexOf('(');
                            int close = tok.indexOf(')');
                            int rawV = Integer.parseInt(tok.substring(0, open).trim());
                            int w = Integer.parseInt(tok.substring(open + 1, close).trim());
                            int v = oneIndexed ? (rawV - 1) : rawV;
                            vertexSet.add(v);
                            parsedEdges.add(new int[]{u, v, w});
                        } else {
                            int rawV = Integer.parseInt(tok.trim());
                            int v = oneIndexed ? (rawV - 1) : rawV;
                            vertexSet.add(v);
                            parsedEdges.add(new int[]{u, v, 1});
                        }
                    }
                }
            }
        }

        for (int vId : vertexSet) {
            nodes.add(new VisualGraphNode(vId, formatNodeLabel(vId, oneIndexed), 0, 0));
        }
        for (int[] e : parsedEdges) {
            addEdge(e[0], e[1], e[2], directed);
        }
    }

    public String exportToText(GraphInputType type, boolean oneIndexed) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case EDGE_LIST -> {
                for (VisualGraphEdge e : edges) {
                    int u = oneIndexed ? e.getU() + 1 : e.getU();
                    int v = oneIndexed ? e.getV() + 1 : e.getV();
                    sb.append(u).append(" ").append(v).append(" ").append(e.getWeight()).append("\n");
                }
            }
            case ADJACENCY_MATRIX -> {
                int n = nodes.size();
                int[][] mat = new int[n][n];
                for (VisualGraphEdge e : edges) {
                    if (e.getU() < n && e.getV() < n) {
                        mat[e.getU()][e.getV()] = e.getWeight();
                        if (!e.isDirected()) mat[e.getV()][e.getU()] = e.getWeight();
                    }
                }
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        sb.append(mat[i][j]).append(j == n - 1 ? "" : " ");
                    }
                    sb.append("\n");
                }
            }
            case ADJACENCY_LIST -> {
                Map<Integer, List<VisualGraphEdge>> adj = getAdjacencyMap();
                for (VisualGraphNode node : nodes) {
                    int u = oneIndexed ? node.getId() + 1 : node.getId();
                    sb.append(u).append(":");
                    List<VisualGraphEdge> out = adj.getOrDefault(node.getId(), Collections.emptyList());
                    for (VisualGraphEdge e : out) {
                        int v = oneIndexed ? e.getV() + 1 : e.getV();
                        sb.append(" ").append(v).append("(").append(e.getWeight()).append(")");
                    }
                    sb.append("\n");
                }
            }
        }
        return sb.toString().trim();
    }

    private String formatNodeLabel(int id, boolean oneIndexed) {
        return String.valueOf(oneIndexed ? id + 1 : id);
    }

    // ============================================================ LAYOUT ENGINE

    public void computeLayout(double width, double height) {
        if (nodes.isEmpty()) return;
        double w = Math.max(400.0, width);
        double h = Math.max(300.0, height);

        switch (layoutType) {
            case BIPARTITE -> computeBipartiteLayout(w, h);
            case TREE -> computeTreeLayout(w, h);
            case DAG -> computeDagLayout(w, h);
            default -> computeDefaultCircularLayout(w, h);
        }
    }

    private void computeDefaultCircularLayout(double width, double height) {
        int n = nodes.size();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double rx = Math.max(120.0, (width / 2.0) - 80.0);
        double ry = Math.max(100.0, (height / 2.0) - 60.0);

        for (int i = 0; i < n; i++) {
            double angle = (2.0 * Math.PI * i / n) - (Math.PI / 2.0);
            double x = centerX + rx * Math.cos(angle);
            double y = centerY + ry * Math.sin(angle);
            nodes.get(i).setX(x);
            nodes.get(i).setY(y);
        }
    }

    private void computeBipartiteLayout(double width, double height) {
        int n = nodes.size();
        int half = (n + 1) / 2;
        double leftX = width * 0.28;
        double rightX = width * 0.72;

        List<VisualGraphNode> leftNodes = new ArrayList<>();
        List<VisualGraphNode> rightNodes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (i < half) leftNodes.add(nodes.get(i));
            else rightNodes.add(nodes.get(i));
        }

        distributeColumn(leftNodes, leftX, height);
        distributeColumn(rightNodes, rightX, height);
    }

    private void distributeColumn(List<VisualGraphNode> colNodes, double x, double height) {
        int m = colNodes.size();
        if (m == 0) return;
        double startY = 70.0;
        double availableH = height - 140.0;
        double step = m > 1 ? availableH / (m - 1) : 0;

        for (int i = 0; i < m; i++) {
            double y = (m == 1) ? (height / 2.0) : (startY + i * step);
            colNodes.get(i).setX(x);
            colNodes.get(i).setY(y);
        }
    }

    private void computeTreeLayout(double width, double height) {
        if (nodes.isEmpty()) return;

        // BFS level assignments from node 0 as root
        Map<Integer, Integer> levels = new HashMap<>();
        Map<Integer, List<Integer>> levelBuckets = new TreeMap<>();

        int root = nodes.get(0).getId();
        levels.put(root, 0);
        levelBuckets.computeIfAbsent(0, k -> new ArrayList<>()).add(root);

        Queue<Integer> q = new LinkedList<>();
        q.add(root);
        Set<Integer> visited = new HashSet<>();
        visited.add(root);

        Map<Integer, List<VisualGraphEdge>> adj = getAdjacencyMap();

        while (!q.isEmpty()) {
            int u = q.poll();
            int currL = levels.get(u);
            for (VisualGraphEdge e : adj.getOrDefault(u, Collections.emptyList())) {
                int v = e.getV();
                if (!visited.contains(v)) {
                    visited.add(v);
                    levels.put(v, currL + 1);
                    levelBuckets.computeIfAbsent(currL + 1, k -> new ArrayList<>()).add(v);
                    q.add(v);
                }
            }
        }

        // Any disconnected nodes placed on bottom level
        int maxL = levelBuckets.keySet().stream().max(Integer::compareTo).orElse(0);
        for (VisualGraphNode n : nodes) {
            if (!levels.containsKey(n.getId())) {
                levelBuckets.computeIfAbsent(maxL + 1, k -> new ArrayList<>()).add(n.getId());
            }
        }

        int totalLevels = levelBuckets.size();
        double startY = 60.0;
        double levelSpacing = totalLevels > 1 ? (height - 120.0) / (totalLevels - 1) : 0;

        int lIdx = 0;
        for (Map.Entry<Integer, List<Integer>> entry : levelBuckets.entrySet()) {
            double y = (totalLevels == 1) ? (height / 2.0) : (startY + lIdx * levelSpacing);
            List<Integer> bucket = entry.getValue();
            int bSize = bucket.size();
            double spacingX = width / (bSize + 1);

            for (int j = 0; j < bSize; j++) {
                int vId = bucket.get(j);
                VisualGraphNode node = getNodeById(vId);
                if (node != null) {
                    node.setX(spacingX * (j + 1));
                    node.setY(y);
                }
            }
            lIdx++;
        }
    }

    private void computeDagLayout(double width, double height) {
        int n = nodes.size();
        int inDeg[] = new int[n];
        Map<Integer, List<VisualGraphEdge>> adj = getAdjacencyMap();

        for (VisualGraphEdge e : edges) {
            if (e.getV() < n) inDeg[e.getV()]++;
        }

        // Rank layering via longest path from sources
        int[] rank = new int[n];
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDeg[i] == 0) q.add(i);
        }

        while (!q.isEmpty()) {
            int u = q.poll();
            for (VisualGraphEdge e : adj.getOrDefault(u, Collections.emptyList())) {
                int v = e.getV();
                if (v < n) {
                    rank[v] = Math.max(rank[v], rank[u] + 1);
                    inDeg[v]--;
                    if (inDeg[v] == 0) q.add(v);
                }
            }
        }

        Map<Integer, List<Integer>> rankBuckets = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            rankBuckets.computeIfAbsent(rank[i], k -> new ArrayList<>()).add(i);
        }

        int totalRanks = rankBuckets.size();
        double startX = 80.0;
        double rankStep = totalRanks > 1 ? (width - 160.0) / (totalRanks - 1) : 0;

        int rIdx = 0;
        for (Map.Entry<Integer, List<Integer>> entry : rankBuckets.entrySet()) {
            double x = (totalRanks == 1) ? (width / 2.0) : (startX + rIdx * rankStep);
            List<Integer> b = entry.getValue();
            distributeColumn(b.stream().map(this::getNodeById).filter(Objects::nonNull).toList(), x, height);
            rIdx++;
        }
    }

    // ============================================================ HELPERS

    public Map<Integer, List<VisualGraphEdge>> getAdjacencyMap() {
        Map<Integer, List<VisualGraphEdge>> map = new HashMap<>();
        for (VisualGraphEdge e : edges) {
            map.computeIfAbsent(e.getU(), k -> new ArrayList<>()).add(e);
            if (!e.isDirected()) {
                map.computeIfAbsent(e.getV(), k -> new ArrayList<>()).add(new VisualGraphEdge(e.getV(), e.getU(), e.getWeight(), false));
            }
        }
        return map;
    }

    public VisualGraphNode getNodeById(int id) {
        for (VisualGraphNode node : nodes) {
            if (node.getId() == id) return node;
        }
        return null;
    }

    public List<VisualGraphNode> getNodes() { return nodes; }
    public List<VisualGraphEdge> getEdges() { return edges; }

    public GraphLayoutType getLayoutType() { return layoutType; }
    public void setLayoutType(GraphLayoutType layoutType) { this.layoutType = layoutType; }

    public GraphInputType getInputType() { return inputType; }
    public void setInputType(GraphInputType inputType) { this.inputType = inputType; }

    public boolean isOneIndexed() { return oneIndexed; }
    public void setOneIndexed(boolean oneIndexed) {
        this.oneIndexed = oneIndexed;
        for (VisualGraphNode n : nodes) {
            n.setLabel(formatNodeLabel(n.getId(), oneIndexed));
        }
    }

    public boolean isDirected() { return directed; }
    public void setDirected(boolean directed) {
        this.directed = directed;
        for (VisualGraphEdge e : edges) e.setDirected(directed);
    }
}
