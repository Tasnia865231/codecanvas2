package com.codecanvas.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Repository and JSON generator/parser for algorithm MCQ questions.
 * Supplies 20 domain-specific questions per algorithm covering time complexity,
 * space complexity, data structures, invariants, and edge cases.
 */
public class McqBank {

    private static final Map<String, List<McqQuestion>> CACHE = new HashMap<>();

    public static List<McqQuestion> getQuestionsForAlgorithm(String algorithmName) {
        if (algorithmName == null) algorithmName = "BFS";
        String key = algorithmName.trim();
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }

        List<McqQuestion> questions = buildQuestionsJson(key);
        CACHE.put(key, questions);
        return questions;
    }

    private static List<McqQuestion> buildQuestionsJson(String algo) {
        String slug = algo.toLowerCase()
                .replace("'", "")
                .replace("’", "")
                .replace(" ", "-")
                .trim();

        // 1. Try loading from classpath JSON files (/mcq/{slug}.json or /com/codecanvas/mcq/{slug}.json)
        String[] possiblePaths = {
            "/mcq/" + slug + ".json",
            "/mcq/" + slug.replace("-", "") + ".json",
            "/com/codecanvas/mcq/" + slug + ".json"
        };

        for (String path : possiblePaths) {
            try (java.io.InputStream is = McqBank.class.getResourceAsStream(path)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    String jsonPayload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    List<McqQuestion> list = parseJsonArray(jsonPayload);
                    if (list != null && !list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Could not load MCQ JSON from " + path + ": " + ex.getMessage());
            }
        }

        // 2. Fallback to programmatic generator
        String jsonPayload = generateJsonForAlgorithm(algo);
        return parseJsonArray(jsonPayload);
    }

    private static List<McqQuestion> parseJsonArray(String jsonPayload) {
        List<McqQuestion> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(jsonPayload);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                JSONArray optsArr = obj.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optsArr.length(); j++) {
                    options.add(optsArr.getString(j));
                }
                list.add(new McqQuestion(
                        obj.getInt("id"),
                        obj.getString("algorithm"),
                        obj.getString("question"),
                        options,
                        obj.getInt("answerIndex"),
                        obj.getString("explanation")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String generateJsonForAlgorithm(String algo) {
        JSONArray arr = new JSONArray();

        String lower = algo.toLowerCase();
        String[][] bank;
        if (lower.contains("bfs")) bank = getBfsBank();
        else if (lower.contains("dfs")) bank = getDfsBank();
        else if (lower.contains("dijkstra")) bank = getDijkstraBank();
        else if (lower.contains("bellman")) bank = getBellmanFordBank();
        else if (lower.contains("floyd")) bank = getFloydWarshallBank();
        else if (lower.contains("johnson")) bank = getJohnsonsBank();
        else if (lower.contains("kruskal")) bank = getKruskalBank();
        else if (lower.contains("prim")) bank = getPrimsBank();
        else if (lower.contains("quick")) bank = getQuickSortBank();
        else if (lower.contains("merge")) bank = getMergeSortBank();
        else bank = getGenericBank(algo);

        for (int i = 0; i < bank.length; i++) {
            String[] row = bank[i];
            JSONObject q = new JSONObject();
            q.put("id", i + 1);
            q.put("algorithm", algo);
            q.put("question", row[0]);
            JSONArray opts = new JSONArray();
            opts.put(row[1]);
            opts.put(row[2]);
            opts.put(row[3]);
            opts.put(row[4]);
            q.put("options", opts);
            q.put("answerIndex", Integer.parseInt(row[5]));
            q.put("explanation", row[6]);
            arr.put(q);
        }

        return arr.toString();
    }

    private static String[][] getBfsBank() {
        return new String[][]{
            {"What primary data structure is used to implement Breadth-First Search?", "Stack", "Queue (FIFO)", "Priority Queue", "Binary Search Tree", "1", "BFS traverses level by level, necessitating a First-In-First-Out (FIFO) Queue."},
            {"What is the time complexity of BFS on an adjacency list representation with V vertices and E edges?", "O(V * E)", "O(V^2)", "O(V + E)", "O(E log V)", "2", "Each vertex is visited once and each adjacent edge is inspected once, yielding O(V + E)."},
            {"What is the space complexity of BFS?", "O(1)", "O(V)", "O(E)", "O(V * E)", "1", "The queue and the visited set hold up to V vertices in the worst case."},
            {"Can standard BFS find the shortest path in a graph with non-uniform positive edge weights?", "Yes, always", "No, it only guarantees shortest paths for unweighted graphs", "Yes, if no negative cycles exist", "Only on directed acyclic graphs", "1", "BFS only computes shortest hop-count paths; for weighted graphs, Dijkstra's algorithm is required."},
            {"In BFS, when is a vertex marked as visited?", "When it is dequeued", "When it is enqueued", "After all neighbors are explored", "Never", "1", "Vertices must be marked visited upon enqueuing to prevent redundant insertion into the queue."},
            {"Which problem cannot be directly solved using Breadth-First Search?", "Connected components in undirected graphs", "Shortest path on unweighted maze", "Negative cycle detection in general weighted graphs", "Bipartite graph validation", "2", "Detecting negative weight cycles requires Bellman-Ford or SPFA, as BFS does not account for edge weights."},
            {"What does the BFS tree height represent from the source node?", "The maximum edge weight", "The shortest distance in terms of edge count to each node", "The degree of the source node", "The chromatic number", "1", "BFS discovers vertices in increasing order of their hop count from the root."},
            {"What is the time complexity of BFS if the graph is represented as an adjacency matrix?", "O(V + E)", "O(V^2)", "O(E log V)", "O(V log E)", "1", "Iterating over all rows in a V x V matrix requires inspecting V entries per vertex, taking O(V^2)."},
            {"Which graph traversal discovers all 1-hop neighbors before any 2-hop neighbors?", "Depth-First Search", "Breadth-First Search", "Topological Sort", "Kruskal's Algorithm", "1", "BFS explores all vertices at depth d before moving to depth d + 1."},
            {"In a complete graph K_n, what is the maximum number of elements in the BFS queue at any time?", "1", "n - 1", "n", "n * (n - 1) / 2", "1", "From the source, all remaining n - 1 vertices are enqueued in the very first level."},
            {"Can BFS be used to detect cycles in an undirected graph?", "No, only DFS can detect cycles", "Yes, if an adjacent vertex is already visited and not the parent", "Only if the graph is planar", "Only if vertices are numbered sequentially", "1", "If an explored neighbor is visited and not the immediate parent in the BFS tree, a cross-edge / cycle exists."},
            {"What happens if a graph is disconnected and BFS is run starting from vertex 0?", "It visits all vertices", "It throws an IndexOutOfBoundsException", "It only visits vertices in the component containing vertex 0", "It enters an infinite loop", "2", "BFS only explores vertices reachable from the designated source vertex."},
            {"What is the typical memory consumption behavior of BFS compared to DFS on broad trees?", "BFS consumes significantly more memory due to wide level queues", "BFS consumes less memory than DFS", "Both consume exactly identical memory", "BFS consumes O(1) auxiliary memory", "0", "BFS queue stores an entire level, which can be O(b^d) where b is branching factor and d is depth."},
            {"What is 0-1 BFS?", "BFS running on graphs with only 0 or 1 vertices", "Shortest path algorithm using a Double-Ended Queue (Deque) for weights 0 and 1", "BFS with a single boolean flag", "DFS simulated using binary flags", "1", "0-1 BFS inserts 0-weight edges at the front and 1-weight edges at the back of a Deque in O(V + E) time."},
            {"Which classification does the BFS algorithm fall under?", "Greedy algorithm", "Divide and conquer", "Graph traversal / Search", "Dynamic programming", "2", "BFS is fundamentally a systematic graph traversal and search algorithm."},
            {"In an unweighted bipartite graph, BFS can check 2-colorability by:", "Assigning alternate colors to alternating levels", "Coloring all visited nodes blue", "Reversing directed edges", "Calculating the determinant", "0", "Nodes at even levels get one color, and nodes at odd levels get the other."},
            {"What edge type does NOT appear in an undirected BFS forest?", "Tree edges", "Cross edges", "Back edges spanning multiple levels", "Both tree and cross edges", "2", "In undirected BFS, non-tree edges can only connect vertices in the same level or adjacent levels."},
            {"How does BFS verify if an undirected graph is connected?", "Count if visited count equals |V| after single traversal", "Check if queue size ever reaches zero", "Verify if time complexity is O(V)", "Check if edge count is even", "0", "If visited.size() == V after running BFS from any node, the graph is connected."},
            {"If a graph has negative edges but is unweighted, does BFS work?", "Graphs cannot be unweighted and have negative weights simultaneously", "Yes, BFS does not inspect weights", "No, it crashes", "Only on DAGs", "0", "By definition, an unweighted graph treats all edges as uniform unit cost (weight = 1)."},
            {"What is the diameter of an unweighted tree computable with BFS?", "Running BFS twice: first from arbitrary node to farthest u, then from u to farthest v", "Running BFS once from the root", "Sum of all edge weights", "Number of leaf nodes", "0", "A double BFS reliably computes the diameter (longest path) of any unweighted tree in linear time."}
        };
    }

    private static String[][] getDfsBank() {
        return new String[][]{
            {"Which data structure is inherently used by Depth-First Search?", "FIFO Queue", "LIFO Stack (or recursion call stack)", "Priority Queue", "Hash Table", "1", "DFS dives down recursive paths using the LIFO system call stack or an explicit Stack."},
            {"What is the time complexity of DFS using an adjacency list?", "O(V + E)", "O(V^2)", "O(E log V)", "O(V log V)", "0", "DFS visits every vertex and traverses each edge once, achieving O(V + E)."},
            {"What is the worst-case space complexity of recursive DFS?", "O(1)", "O(E)", "O(V)", "O(V^2)", "2", "In a degenerate chain graph, the recursion call stack can grow to depth V."},
            {"Which algorithmic application relies primarily on DFS discovery and finish times?", "Dijkstra's Shortest Path", "Kosaraju's Strongly Connected Components", "Prim's Minimum Spanning Tree", "Kruskal's MST", "1", "Kosaraju and Tarjan algorithms use DFS timestamps to find strongly connected components."},
            {"Can DFS be used to produce a valid Topological Sort of a DAG?", "No, only BFS can do topological sort", "Yes, by inserting vertices into a list in decreasing order of finish time", "Only if the DAG has no sinks", "Only for binary trees", "1", "Post-order reversal (decreasing finish time) yields a valid topological ordering."},
            {"What type of edge in a directed DFS indicates the presence of a directed cycle?", "Tree edge", "Forward edge", "Cross edge", "Back edge", "3", "A back edge points from a descendant to an active ancestor on the recursion stack, forming a cycle."},
            {"In DFS on an undirected graph, what types of edges can exist?", "Tree edges and Cross edges only", "Tree edges and Back edges only", "Forward edges and Cross edges only", "All four edge types", "1", "Undirected DFS trees contain only Tree edges and Back edges."},
            {"How does Tarjan's algorithm detect bridges (cut edges) using DFS?", "Counting vertex degrees", "Comparing discovery time tin[u] with low-link value low[v]", "Running Dijkstra from each edge", "Inverting edge directions", "1", "If low[v] > tin[u], edge (u, v) is a bridge because v has no alternative path to ancestors of u."},
            {"What is an articulation point (cut vertex)?", "A vertex whose removal increases the number of connected components", "A leaf vertex in the DFS tree", "The root node of every graph", "A vertex with maximum degree", "0", "An articulation point disconnects the graph if removed."},
            {"What is the maximum recursion depth of DFS on a tree with N nodes?", "N - 1", "log N", "N / 2", "1", "0", "A degenerate linear line tree has depth N - 1."},
            {"What is the difference between Pre-order and Post-order traversal in DFS?", "Pre-order processes upon first visit; Post-order processes after all descendants finish", "Pre-order uses queues; Post-order uses stacks", "They are identical in graphs", "Post-order runs in reverse time", "0", "Pre-order records discovery; Post-order records completion of all subtree explorations."},
            {"What is Iterative Deepening DFS (IDDFS)?", "DFS that searches breadth-first using increasing depth limits", "DFS with two stacks", "DFS that never backtracks", "Parallel DFS", "0", "IDDFS combines the space efficiency of DFS with the optimality of BFS."},
            {"What is the time complexity of DFS on an adjacency matrix?", "O(V + E)", "O(V^2)", "O(E^2)", "O(V log E)", "1", "Checking all neighbors of V vertices requires scanning each row of size V, totaling O(V^2)."},
            {"Can DFS be used to generate a maze?", "Yes, randomized DFS with backtracking creates perfect mazes", "No, DFS cannot backtrack", "Only for 2D circles", "Only if weights are negative", "0", "Randomized DFS with backtracking generates perfect spanning tree mazes."},
            {"How does DFS determine if an undirected graph contains a cycle?", "Finding an edge to a visited node that is not the parent", "Counting edges > V", "Checking if stack is empty", "Finding a negative weight", "0", "A back edge to an already-visited non-parent vertex proves a cycle."},
            {"What is the low-link value low[u] in Tarjan's algorithm?", "The lowest discovery time reachable from u via at most one back edge", "The lowest degree neighbor", "The shortest distance to root", "The index in adjacency list", "0", "low[u] is the earliest visited vertex reachable from u's subtree via back edges."},
            {"In bipartite graph checking via DFS, an odd-length cycle is detected when:", "A neighbor already has the same color as the current node", "A node has degree 0", "All nodes are visited", "Edge weight is zero", "0", "If an adjacent vertex has the identical color partition, the graph cannot be 2-colored."},
            {"What is the space complexity of iterative DFS with an explicit Stack?", "O(V)", "O(V^2)", "O(1)", "O(E * V)", "0", "The explicit stack holds at most V vertices."},
            {"Eulerian path existence in a directed graph requires DFS to check:", "Connectivity of non-zero degree vertices and in-degree/out-degree balance", "Only cycle freedom", "Tree height <= 3", "All edge weights >= 0", "0", "Eulerian paths require balanced degrees and a single connected component for active edges."},
            {"When does DFS fail to find the shortest path in unweighted graphs?", "Whenever there are alternate paths with varying hop counts", "Never", "Only when cycles exist", "Only when V > 100", "0", "DFS plunges deep along the first branch encountered, potentially finding a convoluted long path."}
        };
    }

    private static String[][] getDijkstraBank() {
        return new String[][]{
            {"What is Dijkstra's algorithm used for?", "All-pairs shortest paths with negative cycles", "Single-source shortest path with non-negative edge weights", "Minimum spanning tree on unweighted graphs", "Maximum flow", "1", "Dijkstra computes single-source shortest paths on graphs with non-negative weights."},
            {"What is the time complexity of Dijkstra using a Min-Indexed Binary Heap?", "O(V^2)", "O((V + E) log V)", "O(V * E)", "O(V^3)", "1", "Extract-min takes O(log V) and edge relaxations take O(log V) per edge, totaling O((V + E) log V)."},
            {"Why does standard Dijkstra fail on graphs with negative edge weights?", "It enters an infinite loop", "Greedy assumption breaks because a settled node's distance could be decreased later", "Throws ArithmeticException", "Priority queue underflows", "1", "Dijkstra marks vertices as finalized once extracted; negative edges can invalidate that finalized distance."},
            {"What happens if all edge weights in a graph are equal to 1?", "Dijkstra runs in O(1)", "Dijkstra behaves equivalently to standard BFS", "Dijkstra fails", "Priority queue is unnecessary", "1", "When all weights are uniform positive values, Dijkstra's priority queue behaves like a FIFO queue (BFS)."},
            {"Which data structure achieves Dijkstra's theoretical lower bound of O(E + V log V)?", "Binary Heap", "Fibonacci Heap", "Linked List", "AVL Tree", "1", "Fibonacci Heap offers O(1) amortized decrease-key operations."},
            {"What is edge relaxation in Dijkstra's algorithm?", "Decreasing edge weight", "Updating dist[v] to dist[u] + weight(u, v) if it offers a shorter path", "Removing an edge from the graph", "Inverting edge direction", "1", "Relaxation checks if traveling through u yields a strictly shorter route to neighbor v."},
            {"What is the space complexity of Dijkstra's algorithm?", "O(1)", "O(V)", "O(V^2)", "O(E * V)", "1", "Distance array, visited set, and priority queue consume O(V) space."},
            {"Can Dijkstra be used on Directed Acyclic Graphs (DAGs)?", "Yes, though topological sort relaxation is faster O(V + E)", "No, Dijkstra only works on undirected graphs", "Only if weights are negative", "Never", "0", "Dijkstra works on DAGs with non-negative weights, but topological sort can solve DAG shortest paths in linear time."},
            {"What is the initial distance assigned to the source vertex in Dijkstra?", "0", "1", "Infinity", "-1", "0", "Distance to the source vertex is 0, while all other vertices begin at infinity."},
            {"Which algorithmic paradigm does Dijkstra's algorithm employ?", "Dynamic Programming", "Greedy Paradigm", "Backtracking", "Divide and Conquer", "1", "Dijkstra greedily chooses the unvisited vertex with the minimum tentative distance at each step."},
            {"Can Dijkstra find the shortest path from single source to a single target?", "Yes, terminate early as soon as the target vertex is extracted from the priority queue", "No, it must always visit all vertices in the graph", "Only if graph is planar", "Only if V < 10", "0", "Once the target vertex is popped from the min-heap, its shortest path is guaranteed settled."},
            {"What is A* Search in relation to Dijkstra?", "Dijkstra with a heuristic function h(n) biasing exploration toward the goal", "Unrelated sorting algorithm", "BFS with recursion", "Dijkstra with negative cycles", "0", "A* generalizes Dijkstra by incorporating an admissible heuristic estimate h(n)."},
            {"If a graph has negative edge weights but NO negative cycles, can Dijkstra be fixed simply by adding a constant C to all edges?", "Yes, adding C makes all edges positive and preserves paths", "No, paths with more edges are unfairly penalized by C * k", "Yes, this is Johnson's technique", "Only if C > 100", "1", "Adding a constant adds k*C to a k-edge path, skewing preferences toward fewer edges rather than lower original sum."},
            {"What is the time complexity of Dijkstra implemented with an unsorted array?", "O(V^2)", "O(V + E)", "O(E log V)", "O(log V)", "0", "Finding the minimum takes O(V) per step across V steps, yielding O(V^2), optimal for dense graphs where E ≈ V^2."},
            {"How are paths reconstructed after running Dijkstra?", "By following parent pointers backwards from target to source", "Running BFS on the distance array", "Multiplying distances", "Re-sorting edges", "0", "Storing parent[v] = u during relaxation allows linear-time path backtracking."},
            {"In a graph with V vertices and 0 edges, what does Dijkstra return for unreachable nodes?", "0", "Integer.MAX_VALUE (Infinity)", "-1", "Throws an Exception", "1", "Unreachable nodes retain their initial tentative distance of Infinity."},
            {"Can Dijkstra handle self-loops with positive weight?", "Yes, they are ignored because dist[u] + weight > dist[u]", "No, it enters an infinite loop", "Only if self-loop weight is 0", "Throws error", "0", "Positive self-loops cannot reduce the distance to u, so relaxation naturally skips them."},
            {"What happens if Dijkstra is executed on an undirected graph?", "It works seamlessly, treating each undirected edge as two symmetric directed edges", "It fails due to cycles", "It requires negative cycle checks", "Only works if tree", "0", "Undirected edges (u, v, w) are represented as two opposing directed edges with non-negative weight w."},
            {"What is the maximum number of times any vertex is popped from the Priority Queue in standard indexed Dijkstra?", "Once", "Twice", "V times", "Degree of the vertex", "0", "Each vertex is marked finalized upon its first extraction from the priority queue."},
            {"Which algorithm is a predecessor to Dijkstra for unweighted single-source shortest path?", "Breadth-First Search (BFS)", "Depth-First Search (DFS)", "QuickSort", "Kruskal's", "0", "BFS is the unweighted unit-cost specialization of Dijkstra's algorithm."}
        };
    }

    private static String[][] getBellmanFordBank() {
        return new String[][]{
            {"What distinguishes Bellman-Ford from Dijkstra's algorithm?", "It handles negative edge weights and detects negative cycles", "It is faster than Dijkstra", "It only works on DAGs", "It uses a FIFO queue", "0", "Bellman-Ford supports negative weights and detects negative cycles by relaxing edges V - 1 times."},
            {"What is the time complexity of Bellman-Ford on a graph with V vertices and E edges?", "O(V * E)", "O((V + E) log V)", "O(V^2)", "O(V^3)", "0", "Bellman-Ford iterates |V| - 1 times over all |E| edges, yielding O(V * E)."},
            {"Why does Bellman-Ford relax edges exactly |V| - 1 times?", "A simple shortest path in a graph with V vertices has at most V - 1 edges", "Because V is prime", "To allow recursion", "It is an arbitrary constant", "0", "The longest possible simple path without cycles contains at most |V| - 1 edges."},
            {"How does Bellman-Ford detect a negative weight cycle?", "If an edge can still be relaxed on the V-th iteration", "If dist[source] < 0", "If all distances are 0", "If vertex count is odd", "0", "If any distance decreases on the |V|-th pass, a negative weight cycle exists."},
            {"What is the space complexity of the Bellman-Ford algorithm?", "O(V)", "O(E * V)", "O(1)", "O(V^2)", "0", "It only requires a 1D distance array of size V."},
            {"What happens to the shortest path problem when a graph contains a negative cycle reachable from the source?", "Shortest path is undefined because distance can decrease indefinitely to -Infinity", "Shortest path is 0", "Shortest path is the longest cycle", "Returns Integer.MAX_VALUE", "0", "Traversing the negative cycle infinitely many times reduces the path cost without bound."},
            {"Can Bellman-Ford solve shortest paths on a Directed Acyclic Graph (DAG)?", "Yes, in O(V * E) time, though DAG relaxation in topological order is faster O(V + E)", "No, only graphs with cycles", "Only if weights are positive", "Never", "0", "It works correctly on DAGs, but topological sorting is much more efficient."},
            {"What is the Shortest Path Faster Algorithm (SPFA)?", "A queue-based optimization of Bellman-Ford", "A version of QuickSort", "A hardware accelerator", "A genetic algorithm", "0", "SPFA optimizes Bellman-Ford by only relaxing vertices whose distances changed in the previous pass."},
            {"Which routing protocol in computer networks uses the Bellman-Ford algorithm?", "Routing Information Protocol (RIP)", "Border Gateway Protocol (BGP)", "OSPF", "HTTP", "0", "RIP uses the distance-vector routing protocol based on Bellman-Ford."},
            {"If a graph has V vertices and V edges with no negative cycle, how many passes are needed?", "At most V - 1 passes", "V^2 passes", "1 pass", "Zero passes", "0", "V - 1 passes are mathematically sufficient for any cycle-free shortest path."},
            {"Can Bellman-Ford be stopped early if no distance changes during a pass?", "Yes, this optimization terminates when no edges are relaxed in a full iteration", "No, it must always complete V-1 passes", "Only if graph is complete", "Only if source is vertex 0", "0", "If an entire pass yields zero relaxations, the distances have converged."},
            {"What is the initial tentative distance for all non-source vertices in Bellman-Ford?", "Infinity (Integer.MAX_VALUE)", "0", "-1", "1000", "0", "Non-source nodes start at infinity, and the source starts at 0."},
            {"What edge data structure is most convenient for Bellman-Ford?", "A flat list or array of all edges (u, v, weight)", "Adjacency matrix", "Binary heap", "B-Tree", "0", "Because each pass simply iterates over all edges, a flat edge list is ideal."},
            {"How does Bellman-Ford handle disconnected components?", "Vertices unreachable from source remain at Infinity", "Throws an Exception", "Connects them with 0-weight edges", "Halts immediately", "0", "Unreachable vertices never satisfy dist[u] + w < dist[v] since dist[u] is Infinity."},
            {"Is Bellman-Ford a greedy algorithm?", "No, it is based on Dynamic Programming", "Yes, strictly greedy like Dijkstra", "It is divide-and-conquer", "It is randomized", "0", "Bellman-Ford iteratively builds solutions for paths of length k using subproblems of length k-1 (DP)."},
            {"If a negative cycle exists but is NOT reachable from the source vertex, does Bellman-Ford detect it?", "No, it only detects negative cycles reachable from the designated source", "Yes, always", "It crashes", "It prints a warning", "0", "Relaxations only propagate along paths reachable from the source vertex."},
            {"How can Bellman-Ford be adapted to find ANY negative cycle in a graph?", "Add a virtual source connected to all vertices with 0-weight edges", "Reverse all edge signs", "Multiply weights by -1", "Run BFS first", "0", "A virtual super-source with 0-weight edges to all nodes makes all cycles reachable."},
            {"What is the worst-case time complexity of Bellman-Ford on a complete graph?", "O(V^3)", "O(V^2)", "O(V log V)", "O(V)", "0", "On a complete graph E = O(V^2), so O(V * E) = O(V^3)."},
            {"Can Bellman-Ford handle undirected graphs with negative edges?", "An undirected negative edge constitutes a negative cycle of length 2 (u <-> v)", "Yes, without any issues", "Only if weights are even", "Only if V < 5", "0", "An undirected edge with negative weight allows bouncing back and forth endlessly, forming a negative 2-cycle."},
            {"What is the return value of standard Bellman-Ford?", "Boolean flag indicating whether a negative cycle exists, alongside distance array", "Only the execution time", "A single integer", "Sorted list of edges", "0", "It returns the distances and true/false indicating cycle presence."}
        };
    }

    private static String[][] getFloydWarshallBank() {
        return new String[][]{
            {"What type of shortest path problem does Floyd-Warshall solve?", "All-Pairs Shortest Path (APSP)", "Single-Source Shortest Path (SSSP)", "Single-Pair Shortest Path", "Longest Common Subsequence", "0", "Floyd-Warshall computes shortest paths between every pair of vertices in a weighted graph."},
            {"What is the time complexity of the Floyd-Warshall algorithm?", "O(V^3)", "O(V^2)", "O(V * E)", "O(E log V)", "0", "Three nested loops from 1 to V yield O(V^3) time complexity."},
            {"What algorithmic paradigm does Floyd-Warshall utilize?", "Dynamic Programming", "Greedy Algorithm", "Divide and Conquer", "Backtracking", "0", "It defines dist[i][j][k] as shortest path using intermediate vertices from {1..k}."},
            {"What is the space complexity of standard Floyd-Warshall?", "O(V^2)", "O(V^3)", "O(V)", "O(1)", "0", "The distance matrix dist[i][j] requires V x V = O(V^2) memory."},
            {"How does Floyd-Warshall detect negative weight cycles?", "If any diagonal entry dist[i][i] becomes negative", "If any off-diagonal entry is zero", "If execution takes > 1 second", "If all entries are negative", "0", "A negative value on the main diagonal dist[i][i] < 0 indicates a path from i back to i with negative cost."},
            {"In the recurrence dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j]), what does k represent?", "The candidate intermediate vertex being evaluated", "The number of edges", "The recursion depth", "The source node", "0", "k is the intermediate vertex through which paths from i to j are relaxed."},
            {"Can Floyd-Warshall operate on graphs with negative edge weights (provided no negative cycles exist)?", "Yes, it correctly calculates shortest paths with negative weights", "No, all weights must be positive", "Only for trees", "Only if undirected", "0", "Floyd-Warshall seamlessly handles negative edge weights as long as no negative cycle is reachable."},
            {"What is the transitive closure of a directed graph computable by Warshall's variant?", "Reachability matrix indicating whether a path exists between any two vertices", "Spanning tree", "MST weight", "Topological order", "0", "Warshall's algorithm uses boolean OR/AND operations to determine pairwise reachability."},
            {"What is the initial value of dist[i][i] in Floyd-Warshall?", "0", "Infinity", "-1", "1", "0", "The distance from any vertex to itself is initially 0."},
            {"For dense graphs where E ≈ V^2, how does Floyd-Warshall compare to running Dijkstra V times?", "Floyd-Warshall O(V^3) has simpler loops and better cache locality than V * O(V^2 log V)", "Floyd-Warshall is 100x slower", "Dijkstra is always superior", "Floyd-Warshall cannot run on dense graphs", "0", "On dense graphs, Floyd-Warshall's tight 3-loop structure offers excellent performance and cache locality."},
            {"How are paths reconstructed in Floyd-Warshall?", "Using a predecessor matrix next[i][j] storing the intermediate vertex", "By re-running BFS", "Looking up hash tables", "Inverting matrix", "0", "A predecessor matrix next[i][j] enables reconstructing the exact sequence of vertices."},
            {"What happens if dist[i][k] is Infinity in the inner loop?", "The relaxation through k can be skipped because no path exists from i to k", "Throws NullPointerException", "Distance becomes negative", "Terminates the program", "0", "If dist[i][k] == INF, there is no route through k, so the inner relaxation is skipped."},
            {"What is the order of loops in standard Floyd-Warshall?", "k (intermediate) must be the outermost loop", "i (source) must be outermost", "j (target) must be outermost", "Loop order does not matter", "0", "k must be outermost to ensure subproblems for intermediate vertices 1..k-1 are fully computed."},
            {"Can Floyd-Warshall be used to find the graph diameter?", "Yes, by finding the maximum value in the final distance matrix", "No", "Only for DAGs", "Only if V < 5", "0", "The graph diameter is the maximum shortest distance between any pair of connected vertices."},
            {"What is the graph center computable using Floyd-Warshall?", "The vertex with the minimum eccentricity (maximum distance to any other node)", "The node with ID 0", "The vertex with highest degree", "The centroid of weights", "0", "The center minimizes the maximum shortest distance to any other vertex."},
            {"If two nodes i and j have no path between them, what is dist[i][j]?", "Infinity (or a sentinel INF value like 1e9)", "0", "-1", "NaN", "0", "Unconnected pairs remain at the sentinel INF value."},
            {"Does Floyd-Warshall work on graphs with directed edges?", "Yes, it works identically on directed and undirected graphs", "No, only undirected", "Only bipartite graphs", "Only trees", "0", "Adjacency matrices natively represent directed edges dist[u][v] != dist[v][u]."},
            {"What is the bitwise version of Warshall's algorithm known as?", "Bitset transitive closure optimization using word-level parallelism", "BitSort", "Huffman coding", "RSA", "0", "Bitsets accelerate Warshall's reachability algorithm by 64x using 64-bit word bitwise OR operations."},
            {"Can Floyd-Warshall find the widest path (maximum bottleneck capacity)?", "Yes, by replacing min with max and + with min", "No", "Only on trees", "Only with unit weights", "0", "Modifying the semi-ring algebra allows computing maximum bottleneck capacity paths."},
            {"What happens if a negative cycle exists between vertices reachable from i to j?", "dist[i][j] can become arbitrarily negative or -INF", "The program crashes", "Output is 0", "Matrix is cleared", "0", "Shortest path cost between those vertices decreases without bound."}
        };
    }

    private static String[][] getJohnsonsBank() {
        return new String[][]{
            {"What is the primary purpose of Johnson's algorithm?", "All-pairs shortest paths on sparse graphs with negative edge weights", "Maximum flow", "String matching", "Minimum cut", "0", "Johnson's algorithm efficiently computes APSP on sparse graphs by reweighting edges."},
            {"What is the time complexity of Johnson's algorithm using Fibonacci Heaps?", "O(V^2 log V + V * E)", "O(V^3)", "O(V * E)", "O(E log V)", "0", "Reweighting takes O(V * E), followed by V runs of Dijkstra taking O(V^2 log V + V * E)."},
            {"How does Johnson's algorithm eliminate negative edge weights without altering shortest paths?", "Reweighting using vertex potentials h(u) computed by Bellman-Ford", "Adding a global constant C to all edges", "Deleting negative edges", "Squaring all weights", "0", "Edge reweighting w'(u, v) = w(u, v) + h(u) - h(v) ensures non-negative weights and preserves path optimality."},
            {"Why is adding a constant C to all edge weights invalid for shortest paths?", "Paths with more edges receive more penalties (k * C), distorting path comparisons", "It causes integer overflow", "It makes all weights negative", "It changes the graph topology", "0", "A 5-edge path receives 5C while a 1-edge path receives 1C, favoring fewer edges over lower true sum."},
            {"Which algorithm is invoked first in Johnson's algorithm to compute vertex potentials h(v)?", "Bellman-Ford from a new virtual super-source", "Kruskal's algorithm", "QuickSort", "BFS", "0", "Bellman-Ford runs from a virtual source node connected to all vertices with weight 0."},
            {"What happens if Bellman-Ford detects a negative cycle during the initial phase of Johnson's algorithm?", "The algorithm terminates reporting that negative cycles exist", "It ignores the cycle", "It removes the cycle edges", "It resets weights to 0", "0", "A negative cycle makes shortest paths undefined, so Johnson's algorithm aborts."},
            {"After reweighting edges, which algorithm is run V times in Johnson's algorithm?", "Dijkstra's algorithm", "Breadth-First Search", "Depth-First Search", "Floyd-Warshall", "0", "Dijkstra's algorithm is run once from each vertex since all reweighted edges are guaranteed non-negative."},
            {"How is the true distance dist(u, v) recovered from the reweighted distance dist'(u, v)?", "dist(u, v) = dist'(u, v) - h(u) + h(v)", "dist(u, v) = dist'(u, v) * 2", "dist(u, v) = dist'(u, v) + h(u)", "dist(u, v) = dist'(u, v) / V", "0", "Telescoping cancellation yields dist(u, v) = dist'(u, v) - h(u) + h(v)."},
            {"On which type of graph is Johnson's algorithm strictly faster than Floyd-Warshall?", "Sparse graphs where E << V^2", "Complete graphs", "Graphs with 0 edges", "Only trees", "0", "On sparse graphs, O(V^2 log V + V * E) is significantly faster than Floyd-Warshall's O(V^3)."},
            {"What is the weight of edges connecting the virtual super-source node s to every other vertex in Johnson's?", "0", "1", "-1", "Infinity", "0", "Virtual edges have weight 0 so that h(v) represents the shortest distance from s to v."},
            {"Why are reweighted edge costs w'(u, v) guaranteed to be >= 0?", "By triangle inequality in shortest paths: h(v) <= h(u) + w(u, v)", "Because of random seeds", "Because h values are positive", "Because edges are undirected", "0", "Shortest path triangle inequality h(v) <= h(u) + w(u, v) implies w(u, v) + h(u) - h(v) >= 0."},
            {"What is the space complexity of Johnson's algorithm?", "O(V^2) to store the all-pairs output distance matrix", "O(1)", "O(V^3)", "O(E * V^2)", "0", "Storing the all-pairs shortest path matrix requires O(V^2) memory."},
            {"If a graph has only non-negative edge weights from the start, does Johnson's algorithm require Bellman-Ford?", "No, it can directly run Dijkstra V times without reweighting", "Yes, Bellman-Ford is always mandatory", "It throws an error", "Only if undirected", "0", "Without negative weights, potentials are not needed and V runs of Dijkstra suffice."},
            {"Can Johnson's algorithm be parallelized?", "Yes, the V independent Dijkstra runs can execute concurrently across threads", "No, Dijkstra runs must be strictly sequential", "Only on GPUs", "Only for 2 threads", "0", "Each Dijkstra run from vertex i is completely independent, allowing trivial multi-core parallelization."},
            {"What is the reweighted length of any cycle in Johnson's algorithm?", "Equal to its original length", "Zero", "Twice its original length", "-Infinity", "0", "In a cycle, sum of h(u) - h(v) telescopes to 0, leaving the cycle sum identical to the original."},
            {"Who invented Johnson's algorithm?", "Donald B. Johnson (1977)", "Robert Floyd", "Edsger Dijkstra", "Richard Bellman", "0", "Donald B. Johnson published the algorithm in 1977."},
            {"In a graph with V = 1000 and E = 2000, which APSP algorithm is preferable?", "Johnson's algorithm (sparse)", "Floyd-Warshall", "Brute force DFS", "Selection Sort", "0", "With E = 2V, Johnson's algorithm runs orders of magnitude faster than Floyd-Warshall's 10^9 operations."},
            {"What is the vertex potential h(u) mathematically equal to?", "The shortest path distance from the virtual source s to u", "The degree of vertex u", "The index of u", "The in-degree minus out-degree", "0", "h(u) = dist(s, u) computed by Bellman-Ford."},
            {"Does Johnson's algorithm work on disconnected graphs?", "Yes, the virtual source connects to all vertices ensuring reachability in the potential phase", "No", "Only if connected", "Only on bipartite graphs", "0", "The virtual source has directed edges to every vertex, ensuring full reachability."},
            {"What is the primary bottleneck in Johnson's algorithm on very dense graphs?", "The V Dijkstra runs approach O(V^3), losing advantage over Floyd-Warshall", "Bellman-Ford", "Memory allocation", "String parsing", "0", "When E ≈ V^2, V runs of Dijkstra take O(V^3), where Floyd-Warshall's simpler loops are faster in practice."}
        };
    }

    private static String[][] getQuickSortBank() {
        return new String[][]{
            {"What is the average time complexity of Quick Sort?", "O(N log N)", "O(N^2)", "O(N)", "O(log N)", "0", "On average, balanced partitioning divides the array in half, achieving O(N log N)."},
            {"What is the worst-case time complexity of Quick Sort?", "O(N^2)", "O(N log N)", "O(N)", "O(N^3)", "0", "When partitions are severely unbalanced (e.g., sorted array with first/last element pivot), complexity degrades to O(N^2)."},
            {"Which partition scheme uses two pointers moving towards each other?", "Hoare Partition Scheme", "Lomuto Partition Scheme", "Dutch National Flag", "Merge Partition", "0", "Hoare's scheme uses pointers from both ends moving inward and does fewer swaps than Lomuto's."},
            {"What is the auxiliary space complexity of standard in-place Quick Sort?", "O(log N) for the recursion stack", "O(1)", "O(N)", "O(N^2)", "0", "The call stack requires O(log N) space in average and best cases."},
            {"Is standard Quick Sort a stable sorting algorithm?", "No, equal elements may have their relative order swapped during partitioning", "Yes, always stable", "Only for strings", "Only when using Lomuto", "0", "Long-distance swaps across the pivot can reorder duplicate keys, making standard Quick Sort unstable."},
            {"Which technique helps avoid Quick Sort's worst-case O(N^2) on sorted inputs?", "Randomized pivot selection (or median-of-three)", "Always choosing the first element", "Using recursion", "Iterating from right to left", "0", "Picking a random pivot or median-of-three prevents deterministic worst-case degradation."},
            {"What algorithmic paradigm does Quick Sort belong to?", "Divide and Conquer", "Dynamic Programming", "Greedy Algorithm", "Backtracking", "0", "Quick Sort partitions the array (Divide), recursively sorts halves (Conquer), and merges implicitly."},
            {"What is the dual-pivot Quick Sort algorithm?", "Yaroslavskiy's partition algorithm using two pivots, default in Java's Arrays.sort()", "Quick Sort with two threads", "Quick Sort on 2D arrays", "MergeSort hybrid", "0", "Java's standard library uses Vladimir Yaroslavskiy's Dual-Pivot QuickSort for primitives."},
            {"When does Lomuto's partition scheme exhibit poor performance?", "When the array contains many identical elements", "When array size is power of 2", "When elements are floats", "Never", "0", "Lomuto does not handle duplicate elements well, resulting in O(N^2) when all elements are equal."},
            {"What is Introsort?", "Hybrid sort starting with QuickSort and switching to HeapSort if recursion depth exceeds 2 log N", "Internal sorting only", "Interactive sort", "Visualizer sort", "0", "Introsort guarantees worst-case O(N log N) by falling back to HeapSort if QuickSort recurses too deep."},
            {"What is the best-case time complexity of Quick Sort?", "O(N log N)", "O(N)", "O(1)", "O(log N)", "0", "When the pivot divides the array into two equal halves at every level, best-case is O(N log N)."},
            {"How many recursive calls are made at depth k of Quick Sort?", "2^k", "k", "k^2", "log k", "0", "Each node divides into two subproblems, forming a binary recursion tree with 2^k calls at depth k."},
            {"What is Tail Call Elimination in Quick Sort?", "Recursively sorting the smaller partition and using a loop for the larger partition to bound stack depth to O(log N)", "Deleting the last element", "Sorting backwards", "Avoiding pivot selection", "0", "Tail call elimination bounds stack usage to O(log N) even in the worst case."},
            {"Can Quick Sort be used on Linked Lists efficiently?", "Yes, but it lacks random access, making MergeSort more popular for linked lists", "No, impossible", "Only doubly linked lists", "Only circular lists", "0", "Lack of random index access makes partitioning less cache-friendly than MergeSort on linked lists."},
            {"Why is Quick Sort often faster in practice than MergeSort for arrays?", "Better cache locality and zero memory allocation overhead", "It does fewer comparisons", "It is stable", "It uses queues", "0", "In-place partition operations maximize CPU cache line reuse without allocating auxiliary buffers."},
            {"What is the 3-Way Partitioning (Dutch National Flag) variant of Quick Sort?", "Divides array into: less than pivot, equal to pivot, and greater than pivot", "Sorts 3 arrays simultaneously", "Uses 3 pivots", "Triples array size", "0", "3-way partitioning solves the duplicate keys problem, running in linear O(N) time for identical keys."},
            {"What is the minimum number of comparisons needed to sort N elements in comparison-based sorting?", "Omega(N log N)", "O(N)", "O(1)", "O(log N)", "0", "Decision tree depth proves comparison sorting requires at least Omega(N log N) comparisons in worst case."},
            {"In Lomuto partitioning, which index is usually chosen as pivot?", "The last element (arr[high])", "The first element", "A random middle element", "The minimum element", "0", "Classical Lomuto partitioning selects the last element arr[high] as the pivot."},
            {"What is the recurrence relation for Quick Sort in the average case?", "T(N) = 2T(N/2) + O(N)", "T(N) = T(N-1) + O(1)", "T(N) = 4T(N/2) + O(N)", "T(N) = T(N/2) + O(1)", "0", "Balanced partitioning yields T(N) = 2T(N/2) + O(N), which evaluates to O(N log N)."},
            {"Who invented Quick Sort?", "Tony Hoare (1959)", "John von Neumann", "Alan Turing", "Donald Knuth", "0", "Sir Tony Hoare developed QuickSort in 1959 while working on machine translation."}
        };
    }

    private static String[][] getMergeSortBank() {
        return new String[][]{
            {"What is the time complexity of Merge Sort in the worst case?", "O(N log N)", "O(N^2)", "O(N)", "O(log N)", "0", "Merge Sort guarantees O(N log N) across worst, average, and best cases."},
            {"Is standard Merge Sort stable?", "Yes, it preserves the relative order of duplicate elements", "No, it is unstable", "Only for integers", "Only when inverted", "0", "By taking elements from the left subarray on ties (<=), Merge Sort preserves stability."},
            {"What is the auxiliary space complexity of standard Merge Sort on arrays?", "O(N)", "O(1)", "O(log N)", "O(N^2)", "0", "Merging two sorted subarrays requires an auxiliary array buffer of size O(N)."},
            {"Which algorithmic paradigm does Merge Sort implement?", "Divide and Conquer", "Dynamic Programming", "Greedy", "Branch and Bound", "0", "Merge Sort divides array into two halves, recursively sorts them, and merges the sorted halves."},
            {"What is the recurrence relation for Merge Sort?", "T(N) = 2T(N/2) + O(N)", "T(N) = T(N-1) + O(1)", "T(N) = T(N/2) + O(1)", "T(N) = 3T(N/3) + O(1)", "0", "Dividing into 2 halves of size N/2 and merging in linear O(N) gives T(N) = 2T(N/2) + O(N)."},
            {"Why is Merge Sort preferred for sorting singly linked lists?", "It does not require random access and can merge nodes in O(1) auxiliary space", "It runs in O(N) time", "It uses binary search", "It avoids recursion", "0", "Linked list nodes can be relinked in-place during merging without auxiliary array allocation."},
            {"What is TimSort?", "A hybrid sorting algorithm combining MergeSort and InsertionSort used in Python and Java", "A clock sorting algorithm", "Parallel QuickSort", "Sort for integers only", "0", "TimSort finds natural sorted runs and merges them using an adaptive MergeSort strategy."},
            {"What is Bottom-Up (Iterative) Merge Sort?", "MergeSort that starts with subarrays of size 1 and iteratively doubles merge size without recursion", "MergeSort starting at the bottom of memory", "Reverse sorting", "Sort with single loop", "0", "Iterative MergeSort merges sublists of size 1, 2, 4, 8... iteratively, eliminating stack overhead."},
            {"What is the number of levels in a Merge Sort recursion tree for N elements?", "ceil(log2 N) + 1", "N", "N / 2", "2N", "0", "Halving at each level results in log2 N levels."},
            {"Can Merge Sort be executed in parallel easily?", "Yes, the two recursive branches are completely independent", "No, they must run on same core", "Only if array length is even", "Only for strings", "0", "Fork-Join frameworks naturally parallelize the independent left and right divide operations."},
            {"What is an inversion in an array?", "A pair of indices (i, j) such that i < j and arr[i] > arr[j]", "A negative number", "A reversed string", "A zero value", "0", "Inversions measure how far an array is from being sorted; Merge Sort can count inversions in O(N log N)."},
            {"How does Merge Sort count inversions in an array in O(N log N)?", "During the merge step, when an element is picked from the right array, all remaining left elements form inversions", "By nested loops", "Using a hash set", "With binary search", "0", "If arr[right] < arr[left], it is inverted with all remaining elements in the sorted left subarray."},
            {"What is in-place Merge Sort?", "A variant that merges without O(N) extra space, but has higher time complexity or complexity overhead", "Standard merge sort", "QuickSort", "HeapSort", "0", "In-place merge variants avoid extra space but suffer significant constant factor overhead."},
            {"Who invented Merge Sort?", "John von Neumann (1945)", "Tony Hoare", "Edsger Dijkstra", "Ada Lovelace", "0", "John von Neumann designed Merge Sort in 1945 for electronic computers."},
            {"How many comparisons are performed during the merge of two subarrays of size M and N?", "At most M + N - 1", "M * N", "(M + N) / 2", "log(M + N)", "0", "Each comparison places at least one element into the merged array, totaling at most M + N - 1 comparisons."},
            {"In external sorting (sorting large files that exceed RAM), which algorithm is used?", "Multi-Way External Merge Sort", "QuickSort", "BubbleSort", "Binary Search", "0", "External merge sort splits disk files into sorted chunks and merges them using a min-heap."},
            {"What is the best-case time complexity of standard Merge Sort?", "O(N log N)", "O(N)", "O(1)", "O(log N)", "0", "Standard Merge Sort divides and merges regardless of initial order, taking O(N log N)."},
            {"Can standard Merge Sort be optimized to O(N) for already sorted arrays?", "Yes, by adding a check if arr[mid] <= arr[mid+1] before calling merge", "No", "Only for arrays of size 2", "Only with QuickSort", "0", "If the highest left element is <= lowest right element, the subarrays are already sorted and merge can be skipped."},
            {"What data structure is used to merge K sorted lists efficiently?", "Min-Priority Queue (Min-Heap) in O(N log K) time", "Stack", "FIFO Queue", "LinkedList", "0", "A min-heap of size K allows extracting the minimum among the K list heads in O(log K) time."},
            {"Why is Merge Sort less prone to worst-case performance spikes compared to QuickSort?", "Its split is deterministic (exact half) regardless of input values", "It has no loops", "It uses hashing", "It runs on GPU", "0", "Because division is always at index (low + high)/2, recursion depth is guaranteed log N."}
        };
    }

    private static String[][] getKruskalBank() {
        return new String[][]{
            {"What is the primary objective of Kruskal's algorithm?", "Find single-source shortest paths", "Find a Minimum Spanning Tree (MST) in a connected weighted undirected graph", "Detect strongly connected components", "Compute maximum network flow", "1", "Kruskal's algorithm finds a Minimum Spanning Tree connecting all vertices with minimum total edge weight."},
            {"What is the first step executed in Kruskal's algorithm?", "Choose an arbitrary root vertex", "Sort all edges in non-decreasing order of weight", "Initialize a priority queue of vertices", "Run BFS to check connectivity", "1", "Kruskal's begins by sorting all edges in ascending order of weight."},
            {"Which data structure is primarily used to detect and prevent cycles in Kruskal's algorithm?", "Stack", "Disjoint Set Union (Union-Find)", "Binary Search Tree", "Adjacency Matrix", "1", "Disjoint Set Union (DSU) efficiently tracks connected components and detects cycles in near O(1) amortized time."},
            {"What is the overall time complexity of Kruskal's algorithm using DSU with path compression and union by rank?", "O(V^2)", "O(E log E) or O(E log V)", "O(V * E)", "O(V^3)", "1", "Sorting edges dominates the time complexity at O(E log E) = O(E log V), while DSU operations take O(E * α(V))."},
            {"Which algorithmic design paradigm does Kruskal's algorithm follow?", "Dynamic Programming", "Greedy Paradigm", "Divide and Conquer", "Backtracking", "1", "Kruskal's makes locally optimal greedy choices by picking the smallest available edge that does not form a cycle."},
            {"How does Kruskal's algorithm determine if adding edge (u, v) will create a cycle?", "If find(u) == find(v)", "If degree(u) > 2", "If weight(u, v) < 0", "If u and v are both odd", "0", "If the representative roots of u and v are identical, they already belong to the same component, so adding (u, v) forms a cycle."},
            {"How many edges are present in a completed Minimum Spanning Tree with V vertices?", "V", "V - 1", "V + 1", "E / 2", "1", "Any spanning tree of a graph with V vertices contains exactly V - 1 edges."},
            {"What is the auxiliary space complexity of Kruskal's algorithm?", "O(1)", "O(V + E)", "O(V^2)", "O(E^2)", "1", "Kruskal's stores all E edges for sorting and maintains parent/rank arrays of size V for DSU, taking O(V + E) space."},
            {"Can Kruskal's algorithm handle graphs with negative edge weights?", "Yes, it correctly finds the MST even with negative weights", "No, it enters an infinite loop", "Only if all weights are shifted by a constant", "Only on DAGs", "0", "Kruskal's works correctly with negative weights because negative edges are sorted first and prioritized."},
            {"What does Kruskal's algorithm produce if the input graph is disconnected?", "Throws a runtime exception", "A Minimum Spanning Forest", "A null pointer", "An empty set", "1", "On disconnected graphs, Kruskal's builds a Minimum Spanning Tree for each connected component, forming a Minimum Spanning Forest."},
            {"What is the amortized time complexity of the DSU find operation with path compression and union by rank?", "O(1)", "O(α(V)) where α is inverse Ackermann function", "O(log V)", "O(V)", "1", "Path compression combined with union by rank guarantees nearly constant O(α(V)) amortized time."},
            {"On which type of graph is Kruskal's generally preferred over standard Prim's?", "Sparse graphs with few edges (E << V^2)", "Extremely dense graphs where E ≈ V^2", "Complete graphs", "Graphs with no edges", "0", "On sparse graphs, sorting E edges is fast (O(E log V)), making Kruskal's simpler and faster than dense-graph algorithms."},
            {"If all edge weights in a connected undirected graph are distinct, what can be said about its MST?", "There are multiple distinct MSTs", "The Minimum Spanning Tree is guaranteed to be unique", "No MST exists", "Kruskal's will fail", "1", "By the Cut Property, if every edge weight is strictly distinct, the graph has a unique Minimum Spanning Tree."},
            {"What happens if Kruskal's algorithm encounters parallel edges between vertices u and v?", "It crashes", "The lower-weight edge is considered first during iteration", "Both edges are automatically added", "Parallel edges must be removed manually first", "1", "Because edges are sorted, the minimum-weight parallel edge is considered first, and the heavier one is later rejected for forming a cycle."},
            {"When can Kruskal's algorithm terminate early?", "As soon as exactly V - 1 edges have been added to the MST", "When half of all edges are processed", "When vertex 0 is visited", "Never, it must examine all E edges", "0", "An MST on V vertices requires exactly V - 1 edges; once V - 1 edges are accepted, the tree is complete."},
            {"What is path compression in Disjoint Set Union?", "Making every visited node point directly to the set representative root during find", "Compressing edge weights", "Reducing the number of vertices", "Deleting leaf nodes", "0", "Path compression flattens the tree structure by setting parent[x] = find(parent[x]), reducing future lookup heights to 1."},
            {"Does Kruskal's maintain a single connected growing tree at every intermediate step?", "No, it maintains a forest of disjoint trees that gradually merge", "Yes, it always grows one single component", "Only if the graph is bipartite", "Only if source is vertex 0", "0", "Unlike Prim's, Kruskal's adds edges anywhere in the graph, growing multiple disjoint trees before uniting them."},
            {"If all edge weights in a connected graph are equal to 1, what does Kruskal's algorithm do?", "Any valid spanning tree is an MST, found in O(E) with DSU without complex sorting", "It fails to find an MST", "It produces a cycle", "Throws ArithmeticException", "0", "With uniform weights, any spanning tree has identical cost (V - 1), so sorting is trivial."},
            {"Who introduced Kruskal's algorithm and in what year?", "Joseph Kruskal in 1956", "Robert Prim in 1957", "Edsger Dijkstra in 1959", "Donald Knuth in 1968", "0", "Joseph Kruskal published this algorithm in 1956 in the Proceedings of the American Mathematical Society."},
            {"What is the Cycle Property in graph theory relevant to Kruskal's?", "The strictly heaviest edge in any cycle cannot belong to any Minimum Spanning Tree", "Every cycle must have an even length", "Cycles always have zero weight", "All trees contain cycles", "0", "The Cycle Property states that for any cycle C, the strictly maximum weight edge in C is never part of an MST."}
        };
    }

    private static String[][] getPrimsBank() {
        return new String[][]{
            {"What is the primary function of Prim's algorithm?", "Find all-pairs shortest paths", "Construct a Minimum Spanning Tree for a connected, weighted undirected graph", "Topologically order vertices", "Perform bipartition validation", "1", "Prim's algorithm finds a Minimum Spanning Tree (MST) by growing a single connected tree from an arbitrary starting vertex."},
            {"How does Prim's algorithm differ from Kruskal's algorithm during execution?", "Prim's grows a single connected tree continuously; Kruskal's builds a forest of merging components", "Prim's only works on directed graphs", "Prim's does not use greedy choices", "Prim's requires sorting all edges at the start", "0", "Prim's maintains a single growing component at each step, whereas Kruskal's adds edges across multiple disjoint trees."},
            {"What data structure is standard for achieving O((V + E) log V) time complexity in Prim's?", "FIFO Queue", "Min-Priority Queue (Binary Heap)", "Stack", "Disjoint Set Union", "1", "A Min-Priority Queue extracts the minimum weight cut edge connected to the active tree in O(log V) time."},
            {"What is the time complexity of Prim's algorithm using an Adjacency Matrix without a priority queue?", "O(V^2)", "O(V + E)", "O(E log V)", "O(V log V)", "0", "With an adjacency matrix and simple linear scanning for the min-key vertex across V iterations, Prim's runs in O(V^2)."},
            {"Which data structure achieves Prim's optimal theoretical bound of O(E + V log V)?", "Binary Heap", "Fibonacci Heap", "Treap", "Linked List", "1", "Fibonacci Heaps provide O(1) amortized decrease-key operations, reducing Prim's complexity to O(E + V log V)."},
            {"What is the Cut Property that underpins Prim's algorithm?", "The minimum weight edge crossing any cut between tree vertices and non-tree vertices must belong to the MST", "Every cut has even edge count", "Any edge with weight > 10 is deleted", "The cut vertex must have degree 1", "0", "The Cut Property proves that the minimum-weight edge crossing the cut between the tree and remaining vertices is safe to add."},
            {"Which algorithm behaves almost identically to Prim's algorithm in its structure and mechanics?", "Dijkstra's Shortest Path Algorithm", "Quick Sort", "Kosaraju's Algorithm", "Floyd-Warshall Algorithm", "0", "Prim's and Dijkstra's share nearly identical priority-queue loops; Prim's relaxes edge weight directly, whereas Dijkstra relaxes cumulative distance."},
            {"What is the key difference in the relaxation step between Prim's and Dijkstra's?", "Prim's updates key[v] = min(key[v], weight(u,v)); Dijkstra updates dist[v] = min(dist[v], dist[u] + weight(u,v))", "Prim's ignores weights", "Dijkstra does not use a priority queue", "Prim's checks for negative cycles", "0", "Prim's keys represent the distance from the MST tree cut, not cumulative path distance from a source."},
            {"What is the auxiliary space complexity of Prim's algorithm?", "O(1)", "O(V)", "O(V^2)", "O(E * V)", "1", "Prim's requires key[], parent[], and inMST[] boolean arrays of size V, using O(V) auxiliary space."},
            {"Can Prim's algorithm handle negative edge weights?", "Yes, it correctly finds the MST even with negative weights", "No, negative weights cause an infinite loop", "Only if there are no negative cycles", "Only if weights are even", "0", "Unlike Dijkstra, Prim's works correctly with negative edge weights because it seeks minimum cut edges, not shortest paths."},
            {"For dense graphs where E ≈ V^2, which version of Prim's is optimal?", "Prim's using an Adjacency Matrix O(V^2)", "Kruskal's algorithm O(V^2 log V)", "Prim's with binary heap", "DFS", "0", "On dense graphs, Prim's O(V^2) with adjacency matrix beats heap-based implementations due to no priority queue overhead."},
            {"What initial key value is assigned to all vertices (except the start vertex) in Prim's?", "0", "Infinity", "-1", "Degree of vertex", "1", "All non-source vertices start with key = Infinity, while the starting vertex has key = 0."},
            {"How many times is extract-min performed in Prim's algorithm?", "V times", "E times", "V - 1 times", "1 time", "0", "Each of the V vertices is extracted from the priority queue exactly once."},
            {"What happens if Prim's algorithm is run on a disconnected graph?", "It only finds the MST for the connected component containing the start vertex", "It throws an exception", "It connects all components with virtual edges", "It hangs indefinitely", "0", "Prim's grows from one vertex, so it will only span the connected component containing that initial vertex."},
            {"Who originally developed the algorithm before Robert Prim rediscovered it in 1957?", "Vojtěch Jarník in 1930", "Edsger Dijkstra in 1959", "Joseph Kruskal in 1956", "Richard Bellman in 1958", "0", "Czech mathematician Vojtěch Jarník first developed the algorithm in 1930; it is often called the Jarník-Prim algorithm."},
            {"Can the choice of initial start vertex affect the total weight of the resulting MST?", "No, the total weight of the MST is always identical regardless of the start vertex", "Yes, start vertex determines minimum weight", "Only if weights are negative", "Only if V > 50", "0", "Any valid MST of a connected graph has the exact same total minimum weight, regardless of starting vertex."},
            {"In Prim's algorithm, what does the parent[v] array represent?", "The tree edge connecting v to the growing MST", "The root node of the graph", "The shortest path hop count", "The degree of v", "0", "parent[v] records which tree vertex connects v into the MST, allowing edge reconstruction."},
            {"How does Prim's avoid forming cycles when selecting edges?", "By only considering edges that connect an in-tree vertex to an out-of-tree vertex", "By running DFS before every insertion", "By using a cycle detection stack", "By limiting edge weights", "0", "Because edges are always chosen across the cut (from inMST to !inMST), cycle formation is fundamentally impossible."},
            {"If an undirected graph has a unique lightest edge e, does e belong to every MST?", "Yes, by the Cut Property, the unique lightest edge must belong to every MST", "No, it depends on the start vertex", "Only if the graph is bipartite", "Never", "0", "The global lightest edge e crosses the cut between its two endpoints, so the Cut Property guarantees it is in every MST."},
            {"What is the maximum number of decrease-key operations in heap-based Prim's?", "O(E)", "O(V)", "O(1)", "O(V^2)", "0", "Each edge in the graph can trigger at most one decrease-key relaxation, giving at most O(E) decrease-key operations."}
        };
    }

    private static String[][] getGenericBank(String algo) {
        return new String[][]{
            {"What is the primary role of " + algo + " in computer science?", "Algorithmic problem solving and data processing", "Hardware routing", "Compiler bytecode generation", "Database backup", "0", "Algorithms provide foundational methodologies for structured problem solving."},
            {"What is the importance of analyzing time and space complexity?", "To predict resource consumption and scalability as input size grows", "To satisfy compiler warnings", "To minimize code line count", "To enforce OOP", "0", "Asymptotic analysis guarantees system predictability under scale."},
            {"Which asymptotic notation represents the tight bound of an algorithm?", "Theta notation (Θ)", "Big O notation (O)", "Omega notation (Ω)", "Little o notation", "0", "Theta notation characterizes both upper and lower asymptotic bounds."},
            {"What does space complexity measure?", "Total auxiliary and memory overhead required during execution as a function of N", "Disk size of the .java file", "RAM capacity of the computer", "Network bandwidth", "0", "Space complexity quantifies memory growth relative to input size."},
            {"What is the time complexity of binary search on a sorted array?", "O(log N)", "O(N)", "O(N log N)", "O(1)", "0", "Halving the search space at each comparison step takes logarithmic time O(log N)."},
            {"Which data structure provides O(1) average time complexity for insertion and lookup?", "Hash Table", "Binary Search Tree", "Linked List", "Array", "0", "Hash tables compute array indices via hash functions in constant average time."},
            {"What is a Greedy algorithm?", "An algorithm that makes the locally optimal choice at each stage", "An algorithm that tries every permutation", "An algorithm that backtracks", "An algorithm using neural networks", "0", "Greedy strategies commit to immediate best choices without revising past decisions."},
            {"What is Dynamic Programming?", "Solving complex problems by breaking them down into overlapping subproblems with optimal substructure", "Programming with dynamic typing", "Executing code on runtime threads", "Using mutable variables", "0", "DP stores solutions to overlapping subproblems to prevent redundant computation."},
            {"What is Memoization in Dynamic Programming?", "Top-down caching of function return values", "Bottom-up tabulation", "Writing comments", "Garbage collection", "0", "Memoization records previously computed recursive outputs in a lookup table."},
            {"What is Tabulation in Dynamic Programming?", "Bottom-up iterative filling of an array/table", "Top-down recursion", "Sorting columns", "Parsing text", "0", "Tabulation builds solutions iteratively from base cases up to the target value."},
            {"What is a Directed Acyclic Graph (DAG)?", "A directed graph with no directed cycles", "A tree with undirected edges", "A complete graph", "A bipartite graph", "0", "A DAG has directed edges and no cycle paths that loop back to the origin."},
            {"What is a Spanning Tree of a connected undirected graph G?", "A subgraph that is a tree and includes all vertices of G", "A tree with all edges of G", "A cycle containing all vertices", "A clique", "0", "A spanning tree spans all |V| vertices using exactly |V| - 1 edges with no cycles."},
            {"Which algorithm finds the Minimum Spanning Tree using a greedy edge-by-edge approach?", "Kruskal's Algorithm", "Dijkstra's Algorithm", "BFS", "Floyd-Warshall", "0", "Kruskal's sorts all edges by weight and adds non-cycle edges using Disjoint Set Union (DSU)."},
            {"What is the time complexity of Kruskal's algorithm?", "O(E log E) or O(E log V)", "O(V^3)", "O(V + E)", "O(V^2)", "0", "Sorting E edges dominates the runtime at O(E log E)."},
            {"What is the purpose of the Disjoint Set Union (Union-Find) data structure?", "Tracking elements partitioned into disjoint subsets with near O(1) union and find", "Sorting integers", "Binary heap storage", "Matrix multiplication", "0", "DSU with path compression and union by rank operates in near constant inverse Ackermann time."},
            {"What is Amortized Analysis?", "Averaging the time required to perform a sequence of data structure operations over all operations", "Measuring CPU voltage", "Calculating worst-case single operation", "Static memory checking", "0", "Amortization proves that occasional expensive operations (e.g. ArrayList resize) average out to O(1)."},
            {"What is NP-Completeness?", "The class of decision problems in NP to which any other NP problem can be reduced in polynomial time", "Non-Polynomial algorithms", "Problems with no solution", "Networks protocols", "0", "NP-Complete problems are the hardest problems in NP; if one has a polynomial solution, P = NP."},
            {"Which problem is classically NP-Complete?", "Travelling Salesperson Decision Problem", "Shortest Path on DAG", "Minimum Spanning Tree", "Binary Search", "0", "The Travelling Salesperson Decision Problem (TSP) is known to be NP-Complete."},
            {"What is asymptotic notation Big-O used for?", "To specify an upper bound on the growth rate of a function", "To calculate exact milliseconds", "To find the minimum time", "To measure memory leaks", "0", "Big-O provides a formal mathematical upper bound on algorithm scaling."},
            {"Why is CodeCanvas useful for CS students?", "It bridges theoretical algorithmic math with visual simulations, code implementation, and interactive assessment", "It compiles C++", "It replaces the OS", "It deletes old files", "0", "CodeCanvas provides multi-modal learning across theory, code, simulation, and self-quizzing."}
        };
    }
}
