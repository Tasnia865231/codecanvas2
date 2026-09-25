package com.codecanvas.controller;

import com.codecanvas.model.AlgorithmItem;
import com.codecanvas.navigation.NavigationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Figure 2: Main Algorithm Dashboard & Search.
 * Features real-time case-insensitive search, category TreeView navigation,
 * an itemized TableView of CS algorithms, and smooth transition to Detail Hub (Figure 3).
 */
public class DashboardController implements Initializable {

    private final NavigationManager navManager = NavigationManager.getInstance();

    @FXML private TextField searchField;
    @FXML private Button clearSearchBtn;
    @FXML private TreeView<String> categoryTreeView;
    @FXML private Label activeFilterLabel;
    @FXML private TableView<AlgorithmItem> algorithmTable;
    @FXML private TableColumn<AlgorithmItem, String> nameCol;
    @FXML private TableColumn<AlgorithmItem, String> categoryCol;
    @FXML private TableColumn<AlgorithmItem, String> timeCol;
    @FXML private TableColumn<AlgorithmItem, String> spaceCol;
    @FXML private TableColumn<AlgorithmItem, String> descCol;
    @FXML private Label resultCountLabel;
    @FXML private Button openDetailBtn;
    @FXML private Button profileShortcutBtn;

    private final ObservableList<AlgorithmItem> masterData = FXCollections.observableArrayList();
    private FilteredList<AlgorithmItem> filteredData;
    private String selectedCategory = "ALL";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initAlgorithmsData();

        // Setup TableView columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeComplexity"));
        spaceCol.setCellValueFactory(new PropertyValueFactory<>("spaceComplexity"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Setup FilteredList
        filteredData = new FilteredList<>(masterData, p -> true);
        algorithmTable.setItems(filteredData);

        // Real-time case-insensitive filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyCombinedFilter());

        // Category TreeView
        initCategoryTreeView();

        // Double-click row or single-click row to select
        algorithmTable.setRowFactory(tv -> {
            TableRow<AlgorithmItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    AlgorithmItem clickedItem = row.getItem();
                    openAlgorithmDetail(clickedItem);
                }
            });
            return row;
        });

        algorithmTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            openDetailBtn.setDisable(newV == null);
        });

        // Clear search
        clearSearchBtn.setOnAction(e -> searchField.clear());

        // Programmatic Enter on searchField
        searchField.setOnAction(e -> {
            if (!filteredData.isEmpty()) {
                algorithmTable.getSelectionModel().select(0);
                openAlgorithmDetail(filteredData.get(0));
            }
        });

        updateCountLabel();
    }

    private void initAlgorithmsData() {
        masterData.setAll(
            new AlgorithmItem("1", "BFS", "Graph", "O(V + E)", "O(V)", 
                "Breadth-First Search traverses tree/graph level by level using a queue.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/graphs/bfs.md", "src/main/java/com/thealgorithms/searches/BreadthFirstSearch.java", "BFS.mp4"),
            new AlgorithmItem("2", "DFS", "Graph", "O(V + E)", "O(V)", 
                "Depth-First Search traverses graph branches deeply before backtracking.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/graphs/dfs.md", "src/main/java/com/thealgorithms/searches/DepthFirstSearch.java", "DFS.mp4"),
            new AlgorithmItem("3", "Dijkstra", "Graph", "O((V + E) log V)", "O(V)", 
                "Finds shortest paths from single source in non-negative weighted graphs.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/graphs/dijkstra.md", "src/main/java/com/thealgorithms/datastructures/graphs/Dijkstra.java", "Dijkstra.mp4"),
            new AlgorithmItem("4", "Bellman-Ford", "Graph", "O(V * E)", "O(V)", 
                "Computes single-source shortest path and detects negative cycles.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/graphs/bellman_ford.md", "src/main/java/com/thealgorithms/datastructures/graphs/BellmanFord.java", "Bellman-Ford.mp4"),
            new AlgorithmItem("5", "Floyd-Warshall", "Dynamic Programming", "O(V^3)", "O(V^2)", 
                "All-pairs shortest path dynamic programming algorithm on weighted graphs.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/graphs/floyd_warshall.md", "src/main/java/com/thealgorithms/dynamicprogramming/FloydWarshall.java", "Floyd-Warshall.mp4"),
            new AlgorithmItem("6", "Johnson's", "Graph", "O(V^2 log V + VE)", "O(V^2)", 
                "All-pairs shortest paths algorithm optimized for sparse graphs via reweighting.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/graphs/johnsons.md", "src/main/java/com/thealgorithms/datastructures/graphs/JohnsonsAlgorithm.java", "Johnson.mp4"),
            new AlgorithmItem("7", "Quick Sort", "Sorting", "O(N log N)", "O(log N)", 
                "Divide-and-conquer sorting algorithm using in-place partitioning.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/sorts/quick_sort.md", "src/main/java/com/thealgorithms/sorts/QuickSort.java", "QuickSort.mp4"),
            new AlgorithmItem("8", "Merge Sort", "Sorting", "O(N log N)", "O(N)", 
                "Stable divide-and-conquer sorting algorithm with predictable complexity.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/sorts/merge_sort.md", "src/main/java/com/thealgorithms/sorts/MergeSort.java", "MergeSort.mp4"),
            new AlgorithmItem("9", "Binary Search", "Searching", "O(log N)", "O(1)", 
                "Efficient interval halving search on sorted arrays.",
                "TheAlgorithms", "Java", "src/main/resources/algorithms/searches/binary_search.md", "src/main/java/com/thealgorithms/searches/BinarySearch.java", "BinarySearch.mp4")
        );
    }

    private void initCategoryTreeView() {
        TreeItem<String> root = new TreeItem<>("All Categories");
        root.setExpanded(true);

        TreeItem<String> graph = new TreeItem<>("Graph");
        graph.getChildren().addAll(new TreeItem<>("BFS"), new TreeItem<>("DFS"), 
                                   new TreeItem<>("Dijkstra"), new TreeItem<>("Bellman-Ford"), 
                                   new TreeItem<>("Johnson's"));
        graph.setExpanded(true);

        TreeItem<String> dp = new TreeItem<>("Dynamic Programming");
        dp.getChildren().addAll(new TreeItem<>("Floyd-Warshall"));
        dp.setExpanded(true);

        TreeItem<String> sort = new TreeItem<>("Sorting");
        sort.getChildren().addAll(new TreeItem<>("Quick Sort"), new TreeItem<>("Merge Sort"));
        sort.setExpanded(true);

        TreeItem<String> search = new TreeItem<>("Searching");
        search.getChildren().addAll(new TreeItem<>("Binary Search"));
        search.setExpanded(true);

        root.getChildren().addAll(graph, dp, sort, search);
        categoryTreeView.setRoot(root);

        categoryTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                String val = newV.getValue();
                if (newV.isLeaf() && newV.getParent() != null && newV.getParent() != root) {
                    // Specific algorithm clicked in tree
                    searchField.setText(val);
                } else if (val.equals("All Categories")) {
                    selectedCategory = "ALL";
                    activeFilterLabel.setText("Category: All");
                    applyCombinedFilter();
                } else {
                    selectedCategory = val;
                    activeFilterLabel.setText("Category: " + val);
                    applyCombinedFilter();
                }
            }
        });
    }

    private void applyCombinedFilter() {
        String filter = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        filteredData.setPredicate(item -> {
            // Category check
            boolean matchesCat = selectedCategory.equals("ALL") || item.getCategory().equalsIgnoreCase(selectedCategory);

            // Search query check (case-insensitive across name, category, and description)
            boolean matchesQuery = filter.isEmpty() ||
                    item.getName().toLowerCase().contains(filter) ||
                    item.getCategory().toLowerCase().contains(filter) ||
                    item.getDescription().toLowerCase().contains(filter);

            return matchesCat && matchesQuery;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        resultCountLabel.setText("Displaying " + filteredData.size() + " of " + masterData.size() + " algorithms");
    }

    @FXML
    private void onOpenDetailClicked(ActionEvent event) {
        AlgorithmItem selected = algorithmTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openAlgorithmDetail(selected);
        }
    }

    private void openAlgorithmDetail(AlgorithmItem item) {
        navManager.navigateTo(NavigationManager.Screen.DETAIL, item);
    }

    @FXML
    private void onProfileShortcutClicked(ActionEvent event) {
        navManager.navigateTo(NavigationManager.Screen.PROFILE);
    }
}
