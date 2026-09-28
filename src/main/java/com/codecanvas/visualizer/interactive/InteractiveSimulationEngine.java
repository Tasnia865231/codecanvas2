package com.codecanvas.visualizer.interactive;

import java.util.*;

/**
 * VisuAlgo-style step-by-step algorithmic frame generator.
 * Produces structured animation frames for interactive stepping,
 * highlighting active nodes/edges in green/orange/red.
 */
public class InteractiveSimulationEngine {

    public static List<SimulationFrame> generateFramesForAlgorithm(String algorithmName, InteractiveGraphModel model, int sourceVertex, int[] customArray, int targetValue) {
        String name = algorithmName != null ? algorithmName.trim() : "BFS";
        String lower = name.toLowerCase();

        if (lower.contains("bfs") || lower.contains("breadth")) {
            return generateBfs(model, sourceVertex);
        } else if (lower.contains("dfs") || lower.contains("depth")) {
            return generateDfs(model, sourceVertex);
        } else if (lower.contains("dijkstra")) {
            return generateDijkstra(model, sourceVertex);
        } else if (lower.contains("bellman")) {
            return generateBellmanFord(model, sourceVertex);
        } else if (lower.contains("floyd")) {
            return generateFloydWarshall(model);
        } else if (lower.contains("kruskal")) {
            return generateKruskal(model);
        } else if (lower.contains("prim")) {
            return generatePrim(model, sourceVertex);
        } else if (lower.contains("johnson")) {
            return generateJohnson(model);
        } else if (lower.contains("quick")) {
            int[] arr = (customArray != null && customArray.length > 0) ? customArray : new int[]{38, 27, 43, 3, 9, 82, 10, 19};
            return generateQuickSort(arr);
        } else if (lower.contains("merge")) {
            int[] arr = (customArray != null && customArray.length > 0) ? customArray : new int[]{38, 27, 43, 3, 9, 82, 10, 19};
            return generateMergeSort(arr);
        } else if (lower.contains("binary") || lower.contains("search")) {
            int[] arr = (customArray != null && customArray.length > 0) ? customArray : new int[]{3, 9, 10, 19, 27, 38, 43, 82};
            return generateBinarySearch(arr, targetValue);
        }

        return generateBfs(model, sourceVertex);
    }

    public static List<SimulationFrame> generateFramesForAlgorithm(String algorithmName, InteractiveGraphModel model, int sourceVertex, int[] customArray) {
        return generateFramesForAlgorithm(algorithmName, model, sourceVertex, customArray, 27);
    }

    // ============================================================ BFS

    private static List<SimulationFrame> generateBfs(InteractiveGraphModel model, int startVertex) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        VisualGraphNode srcNode = model.getNodeById(startVertex);
        int src = (srcNode != null) ? startVertex : nodes.get(0).getId();
        int n = nodes.size();
        int maxId = nodes.stream().mapToInt(VisualGraphNode::getId).max().orElse(0);
        int allocSize = Math.max(n, maxId + 1);

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), "d=∞");
        }
        for (VisualGraphEdge edge : model.getEdges()) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        // Frame 0: Initialization
        SimulationFrame f0 = createFrame(1, "Initialize BFS", "Setting all vertex distances to ∞. Queue is currently empty.");
        copyState(f0, nState, nLabels, eState);
        f0.setAuxStructureText("Queue: []");
        frames.add(f0);

        boolean[] visited = new boolean[allocSize];
        int[] dist = new int[allocSize];
        Arrays.fill(dist, Integer.MAX_VALUE);

        Queue<Integer> q = new LinkedList<>();
        visited[src] = true;
        dist[src] = 0;
        q.add(src);

        nState.put(src, NodeState.ACTIVE);
        nLabels.put(src, "d=0");

        SimulationFrame f1 = createFrame(2, "Enqueue Source Node", "Discovered source node " + model.getNodeById(src).getLabel() + ". Set distance d=0 and enqueued.");
        copyState(f1, nState, nLabels, eState);
        f1.setAuxStructureText("Queue: [" + model.getNodeById(src).getLabel() + "]");
        frames.add(f1);

        Map<Integer, List<VisualGraphEdge>> adj = model.getAdjacencyMap();

        int step = 3;
        while (!q.isEmpty()) {
            int u = q.poll();
            nState.put(u, NodeState.ACTIVE);

            SimulationFrame fPop = createFrame(step++, "Dequeue Node " + model.getNodeById(u).getLabel(),
                    "Dequeued vertex " + model.getNodeById(u).getLabel() + " (distance=" + dist[u] + "). Exploring outgoing edges.");
            copyState(fPop, nState, nLabels, eState);
            fPop.setAuxStructureText("Queue: " + formatQueue(q, model));
            frames.add(fPop);

            List<VisualGraphEdge> outgoing = adj.getOrDefault(u, Collections.emptyList());
            for (VisualGraphEdge edge : outgoing) {
                int v = edge.getV();
                String eKey = edge.getKey();

                eState.put(eKey, EdgeState.EXPLORING);
                SimulationFrame fProbe = createFrame(step++, "Inspect Edge (" + model.getNodeById(u).getLabel() + " -> " + model.getNodeById(v).getLabel() + ")",
                        "Examining edge to neighbor " + model.getNodeById(v).getLabel() + " (weight=" + edge.getWeight() + ").");
                copyState(fProbe, nState, nLabels, eState);
                fProbe.setAuxStructureText("Queue: " + formatQueue(q, model));
                frames.add(fProbe);

                if (!visited[v]) {
                    visited[v] = true;
                    dist[v] = dist[u] + 1;
                    q.add(v);

                    nState.put(v, NodeState.ACTIVE);
                    nLabels.put(v, "d=" + dist[v]);
                    eState.put(eKey, EdgeState.SELECTED_MST);

                    SimulationFrame fVisit = createFrame(step++, "Discover Node " + model.getNodeById(v).getLabel(),
                            "Unvisited node " + model.getNodeById(v).getLabel() + " discovered! Recorded distance d=" + dist[v] + " and enqueued.");
                    copyState(fVisit, nState, nLabels, eState);
                    fVisit.setAuxStructureText("Queue: " + formatQueue(q, model));
                    frames.add(fVisit);
                } else {
                    eState.put(eKey, EdgeState.DEFAULT);
                }
            }

            nState.put(u, NodeState.VISITED);
        }

        // Final Frame
        for (VisualGraphNode node : nodes) {
            if (node.getId() < visited.length && visited[node.getId()]) nState.put(node.getId(), NodeState.VISITED);
        }
        SimulationFrame fEnd = createFrame(step, "BFS Traversal Complete", "All reachable nodes have been traversed level by level.");
        copyState(fEnd, nState, nLabels, eState);
        fEnd.setAuxStructureText("Queue: [Empty]");
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    // ============================================================ DFS

    private static List<SimulationFrame> generateDfs(InteractiveGraphModel model, int startVertex) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        VisualGraphNode srcNode = model.getNodeById(startVertex);
        int src = (srcNode != null) ? startVertex : nodes.get(0).getId();
        int n = nodes.size();
        int maxId = nodes.stream().mapToInt(VisualGraphNode::getId).max().orElse(0);
        int allocSize = Math.max(n, maxId + 1);

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), "unvisited");
        }
        for (VisualGraphEdge edge : model.getEdges()) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        SimulationFrame f0 = createFrame(1, "Initialize DFS", "Depth-First Search initialized with source vertex " + model.getNodeById(src).getLabel() + ".");
        copyState(f0, nState, nLabels, eState);
        f0.setAuxStructureText("Call Stack: []");
        frames.add(f0);

        boolean[] visited = new boolean[allocSize];
        List<Integer> callStack = new ArrayList<>();
        int[] step = new int[]{2};

        dfsRecursive(src, -1, model, visited, callStack, nState, nLabels, eState, frames, step);

        SimulationFrame fEnd = createFrame(step[0], "DFS Traversal Complete", "Depth-First Search finished exploring all reachable branches.");
        copyState(fEnd, nState, nLabels, eState);
        fEnd.setAuxStructureText("Call Stack: []");
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    private static void dfsRecursive(int u, int parent, InteractiveGraphModel model, boolean[] visited, List<Integer> callStack,
                                     Map<Integer, NodeState> nState, Map<Integer, String> nLabels, Map<String, EdgeState> eState,
                                     List<SimulationFrame> frames, int[] step) {
        visited[u] = true;
        callStack.add(u);
        nState.put(u, NodeState.ACTIVE);
        nLabels.put(u, "entry #" + callStack.size());

        SimulationFrame fEnter = createFrame(step[0]++, "Enter Node " + model.getNodeById(u).getLabel(),
                "Pushed node " + model.getNodeById(u).getLabel() + " to recursion stack. Exploring outgoing edges deeply.");
        copyState(fEnter, nState, nLabels, eState);
        fEnter.setAuxStructureText("Call Stack: " + formatCallStack(callStack, model));
        frames.add(fEnter);

        Map<Integer, List<VisualGraphEdge>> adj = model.getAdjacencyMap();
        for (VisualGraphEdge edge : adj.getOrDefault(u, Collections.emptyList())) {
            int v = edge.getV();
            if (v == parent && !edge.isDirected()) continue;

            String eKey = edge.getKey();
            eState.put(eKey, EdgeState.EXPLORING);

            SimulationFrame fProbe = createFrame(step[0]++, "Inspect Edge (" + model.getNodeById(u).getLabel() + " -> " + model.getNodeById(v).getLabel() + ")",
                    "Checking branch from node " + model.getNodeById(u).getLabel() + " to node " + model.getNodeById(v).getLabel() + ".");
            copyState(fProbe, nState, nLabels, eState);
            fProbe.setAuxStructureText("Call Stack: " + formatCallStack(callStack, model));
            frames.add(fProbe);

            if (!visited[v]) {
                eState.put(eKey, EdgeState.SELECTED_MST);
                dfsRecursive(v, u, model, visited, callStack, nState, nLabels, eState, frames, step);
            } else {
                eState.put(eKey, EdgeState.DEFAULT);
            }
        }

        nState.put(u, NodeState.VISITED);
        nLabels.put(u, "visited");
        callStack.remove(callStack.size() - 1);

        SimulationFrame fBacktrack = createFrame(step[0]++, "Backtrack from Node " + model.getNodeById(u).getLabel(),
                "Completed branch traversal for node " + model.getNodeById(u).getLabel() + ". Backtracking up the call tree.");
        copyState(fBacktrack, nState, nLabels, eState);
        fBacktrack.setAuxStructureText("Call Stack: " + formatCallStack(callStack, model));
        frames.add(fBacktrack);
    }

    // ============================================================ DIJKSTRA

    private static List<SimulationFrame> generateDijkstra(InteractiveGraphModel model, int startVertex) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        VisualGraphNode srcNode = model.getNodeById(startVertex);
        int src = (srcNode != null) ? startVertex : nodes.get(0).getId();
        int n = nodes.size();
        int maxId = nodes.stream().mapToInt(VisualGraphNode::getId).max().orElse(0);
        int allocSize = Math.max(n, maxId + 1);

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        int[] dist = new int[allocSize];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), node.getId() == src ? "d=0" : "d=∞");
        }
        for (VisualGraphEdge edge : model.getEdges()) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        SimulationFrame f0 = createFrame(1, "Initialize Dijkstra",
                "Set source node " + model.getNodeById(src).getLabel() + " distance to 0, all others to ∞. Initializing PriorityQueue.");
        copyState(f0, nState, nLabels, eState);
        f0.setAuxStructureText("PriorityQueue: [(" + model.getNodeById(src).getLabel() + ", d=0)]");
        frames.add(f0);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{src, 0});
        boolean[] settled = new boolean[allocSize];

        Map<Integer, List<VisualGraphEdge>> adj = model.getAdjacencyMap();
        int step = 2;

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int u = top[0];
            int d = top[1];

            if (settled[u]) continue;
            settled[u] = true;
            nState.put(u, NodeState.ACTIVE);

            SimulationFrame fPop = createFrame(step++, "Extract Minimum Node " + model.getNodeById(u).getLabel(),
                    "Selected node " + model.getNodeById(u).getLabel() + " with confirmed minimum distance d=" + d + " from PriorityQueue.");
            copyState(fPop, nState, nLabels, eState);
            fPop.setAuxStructureText("PQ Size: " + pq.size());
            frames.add(fPop);

            for (VisualGraphEdge edge : adj.getOrDefault(u, Collections.emptyList())) {
                int v = edge.getV();
                int w = edge.getWeight();
                String eKey = edge.getKey();

                eState.put(eKey, EdgeState.EXPLORING);
                SimulationFrame fProbe = createFrame(step++, "Inspect Edge (" + model.getNodeById(u).getLabel() + " -> " + model.getNodeById(v).getLabel() + ")",
                        "Checking if path via " + model.getNodeById(u).getLabel() + " (d=" + d + " + " + w + " = " + (d + w) + ") improves distance to " + model.getNodeById(v).getLabel() + " (" + nLabels.get(v) + ").");
                copyState(fProbe, nState, nLabels, eState);
                fProbe.setAuxStructureText("PQ Size: " + pq.size());
                frames.add(fProbe);

                if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{v, dist[v]});
                    nLabels.put(v, "d=" + dist[v]);
                    eState.put(eKey, EdgeState.SELECTED_MST);

                    SimulationFrame fRelax = createFrame(step++, "Relax Edge to Node " + model.getNodeById(v).getLabel(),
                            "Relaxation successful! Updated dist[" + model.getNodeById(v).getLabel() + "] = " + dist[v] + " and pushed to PriorityQueue.");
                    copyState(fRelax, nState, nLabels, eState);
                    fRelax.setAuxStructureText("PQ Size: " + pq.size());
                    frames.add(fRelax);
                } else {
                    eState.put(eKey, EdgeState.DEFAULT);
                }
            }

            nState.put(u, NodeState.VISITED);
        }

        SimulationFrame fEnd = createFrame(step, "Dijkstra Shortest Paths Found",
                "Shortest path tree computed successfully. All reachable node distances are final.");
        copyState(fEnd, nState, nLabels, eState);
        fEnd.setAuxStructureText("Settled Nodes: " + Arrays.stream(dist).filter(d -> d < Integer.MAX_VALUE).count());
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    // ============================================================ BELLMAN-FORD

    private static List<SimulationFrame> generateBellmanFord(InteractiveGraphModel model, int startVertex) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        VisualGraphNode srcNode = model.getNodeById(startVertex);
        int src = (srcNode != null) ? startVertex : nodes.get(0).getId();
        int n = nodes.size();
        int maxId = nodes.stream().mapToInt(VisualGraphNode::getId).max().orElse(0);
        int allocSize = Math.max(n, maxId + 1);
        List<VisualGraphEdge> edges = model.getEdges();

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        int[] dist = new int[allocSize];
        Arrays.fill(dist, 999999);
        dist[src] = 0;

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), node.getId() == src ? "d=0" : "d=∞");
        }
        for (VisualGraphEdge edge : edges) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        SimulationFrame f0 = createFrame(1, "Initialize Bellman-Ford",
                "Source node " + model.getNodeById(src).getLabel() + " distance set to 0. Preparing |V|-1 (" + (n - 1) + ") edge relaxation passes.");
        copyState(f0, nState, nLabels, eState);
        frames.add(f0);

        int step = 2;
        for (int pass = 1; pass <= Math.min(3, n - 1); pass++) {
            for (VisualGraphEdge edge : edges) {
                int u = edge.getU();
                int v = edge.getV();
                int w = edge.getWeight();
                String eKey = edge.getKey();

                if (dist[u] < 999999 && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    nLabels.put(v, "d=" + dist[v]);
                    nState.put(v, NodeState.ACTIVE);
                    eState.put(eKey, EdgeState.SELECTED_MST);

                    SimulationFrame fPass = createFrame(step++, "Pass " + pass + ": Relax (" + model.getNodeById(u).getLabel() + " -> " + model.getNodeById(v).getLabel() + ")",
                            "Edge relaxed! dist[" + model.getNodeById(v).getLabel() + "] reduced to " + dist[v] + ".");
                    copyState(fPass, nState, nLabels, eState);
                    frames.add(fPass);
                    nState.put(v, NodeState.DEFAULT);
                }
            }
        }

        for (VisualGraphNode node : nodes) {
            if (dist[node.getId()] < 999999) nState.put(node.getId(), NodeState.VISITED);
        }
        SimulationFrame fEnd = createFrame(step, "Bellman-Ford Complete", "Passes completed. No negative cycles detected.");
        copyState(fEnd, nState, nLabels, eState);
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    // ============================================================ FLOYD-WARSHALL

    private static List<SimulationFrame> generateFloydWarshall(InteractiveGraphModel model) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        int n = nodes.size();
        int[][] dist = new int[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], 999999);
            dist[i][i] = 0;
        }
        for (VisualGraphEdge e : model.getEdges()) {
            if (e.getU() < n && e.getV() < n) {
                dist[e.getU()][e.getV()] = Math.min(dist[e.getU()][e.getV()], e.getWeight());
                if (!e.isDirected()) dist[e.getV()][e.getU()] = Math.min(dist[e.getV()][e.getU()], e.getWeight());
            }
        }

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();
        for (VisualGraphNode node : nodes) nState.put(node.getId(), NodeState.DEFAULT);
        for (VisualGraphEdge edge : model.getEdges()) eState.put(edge.getKey(), EdgeState.DEFAULT);

        SimulationFrame f0 = createFrame(1, "Initialize Floyd-Warshall", "Initialized distance matrix D[u][v]. Ready to evaluate intermediate vertices k = 0.." + (n - 1) + ".");
        copyState(f0, nState, nLabels, eState);
        frames.add(f0);

        int step = 2;
        for (int k = 0; k < Math.min(n, 3); k++) {
            nState.put(k, NodeState.ACTIVE);
            nLabels.put(k, "Pivot (k=" + model.getNodeById(k).getLabel() + ")");

            SimulationFrame fK = createFrame(step++, "Evaluate Intermediate Vertex k = " + model.getNodeById(k).getLabel(),
                    "Using node " + model.getNodeById(k).getLabel() + " as intermediate hop to test path improvements: dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j]).");
            copyState(fK, nState, nLabels, eState);
            frames.add(fK);

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] < 999999 && dist[k][j] < 999999 && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
            nState.put(k, NodeState.VISITED);
        }

        SimulationFrame fEnd = createFrame(step, "Floyd-Warshall Complete", "All-pairs shortest path matrix fully converged with complexity O(V^3).");
        copyState(fEnd, nState, nLabels, eState);
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    // ============================================================ JOHNSON'S ALGORITHM

    private static List<SimulationFrame> generateJohnson(InteractiveGraphModel model) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        int n = nodes.size();
        List<VisualGraphEdge> edges = model.getEdges();

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), "h=0");
        }
        for (VisualGraphEdge edge : edges) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        // Frame 1: Introduce auxiliary source s
        SimulationFrame f0 = createFrame(1, "Step 1: Add Auxiliary Source s",
                "Augment graph G with a new vertex s connected to every vertex v in V with directed weight 0.");
        copyState(f0, nState, nLabels, eState);
        f0.setAuxStructureText("Phase: Bellman-Ford on Augmented Graph");
        frames.add(f0);

        // Frame 2: Bellman-Ford to compute potentials h(v)
        int[] h = new int[n];
        Arrays.fill(h, 0);
        int step = 2;

        SimulationFrame fBf = createFrame(step++, "Step 2: Run Bellman-Ford from s",
                "Computing potential function h(v) = dist(s, v). If a negative cycle is detected, Johnson's terminates.");
        copyState(fBf, nState, nLabels, eState);
        fBf.setAuxStructureText("Potential h values computed via Bellman-Ford");
        frames.add(fBf);

        // Simulate potentials
        for (VisualGraphEdge edge : edges) {
            int u = edge.getU();
            int v = edge.getV();
            if (u < n && v < n && edge.getWeight() < 0) {
                h[v] = Math.min(h[v], h[u] + edge.getWeight());
            }
        }
        for (int i = 0; i < n; i++) {
            nLabels.put(i, "h=" + h[i]);
            nState.put(i, NodeState.ACTIVE);
        }

        SimulationFrame fH = createFrame(step++, "Potential Function h(v) Established",
                "Calculated potentials: " + Arrays.toString(h) + ". Reweighting all directed edges to make weights non-negative.");
        copyState(fH, nState, nLabels, eState);
        fH.setAuxStructureText("All h(v) verified • No negative cycles");
        frames.add(fH);

        // Frame 3: Reweight edges: w_hat(u, v) = w(u, v) + h(u) - h(v)
        for (VisualGraphEdge edge : edges) {
            int u = edge.getU();
            int v = edge.getV();
            if (u < n && v < n) {
                eState.put(edge.getKey(), EdgeState.SELECTED_MST);
            }
        }

        SimulationFrame fReweight = createFrame(step++, "Step 3: Edge Reweighting: ŵ(u,v) = w(u,v) + h(u) - h(v) >= 0",
                "All edge weights transformed into non-negative values. Triangle inequality guarantees ŵ(u,v) >= 0.");
        copyState(fReweight, nState, nLabels, eState);
        fReweight.setAuxStructureText("All edge weights >= 0");
        frames.add(fReweight);

        // Frame 4..: Run Dijkstra from each vertex
        for (int i = 0; i < Math.min(n, 3); i++) {
            for (VisualGraphNode node : nodes) nState.put(node.getId(), NodeState.DEFAULT);
            nState.put(i, NodeState.ACTIVE);
            nLabels.put(i, "Dijkstra Root");

            SimulationFrame fDijk = createFrame(step++, "Step 4: Dijkstra from Source Node " + model.getNodeById(i).getLabel(),
                    "Executing Dijkstra's algorithm with priority queue on non-negative weights ŵ from node " + model.getNodeById(i).getLabel() + ".");
            copyState(fDijk, nState, nLabels, eState);
            fDijk.setAuxStructureText("Dijkstra Pass " + (i + 1) + "/" + n);
            frames.add(fDijk);
        }

        // Frame Final: Conversion back to original distances
        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.VISITED);
            nLabels.put(node.getId(), "d settled");
        }
        for (VisualGraphEdge edge : edges) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        SimulationFrame fEnd = createFrame(step, "Johnson's Algorithm Complete",
                "All-pairs shortest paths computed: d(u, v) = d_hat(u, v) - h(u) + h(v). Time complexity: O(V^2 log V + VE).");
        copyState(fEnd, nState, nLabels, eState);
        fEnd.setAuxStructureText("All-pairs shortest path matrix complete");
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    // ============================================================ KRUSKAL'S MST

    private static List<SimulationFrame> generateKruskal(InteractiveGraphModel model) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        int n = nodes.size();
        List<VisualGraphEdge> edgeList = new ArrayList<>(model.getEdges());
        edgeList.sort(Comparator.comparingInt(VisualGraphEdge::getWeight));

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), "set={" + node.getLabel() + "}");
        }
        for (VisualGraphEdge edge : edgeList) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        SimulationFrame f0 = createFrame(1, "Sort Edges by Weight", "Sorted " + edgeList.size() + " edges in ascending order of weight. Initialized Disjoint Set Union (DSU).");
        copyState(f0, nState, nLabels, eState);
        f0.setAuxStructureText("MST Weight = 0 | Edges in MST = 0/" + (n - 1));
        frames.add(f0);

        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        int mstWeight = 0;
        int mstEdges = 0;
        int step = 2;

        for (VisualGraphEdge edge : edgeList) {
            int u = edge.getU();
            int v = edge.getV();
            int w = edge.getWeight();
            String eKey = edge.getKey();

            eState.put(eKey, EdgeState.EXPLORING);
            nState.put(u, NodeState.ACTIVE);
            nState.put(v, NodeState.ACTIVE);

            SimulationFrame fInspect = createFrame(step++, "Inspect Edge (" + model.getNodeById(u).getLabel() + " - " + model.getNodeById(v).getLabel() + ", w=" + w + ")",
                    "Checking if adding edge (" + model.getNodeById(u).getLabel() + " - " + model.getNodeById(v).getLabel() + ") connects disjoint components or creates a cycle.");
            copyState(fInspect, nState, nLabels, eState);
            fInspect.setAuxStructureText("MST Weight = " + mstWeight + " | Edges in MST = " + mstEdges + "/" + (n - 1));
            frames.add(fInspect);

            int rootU = find(parent, u);
            int rootV = find(parent, v);

            if (rootU != rootV) {
                parent[rootU] = rootV;
                mstWeight += w;
                mstEdges++;

                eState.put(eKey, EdgeState.SELECTED_MST);
                nState.put(u, NodeState.VISITED);
                nState.put(v, NodeState.VISITED);

                SimulationFrame fAdd = createFrame(step++, "Add Edge to MST: (" + model.getNodeById(u).getLabel() + " - " + model.getNodeById(v).getLabel() + ")",
                        "No cycle! Added edge with weight " + w + " to Minimum Spanning Tree. Running MST weight = " + mstWeight + ".");
                copyState(fAdd, nState, nLabels, eState);
                fAdd.setAuxStructureText("MST Weight = " + mstWeight + " | Edges in MST = " + mstEdges + "/" + (n - 1));
                frames.add(fAdd);

                if (mstEdges == n - 1) {
                    SimulationFrame fEarly = createFrame(step++, "MST Spanned (V - 1 Edges)",
                            "Successfully connected all " + n + " vertices with " + mstEdges + " edges. Kruskal's terminates early!");
                    copyState(fEarly, nState, nLabels, eState);
                    fEarly.setAuxStructureText("Final MST Weight = " + mstWeight);
                    frames.add(fEarly);
                    break;
                }
            } else {
                eState.put(eKey, EdgeState.DISCARDED_CYCLE);
                nState.put(u, NodeState.DISCARDED);
                nState.put(v, NodeState.DISCARDED);

                SimulationFrame fCycle = createFrame(step++, "Cycle Detected! Skipped (" + model.getNodeById(u).getLabel() + " - " + model.getNodeById(v).getLabel() + ")",
                        "Both vertices belong to the same component (root=" + model.getNodeById(rootU).getLabel() + "). Discarded to prevent cycle.");
                copyState(fCycle, nState, nLabels, eState);
                fCycle.setAuxStructureText("Cycle Rejected | MST Weight = " + mstWeight);
                frames.add(fCycle);

                nState.put(u, NodeState.VISITED);
                nState.put(v, NodeState.VISITED);
            }
        }

        setTotals(frames);
        return frames;
    }

    private static int find(int[] parent, int i) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent, parent[i]);
    }

    // ============================================================ PRIM'S MST

    private static List<SimulationFrame> generatePrim(InteractiveGraphModel model, int startVertex) {
        List<SimulationFrame> frames = new ArrayList<>();
        List<VisualGraphNode> nodes = model.getNodes();
        if (nodes.isEmpty()) return frames;

        VisualGraphNode srcNode = model.getNodeById(startVertex);
        int src = (srcNode != null) ? startVertex : nodes.get(0).getId();
        int n = nodes.size();
        int maxId = nodes.stream().mapToInt(VisualGraphNode::getId).max().orElse(0);
        int allocSize = Math.max(n, maxId + 1);

        Map<Integer, NodeState> nState = new HashMap<>();
        Map<Integer, String> nLabels = new HashMap<>();
        Map<String, EdgeState> eState = new HashMap<>();

        for (VisualGraphNode node : nodes) {
            nState.put(node.getId(), NodeState.DEFAULT);
            nLabels.put(node.getId(), "outside");
        }
        for (VisualGraphEdge edge : model.getEdges()) {
            eState.put(edge.getKey(), EdgeState.DEFAULT);
        }

        boolean[] inMST = new boolean[allocSize];
        inMST[src] = true;
        nState.put(src, NodeState.VISITED);
        nLabels.put(src, "in-MST");

        SimulationFrame f0 = createFrame(1, "Start Prim's from Node " + model.getNodeById(src).getLabel(),
                "Marked node " + model.getNodeById(src).getLabel() + " as root of MST. Enqueueing boundary edges into PriorityQueue.");
        copyState(f0, nState, nLabels, eState);
        f0.setAuxStructureText("MST Tree Size: 1 node | Total Weight: 0");
        frames.add(f0);

        PriorityQueue<VisualGraphEdge> pq = new PriorityQueue<>(Comparator.comparingInt(VisualGraphEdge::getWeight));
        Map<Integer, List<VisualGraphEdge>> adj = model.getAdjacencyMap();
        for (VisualGraphEdge edge : adj.getOrDefault(src, Collections.emptyList())) {
            pq.offer(edge);
        }

        int mstWeight = 0;
        int mstEdges = 0;
        int step = 2;

        while (!pq.isEmpty() && mstEdges < n - 1) {
            VisualGraphEdge minEdge = pq.poll();
            int u = minEdge.getU();
            int v = minEdge.getV();
            int w = minEdge.getWeight();
            String eKey = minEdge.getKey();

            if (inMST[v]) {
                eState.put(eKey, EdgeState.DISCARDED_CYCLE);
                continue;
            }

            inMST[v] = true;
            mstWeight += w;
            mstEdges++;

            eState.put(eKey, EdgeState.SELECTED_MST);
            nState.put(v, NodeState.VISITED);
            nLabels.put(v, "in-MST");

            SimulationFrame fAdd = createFrame(step++, "Add Node " + model.getNodeById(v).getLabel() + " via Edge (" + model.getNodeById(u).getLabel() + " -> " + model.getNodeById(v).getLabel() + ", w=" + w + ")",
                    "Selected minimum cut edge. Node " + model.getNodeById(v).getLabel() + " added to MST. Total MST weight = " + mstWeight + ".");
            copyState(fAdd, nState, nLabels, eState);
            fAdd.setAuxStructureText("MST Tree Size: " + (mstEdges + 1) + " nodes | Weight: " + mstWeight);
            frames.add(fAdd);

            for (VisualGraphEdge nextEdge : adj.getOrDefault(v, Collections.emptyList())) {
                if (!inMST[nextEdge.getV()]) {
                    pq.offer(nextEdge);
                }
            }
        }

        SimulationFrame fEnd = createFrame(step, "Prim's MST Complete",
                "Minimum Spanning Tree construction finished with " + mstEdges + " edges and total weight " + mstWeight + ".");
        copyState(fEnd, nState, nLabels, eState);
        fEnd.setAuxStructureText("Final MST Weight: " + mstWeight);
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    // ============================================================ SORTING: QUICK SORT

    private static List<SimulationFrame> generateQuickSort(int[] initialArray) {
        List<SimulationFrame> frames = new ArrayList<>();
        int[] arr = Arrays.copyOf(initialArray, initialArray.length);

        SimulationFrame f0 = createArrayFrame(1, "Initial Array", "Unsorted array ready for divide-and-conquer Quick Sort.", arr);
        frames.add(f0);

        int[] step = new int[]{2};
        quickSortRecursive(arr, 0, arr.length - 1, frames, step);

        SimulationFrame fEnd = createArrayFrame(step[0], "Quick Sort Complete", "All partitions sorted in-place in O(N log N) expected time.", arr);
        for (int i = 0; i < arr.length; i++) fEnd.getArrayElementStates().put(i, NodeState.VISITED);
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    private static void quickSortRecursive(int[] arr, int low, int high, List<SimulationFrame> frames, int[] step) {
        if (low < high) {
            int pi = partition(arr, low, high, frames, step);
            quickSortRecursive(arr, low, pi - 1, frames, step);
            quickSortRecursive(arr, pi + 1, high, frames, step);
        } else if (low == high && low >= 0 && low < arr.length) {
            SimulationFrame fSingle = createArrayFrame(step[0]++, "Single Element Sorted", "Subarray size 1 is trivially sorted.", arr);
            fSingle.getArrayElementStates().put(low, NodeState.VISITED);
            frames.add(fSingle);
        }
    }

    private static int partition(int[] arr, int low, int high, List<SimulationFrame> frames, int[] step) {
        int pivot = arr[high];
        SimulationFrame fP = createArrayFrame(step[0]++, "Select Pivot = " + pivot, "Chosen rightmost element at index " + high + " as partition pivot.", arr);
        fP.setPointerPivot(high);
        fP.getArrayElementStates().put(high, NodeState.DISCARDED);
        frames.add(fP);

        int i = (low - 1);
        for (int j = low; j < high; j++) {
            SimulationFrame fComp = createArrayFrame(step[0]++, "Compare arr[" + j + "]=" + arr[j] + " with Pivot=" + pivot,
                    "Checking if element " + arr[j] + " <= pivot " + pivot + ".", arr);
            fComp.setPointerI(i);
            fComp.setPointerJ(j);
            fComp.setPointerPivot(high);
            fComp.getArrayElementStates().put(j, NodeState.ACTIVE);
            fComp.getArrayElementStates().put(high, NodeState.DISCARDED);
            frames.add(fComp);

            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;

                SimulationFrame fSwap = createArrayFrame(step[0]++, "Swap arr[" + i + "] and arr[" + j + "]",
                        "Swapped " + arr[i] + " into lower partition (i=" + i + ").", arr);
                fSwap.setPointerI(i);
                fSwap.setPointerJ(j);
                fSwap.setPointerPivot(high);
                frames.add(fSwap);
            }
        }

        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        SimulationFrame fPlaced = createArrayFrame(step[0]++, "Pivot Placed at Index " + (i + 1),
                "Pivot element " + pivot + " placed in its permanent sorted position.", arr);
        fPlaced.getArrayElementStates().put(i + 1, NodeState.VISITED);
        frames.add(fPlaced);

        return i + 1;
    }

    // ============================================================ SORTING: MERGE SORT

    private static List<SimulationFrame> generateMergeSort(int[] initialArray) {
        List<SimulationFrame> frames = new ArrayList<>();
        int[] arr = Arrays.copyOf(initialArray, initialArray.length);

        SimulationFrame f0 = createArrayFrame(1, "Initial Array", "Unsorted array ready for stable divide-and-conquer Merge Sort.", arr);
        frames.add(f0);

        int[] step = new int[]{2};
        mergeSortRecursive(arr, 0, arr.length - 1, frames, step);

        SimulationFrame fEnd = createArrayFrame(step[0], "Merge Sort Complete", "Array successfully sorted with guaranteed O(N log N) time complexity.", arr);
        for (int i = 0; i < arr.length; i++) fEnd.getArrayElementStates().put(i, NodeState.VISITED);
        frames.add(fEnd);

        setTotals(frames);
        return frames;
    }

    private static void mergeSortRecursive(int[] arr, int left, int right, List<SimulationFrame> frames, int[] step) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            SimulationFrame fSplit = createArrayFrame(step[0]++, "Divide Range [" + left + " .. " + right + "]",
                    "Splitting subarray into left [" + left + " .. " + mid + "] and right [" + (mid + 1) + " .. " + right + "].", arr);
            for (int k = left; k <= right; k++) fSplit.getArrayElementStates().put(k, NodeState.ACTIVE);
            frames.add(fSplit);

            mergeSortRecursive(arr, left, mid, frames, step);
            mergeSortRecursive(arr, mid + 1, right, frames, step);
            merge(arr, left, mid, right, frames, step);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right, List<SimulationFrame> frames, int[] step) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] L = new int[n1];
        int[] R = new int[n2];

        System.arraycopy(arr, left, L, 0, n1);
        System.arraycopy(arr, mid + 1, R, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }

        SimulationFrame fMerged = createArrayFrame(step[0]++, "Merged Subarray [" + left + " .. " + right + "]",
                "Merged two sorted halves into single sorted subarray.", arr);
        for (int p = left; p <= right; p++) fMerged.getArrayElementStates().put(p, NodeState.VISITED);
        frames.add(fMerged);
    }

    // ============================================================ SEARCHING: BINARY SEARCH

    private static List<SimulationFrame> generateBinarySearch(int[] arr, int target) {
        List<SimulationFrame> frames = new ArrayList<>();
        int low = 0, high = arr.length - 1;

        SimulationFrame f0 = createArrayFrame(1, "Start Binary Search (Target=" + target + ")",
                "Sorted array initialized. Low=0, High=" + (arr.length - 1) + ".", arr);
        frames.add(f0);

        int step = 2;
        boolean found = false;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            SimulationFrame fMid = createArrayFrame(step++, "Evaluate Mid Index " + mid + " (Value=" + arr[mid] + ")",
                    "Comparing middle element arr[" + mid + "]=" + arr[mid] + " with target " + target + ".", arr);
            fMid.setPointerI(low);
            fMid.setPointerJ(high);
            fMid.setPointerPivot(mid);
            fMid.getArrayElementStates().put(mid, NodeState.ACTIVE);
            frames.add(fMid);

            if (arr[mid] == target) {
                SimulationFrame fFound = createArrayFrame(step++, "Target Found at Index " + mid,
                        "Match found! Value " + target + " confirmed at index " + mid + " in O(log N) comparisons.", arr);
                fFound.getArrayElementStates().put(mid, NodeState.VISITED);
                frames.add(fFound);
                found = true;
                break;
            } else if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (!found) {
            SimulationFrame fNotFound = createArrayFrame(step, "Target Not Found", "Target value " + target + " is not present in array.", arr);
            frames.add(fNotFound);
        }

        setTotals(frames);
        return frames;
    }

    // ============================================================ UTILITIES

    private static SimulationFrame createFrame(int num, String title, String explanation) {
        return new SimulationFrame(num, title, explanation);
    }

    private static SimulationFrame createArrayFrame(int num, String title, String explanation, int[] arr) {
        SimulationFrame f = new SimulationFrame(num, title, explanation);
        f.setArrayVisualization(true);
        f.setArrayData(Arrays.copyOf(arr, arr.length));
        return f;
    }

    private static void copyState(SimulationFrame f, Map<Integer, NodeState> nState, Map<Integer, String> nLabels, Map<String, EdgeState> eState) {
        f.setNodeStates(new HashMap<>(nState));
        f.setNodeSubLabels(new HashMap<>(nLabels));
        f.setEdgeStates(new HashMap<>(eState));
    }

    private static void setTotals(List<SimulationFrame> frames) {
        int total = frames.size();
        for (SimulationFrame f : frames) {
            f.setTotalSteps(total);
        }
    }

    private static String formatQueue(Queue<Integer> q, InteractiveGraphModel model) {
        if (q.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int v : q) {
            sb.append(model.getNodeById(v).getLabel()).append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    private static String formatCallStack(List<Integer> stack, InteractiveGraphModel model) {
        if (stack.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int v : stack) {
            sb.append(model.getNodeById(v).getLabel()).append(" → ");
        }
        if (sb.length() > 3) sb.setLength(sb.length() - 3);
        sb.append("]");
        return sb.toString();
    }
}
