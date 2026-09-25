package com.codecanvas.controller;

import com.codecanvas.db.DBHelper;
import com.codecanvas.model.AlgorithmItem;
import com.codecanvas.model.SavedItem;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Figures 6 & 7: Saved Items Hub & Detail viewer.
 * Provides filter tabs for [Algorithm], [Code], and [Simulator], loads SQLite bookmarks
 * for the authenticated user, and allows reviewing/deleting saved items.
 */
public class SavedHubController implements Initializable {

    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    // Filter Buttons / Tabs
    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton algoFilterBtn;
    @FXML private ToggleButton codeFilterBtn;
    @FXML private ToggleButton simFilterBtn;
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
        simFilterBtn.setToggleGroup(filterGroup);
        allFilterBtn.setSelected(true);

        allFilterBtn.setOnAction(e -> applyFilter("ALL"));
        algoFilterBtn.setOnAction(e -> applyFilter("Algorithm"));
        codeFilterBtn.setOnAction(e -> applyFilter("Code"));
        simFilterBtn.setOnAction(e -> applyFilter("Simulator"));
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

        itemsData.setAll(dbHelper.findSavedItems(user.getUsername(), currentFilter));
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

        // Create domain AlgorithmItem to navigate to Detail Hub
        AlgorithmItem algo = new AlgorithmItem(
                "0", item.getAlgorithmName(), "Graph", "O(V + E)", "O(V)",
                "Saved algorithm entry", "TheAlgorithms", "Java",
                "src/main/resources/algorithms/" + item.getAlgorithmName().toLowerCase() + ".md",
                "src/main/java/" + item.getAlgorithmName() + ".java",
                item.getAlgorithmName() + ".mp4"
        );
        navManager.navigateTo(NavigationManager.Screen.DETAIL, algo);
    }

    private void onDeleteBookmark(ActionEvent event) {
        SavedItem item = savedTable.getSelectionModel().getSelectedItem();
        if (item == null) return;

        dbHelper.deleteSavedItem(item.getId());
        itemsData.remove(item);
        statusSummaryLabel.setText("Deleted bookmark: " + item.getTitle());
        if (itemsData.isEmpty()) {
            clearDetail();
        }
    }
}
