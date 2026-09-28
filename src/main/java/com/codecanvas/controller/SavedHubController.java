package com.codecanvas.controller;

import com.codecanvas.db.DBHelper;
import com.codecanvas.model.AlgorithmItem;
import com.codecanvas.model.SavedItem;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Figures 6 & 7: Saved Items Hub & Detail viewer.
 * Provides filter tabs for [All], [Algorithm], [Code], [Videos], and [Simulation],
 * loads SQLite bookmarks for the authenticated user, and allows reviewing/deleting saved items.
 */
public class SavedHubController implements Initializable {

    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    // Filter Buttons / Tabs
    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton algoFilterBtn;
    @FXML private ToggleButton codeFilterBtn;
    @FXML private ToggleButton videosFilterBtn;
    @FXML private ToggleButton simulationFilterBtn;
    private ToggleGroup filterGroup;

    // TableView
    @FXML private TableView<SavedItem> savedTable;
    @FXML private TableColumn<SavedItem, Integer> colId;
    @FXML private TableColumn<SavedItem, String> colType;
    @FXML private TableColumn<SavedItem, String> colAlgo;
    @FXML private TableColumn<SavedItem, String> colTitle;
    @FXML private TableColumn<SavedItem, String> colSavedAt;

    // Detail inspector (Figure 7)
    @FXML private Label detailAlgoTitleLabel;
    @FXML private Label detailTypeBadge;
    @FXML private Label detailSavedAtLabel;
    @FXML private TextArea detailContentArea;
    @FXML private Button openInHubBtn;
    @FXML private Button deleteBookmarkBtn;
    @FXML private Label statusSummaryLabel;

    private final ObservableList<SavedItem> itemsData = FXCollections.observableArrayList();
    private String currentFilter = "ALL";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilterTabs();
        initTable();
        loadSavedItems();

        savedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            displayItemDetail(newV);
        });

        openInHubBtn.setOnAction(this::onOpenInDetailHub);
        deleteBookmarkBtn.setOnAction(this::onDeleteBookmark);
    }

    private void initFilterTabs() {
        filterGroup = new ToggleGroup();
        allFilterBtn.setToggleGroup(filterGroup);
        algoFilterBtn.setToggleGroup(filterGroup);
        codeFilterBtn.setToggleGroup(filterGroup);
        videosFilterBtn.setToggleGroup(filterGroup);
        simulationFilterBtn.setToggleGroup(filterGroup);
        allFilterBtn.setSelected(true);

        allFilterBtn.setOnAction(e -> applyFilter("ALL"));
        algoFilterBtn.setOnAction(e -> applyFilter("Algorithm"));
        codeFilterBtn.setOnAction(e -> applyFilter("Code"));
        videosFilterBtn.setOnAction(e -> applyFilter("Videos"));
        simulationFilterBtn.setOnAction(e -> applyFilter("Simulation"));
    }

    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAlgo.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSavedAt.setCellValueFactory(new PropertyValueFactory<>("savedAt"));

        savedTable.setItems(itemsData);
    }

    private void applyFilter(String filter) {
        this.currentFilter = filter;
        loadSavedItems();
    }

    public void loadSavedItems() {
        User user = navManager.getCurrentUser();
        if (user == null) {
            statusSummaryLabel.setText("Please log in to view bookmarks.");
            itemsData.clear();
            return;
        }

        List<SavedItem> allUserItems = dbHelper.findSavedItems(user.getUsername(), null);
        List<SavedItem> filtered;

        if (currentFilter.equalsIgnoreCase("ALL")) {
            filtered = allUserItems;
        } else if (currentFilter.equalsIgnoreCase("Videos")) {
            filtered = allUserItems.stream()
                    .filter(i -> i.getType().equalsIgnoreCase("Videos") || i.getType().equalsIgnoreCase("Simulator"))
                    .toList();
        } else {
            filtered = allUserItems.stream()
                    .filter(i -> i.getType().equalsIgnoreCase(currentFilter))
                    .toList();
        }

        itemsData.setAll(filtered);
        statusSummaryLabel.setText("Found " + itemsData.size() + " saved item(s) [" + currentFilter + "]");

        if (!itemsData.isEmpty()) {
            savedTable.getSelectionModel().select(0);
        } else {
            clearDetail();
        }
    }

    private void displayItemDetail(SavedItem item) {
        if (item == null) {
            clearDetail();
            return;
        }

        detailAlgoTitleLabel.setText(item.getAlgorithmName() + " - " + item.getTitle());
        detailTypeBadge.setText("[" + item.getType() + "]");
        detailSavedAtLabel.setText("Saved on: " + item.getSavedAt());
        detailContentArea.setText(item.getContent() != null ? item.getContent() : "");

        openInHubBtn.setDisable(false);
        deleteBookmarkBtn.setDisable(false);
    }

    private void clearDetail() {
        detailAlgoTitleLabel.setText("Select an item to view content");
        detailTypeBadge.setText("");
        detailSavedAtLabel.setText("");
        detailContentArea.clear();
        openInHubBtn.setDisable(true);
        deleteBookmarkBtn.setDisable(true);
    }

    private void onOpenInDetailHub(ActionEvent event) {
        SavedItem item = savedTable.getSelectionModel().getSelectedItem();
        if (item == null) return;

        AlgorithmItem algo = new AlgorithmItem(
                "0", item.getAlgorithmName(), "Graph", "O(V + E)", "O(V)",
                "Saved algorithm entry", "TheAlgorithms", "Java",
                "src/main/resources/algorithms/" + item.getAlgorithmName().toLowerCase() + ".md",
                "src/main/java/com/thealgorithms/" + item.getAlgorithmName().replace(" ", "") + ".java",
                item.getAlgorithmName() + ".mp4"
        );

        navManager.navigateTo(NavigationManager.Screen.ALGORITHM_DETAIL, algo);
    }

    private void onDeleteBookmark(ActionEvent event) {
        SavedItem item = savedTable.getSelectionModel().getSelectedItem();
        if (item == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Bookmark");
        confirm.setContentText("Are you sure you want to remove '" + item.getTitle() + "' from your bookmarks?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            dbHelper.deleteSavedItem(item.getId());
            itemsData.remove(item);
            clearDetail();
            statusSummaryLabel.setText("Bookmark deleted successfully.");
        }
    }
}
