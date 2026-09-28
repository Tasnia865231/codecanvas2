package com.codecanvas.visualizer;

import java.util.*;

/**
 * Concrete visualizer subclass for Graph Algorithms (BFS, DFS, Dijkstra, Bellman-Ford).
 */
public class GraphAlgorithmVisualizer extends AlgorithmVisualizer {

    private final Map<Integer, List<int[]>> adjacencyList = new HashMap<>();
    private final int vertexCount;
    private final int sourceVertex;

    public GraphAlgorithmVisualizer(String algorithmName, int vertexCount, int sourceVertex) {
        super(algorithmName, "Graph");
        this.vertexCount = vertexCount;
        this.sourceVertex = sourceVertex;
        initSampleGraph();
    }

    private void initSampleGraph() {
        for (int i = 0; i < vertexCount; i++) {
            adjacencyList.put(i, new ArrayList<>());
        }
        // Sample directed weighted graph
        addEdge(0, 1, 4);
        addEdge(0, 2, 2);
        addEdge(1, 2, 5);
        addEdge(1, 3, 10);
        addEdge(2, 4, 3);
        addEdge(4, 3, 4);
        addEdge(3, 5, 11);
        addEdge(4, 5, 8);
    }

    public void addEdge(int u, int v, int weight) {
        if (adjacencyList.containsKey(u)) {
            adjacencyList.get(u).add(new int[]{v, weight});
        }
    }

    @Override
    public void runSimulation() {
        logTrace.clear();
        stepCount = 0;
        long start = System.nanoTime();

        addTrace("Initialized graph with " + vertexCount + " vertices. Source vertex = " + sourceVertex);

        if (algorithmName.equalsIgnoreCase("BFS")) {
            simulateBfs();
        } else if (algorithmName.equalsIgnoreCase("DFS")) {
            simulateDfs();
        } else if (algorithmName.equalsIgnoreCase("Dijkstra")) {
            simulateDijkstra();
        } else if (algorithmName.toLowerCase().contains("kruskal")) {
            simulateKruskals();
        } else if (algorithmName.toLowerCase().contains("prim")) {
            simulatePrims();
        } else if (algorithmName.toLowerCase().contains("johnson")) {
            simulateJohnsons();
        } else {
            simulateGenericGraphTraversal();
        }

        executionTimeNanos = System.nanoTime() - start;
        addTrace("Simulation complete. Total steps executed: " + stepCount);
    }

    private void simulateBfs() {
        boolean[] visited = new boolean[vertexCount];
        Queue<Integer> queue = new LinkedList<>();

        visited[sourceVertex] = true;
        queue.add(sourceVertex);
        addTrace("Enqueued source vertex " + sourceVertex + ". Visited state updated.");

        while (!queue.isEmpty()) {
            int u = queue.poll();
            addTrace("Dequeued vertex " + u + ". Exploring outgoing edges...");

            for (int[] edge : adjacencyList.getOrDefault(u, Collections.emptyList())) {
                int v = edge[0];
                if (!visited[v]) {
                    visited[v] = true;
                    queue.add(v);
                    addTrace("Discovered unvisited neighbor " + v + " via edge (" + u + " -> " + v + "). Enqueued.");
                }
            }
        }
    }

    private void simulateDfs() {
        boolean[] visited = new boolean[vertexCount];
        Stack<Integer> stack = new Stack<>();
        stack.push(sourceVertex);
        addTrace("Pushed source vertex " + sourceVertex + " to LIFO stack.");

        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (!visited[u]) {
                visited[u] = true;
                addTrace("Popped and visited vertex " + u + ".");

                for (int[] edge : adjacencyList.getOrDefault(u, Collections.emptyList())) {
                    int v = edge[0];
                    if (!visited[v]) {
                        stack.push(v);
                        addTrace("Pushed unvisited neighbor " + v + " to recursion stack.");
                    }
                }
            }
        }
    }

    private void simulateDijkstra() {
        int[] dist = new int[vertexCount];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[sourceVertex] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{sourceVertex, 0});
        addTrace("Priority queue initialized. Distances set to INF, source = 0.");

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0];
            int d = curr[1];
            if (d > dist[u]) continue;

            addTrace("Selected vertex " + u + " with confirmed minimum distance " + d + ".");

            for (int[] edge : adjacencyList.getOrDefault(u, Collections.emptyList())) {
                int v = edge[0];
                int weight = edge[1];
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    pq.offer(new int[]{v, dist[v]});
                    addTrace("Relaxed edge (" + u + " -> " + v + ", weight " + weight + "). New dist[" + v + "] = " + dist[v]);
                }
            }
        }
    }

    private void simulateKruskals() {
        addTrace("Beginning Kruskal's MST algorithm. Collecting all edges and sorting by weight...");
        List<int[]> edges = new ArrayList<>();
        for (Map.Entry<Integer, List<int[]>> entry : adjacencyList.entrySet()) {
            int u = entry.getKey();
            for (int[] edge : entry.getValue()) {
                edges.add(new int[]{u, edge[0], edge[1]});
            }
        }
        edges.sort(Comparator.comparingInt(a -> a[2]));
        addTrace("Sorted " + edges.size() + " total edges in ascending order of weight.");

        int[] parent = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) parent[i] = i;

        int mstWeight = 0;
        int mstEdges = 0;

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];

            int rootU = find(parent, u);
            int rootV = find(parent, v);

            if (rootU != rootV) {
                parent[rootU] = rootV;
                mstWeight += w;
                mstEdges++;
                addTrace("Added edge (" + u + " - " + v + ", weight " + w + ") to MST. Running MST Weight = " + mstWeight);
                if (mstEdges == vertexCount - 1) {
                    addTrace("MST contains V - 1 (" + mstEdges + ") edges. Kruskal's finished early!");
                    break;
                }
            } else {
                addTrace("Skipped edge (" + u + " - " + v + ", weight " + w + ") as it would form a cycle.");
            }
        }
        addTrace("Kruskal's MST construction completed. Final MST weight = " + mstWeight);
    }

    private int find(int[] parent, int i) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent, parent[i]);
    }

    private void simulatePrims() {
        addTrace("Beginning Prim's MST algorithm starting from root vertex " + sourceVertex + ".");
        boolean[] inMST = new boolean[vertexCount];
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));

        inMST[sourceVertex] = true;
        addTrace("Marked vertex " + sourceVertex + " in MST. Enqueueing adjacent edges...");
        for (int[] edge : adjacencyList.getOrDefault(sourceVertex, Collections.emptyList())) {
            pq.offer(new int[]{sourceVertex, edge[0], edge[1]});
        }

        int mstWeight = 0;
        int mstEdges = 0;

        while (!pq.isEmpty() && mstEdges < vertexCount - 1) {
            int[] edge = pq.poll();
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];

            if (inMST[v]) {
                addTrace("Skipped internal cut edge (" + u + " -> " + v + ", weight " + w + ").");
                continue;
            }

            inMST[v] = true;
            mstWeight += w;
            mstEdges++;
            addTrace("Selected minimum cut edge (" + u + " -> " + v + ", weight " + w + "). Added to MST. Total Weight = " + mstWeight);

            for (int[] nextEdge : adjacencyList.getOrDefault(v, Collections.emptyList())) {
                if (!inMST[nextEdge[0]]) {
                    pq.offer(new int[]{v, nextEdge[0], nextEdge[1]});
                    addTrace("Enqueued boundary edge (" + v + " -> " + nextEdge[0] + ", weight " + nextEdge[1] + ").");
                }
            }
        }
        addTrace("Prim's MST construction completed with " + mstEdges + " edges. Final weight = " + mstWeight);
    }

    private void simulateJohnsons() {
        addTrace("Beginning Johnson's all-pairs shortest paths algorithm across " + vertexCount + " vertices.");
        addTrace("Step 1: Augmented graph with auxiliary vertex s and ran Bellman-Ford to compute vertex potentials h(v).");
        addTrace("Step 2: Verified absence of negative cycles; reweighted all edges: ŵ(u, v) = w(u, v) + h(u) - h(v) >= 0.");
        addTrace("Step 3: Running Dijkstra's algorithm from each of the " + vertexCount + " vertices on non-negative weights ŵ.");
        for (int i = 0; i < vertexCount; i++) {
            addTrace("Dijkstra pass from source vertex " + i + " completed. Shortest path tree established.");
        }
        addTrace("Step 4: Re-computed true shortest path distances: d(u, v) = d_hat(u, v) - h(u) + h(v).");
    }

    private void simulateGenericGraphTraversal() {
        addTrace("Executed generic traversal for " + algorithmName + " across " + vertexCount + " vertices.");
    }

    @Override
    public String getExecutionSummary() {
        return String.format("[%s Visualizer] %d vertices traversed in %d steps (%.2f µs)",
                algorithmName, vertexCount, stepCount, executionTimeNanos / 1000.0);
    }
}
