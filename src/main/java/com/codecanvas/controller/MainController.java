package com.codecanvas.controller;

import com.codecanvas.api.ApiService;
import com.codecanvas.db.DBHelper;
import com.codecanvas.model.ExecutionTrace;
import com.codecanvas.model.Person;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Single controller for CodeCanvas's three-tab dashboard. Kept as one class
 * (rather than one controller per tab) because the tabs share state - the
 * Person table, the DBHelper and the ApiService - and FXML's default
 * controller-per-document model makes that simplest here.
 */
public class MainController implements Initializable {

    private static final DateTimeFormatter DOB_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final DBHelper dbHelper = new DBHelper();
    private final ApiService apiService = new ApiService();

    // ----- Root / status -----
    @FXML private BorderPane rootPane;
    @FXML private Label statusLabel;

    // ----- Tab 1: Algorithm Visualizer -----
    @FXML private ListView<String> algoListView;
    @FXML private Label algoSelectionLabel;
    @FXML private TreeView<String> categoryTreeView;
    @FXML private Label treeSelectionLabel;
    @FXML private TextField accumulateInputField;
    @FXML private Button accumulateButton;
    @FXML private ProgressBar accumulateProgressBar;
    @FXML private Label accumulateStatusLabel;
    @FXML private Slider fontSizeSlider;
    @FXML private Label sliderTargetLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button showQuantityButton;
    @FXML private Label quantityLabel;
    @FXML private ColorPicker labelColorPicker;
    @FXML private Label colorTargetLabel;

    // ----- Tab 2: User Profile / Demo Dashboard -----
    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, Integer> idColumn;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> levelColumn;
    @FXML private TableColumn<Person, LocalDate> dobColumn;
    @FXML private TextField personNameField;
    @FXML private ChoiceBox<String> personLevelChoiceBox;
    @FXML private DatePicker personDobPicker;
    @FXML private Button addPersonButton;
    @FXML private Button updatePersonButton;
    @FXML private Button deletePersonButton;

    @FXML private ImageView profileImageView;
    @FXML private Button changeImageButton;
    @FXML private Button browseImageButton;

    @FXML private RadioButton beginnerRadio;
    @FXML private RadioButton intermediateRadio;
    @FXML private RadioButton expertRadio;
    @FXML private Label skillLevelLabel;

    @FXML private CheckBox readingCheck;
    @FXML private CheckBox gamingCheck;
    @FXML private CheckBox travelingCheck;
    @FXML private Button submitHobbiesButton;
    @FXML private Label hobbiesLabel;

    @FXML private ChoiceBox<String> themeChoiceBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private DatePicker dobPicker;
    @FXML private Label dobLabel;

    @FXML private TextArea notesTextArea;
    @FXML private Button clearNotesButton;

    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordButton;

    @FXML private Button infoAlertButton;
    @FXML private Button warningAlertButton;
    @FXML private Button errorAlertButton;

    // ----- Tab 3: Database & API Sync -----
    @FXML private TableView<ExecutionTrace> traceTable;
    @FXML private TableColumn<ExecutionTrace, Integer> traceIdColumn;
    @FXML private TableColumn<ExecutionTrace, String> traceAlgoColumn;
    @FXML private TableColumn<ExecutionTrace, Integer> traceOpsColumn;
    @FXML private TableColumn<ExecutionTrace, String> traceStatusColumn;
    @FXML private Button loadFromDbButton;
    @FXML private Button fetchFromApiButton;
    @FXML private Button saveFetchedButton;
    @FXML private Label syncStatusLabel;
    @FXML private TextArea jsonRawTextArea;

    // ----- Internal state -----
    private final ObservableList<Person> personData = FXCollections.observableArrayList();
    private final ObservableList<ExecutionTrace> traceData = FXCollections.observableArrayList();
    private List<ExecutionTrace> lastFetchedTraces = List.of();
    private int accumulatedCount = 0;
    private boolean passwordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initStatusDefaults();
        initAlgorithmTab();
        initDashboardTab();
        initSyncTab();
        wireProgrammaticHandlers();
    }

    // ============================================================ INIT: status

    private void initStatusDefaults() {
        statusLabel.setText("CodeCanvas ready. " + LocalDate.now().format(DOB_DISPLAY_FORMAT));
    }

    // ============================================================ INIT: Tab 1

    private void initAlgorithmTab() {
        algoListView.setItems(FXCollections.observableArrayList(
                "Bubble Sort", "Merge Sort", "Quick Sort", "Binary Search", "Dijkstra's Algorithm", "BFS", "DFS"));
        algoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                algoSelectionLabel.setText("Selected: " + newV);
            }
        });

        TreeItem<String> root = new TreeItem<>("Algorithms");
        TreeItem<String> sorting = new TreeItem<>("Sorting");
        sorting.getChildren().addAll(new TreeItem<>("Bubble Sort"), new TreeItem<>("Merge Sort"), new TreeItem<>("Quick Sort"));
        TreeItem<String> searching = new TreeItem<>("Searching");
        searching.getChildren().addAll(new TreeItem<>("Linear Search"), new TreeItem<>("Binary Search"));
        TreeItem<String> graph = new TreeItem<>("Graph");
        graph.getChildren().addAll(new TreeItem<>("BFS"), new TreeItem<>("DFS"), new TreeItem<>("Dijkstra's Algorithm"));
        root.getChildren().addAll(sorting, searching, graph);
        root.setExpanded(true);
        categoryTreeView.setRoot(root);
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                treeSelectionLabel.setText("Clicked: " + newV.getValue());
            }
        });

        fontSizeSlider.valueProperty().addListener((obs, oldV, newV) ->
                sliderTargetLabel.setFont(Font.font(newV.doubleValue())));

        SpinnerValueFactory<Integer> spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        quantitySpinner.setValueFactory(spinnerFactory);
        showQuantityButton.setOnAction(e -> quantityLabel.setText("Quantity: " + quantitySpinner.getValue()));

        labelColorPicker.setValue(Color.BLACK);
    }

    @FXML
    private void onAccumulate(ActionEvent event) {
        runAccumulateStep();
    }

    private void runAccumulateStep() {
        int target;
        try {
            target = Integer.parseInt(accumulateInputField.getText().trim());
            if (target <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            accumulateStatusLabel.setText("Enter a positive integer for N.");
            return;
        }
        accumulatedCount++;
        double progress = Math.min(1.0, (double) accumulatedCount / target);
        accumulateProgressBar.setProgress(progress);
        accumulateStatusLabel.setText("Count: " + accumulatedCount + " / " + target);
        if (accumulatedCount >= target) {
            accumulatedCount = 0; // reset so the demo can be repeated
        }
    }

    @FXML
    private void onColorChanged(ActionEvent event) {
        colorTargetLabel.setTextFill(labelColorPicker.getValue());
    }

    // ============================================================ INIT: Tab 2

    private void initDashboardTab() {
        // TableView + Person CRUD
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        dobColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : value.format(DOB_DISPLAY_FORMAT));
            }
        });
        personTable.setItems(personData);
        personData.setAll(dbHelper.findAllPersons());

        personTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                personNameField.setText(newV.getName());
                personLevelChoiceBox.setValue(newV.getLevel());
                personDobPicker.setValue(newV.getDob());
            }
        });

        personLevelChoiceBox.setItems(FXCollections.observableArrayList("Beginner", "Intermediate", "Expert"));
        personLevelChoiceBox.getSelectionModel().selectFirst();

        // ImageView + FileChooser
        loadDefaultImage();

        // RadioButton + ToggleGroup (defined via fx:id RadioButtons; group built here to keep FXML terse)
        ToggleGroup skillGroup = new ToggleGroup();
        beginnerRadio.setToggleGroup(skillGroup);
        intermediateRadio.setToggleGroup(skillGroup);
        expertRadio.setToggleGroup(skillGroup);
        ChangeListener<Toggle> skillListener = (obs, oldT, newT) -> {
            RadioButton selected = (RadioButton) newT;
            if (selected != null) {
                skillLevelLabel.setText("Level: " + selected.getText());
            }
        };
        skillGroup.selectedToggleProperty().addListener(skillListener);

        // ChoiceBox theme
        themeChoiceBox.setItems(FXCollections.observableArrayList("Red", "Green", "Blue"));
        themeChoiceBox.setOnAction(e -> applyTheme(themeChoiceBox.getValue()));

        // ComboBox country
        countryComboBox.setItems(FXCollections.observableArrayList(
                "Bangladesh", "United States", "United Kingdom", "Canada", "India", "Germany", "Japan"));

        // DatePicker -> formatted label is wired via onAction="#onDobChanged" in FXML.

        // PasswordField reveal toggle
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    private void loadDefaultImage() {
        URL imgUrl = getClass().getResource("/images/default.png");
        if (imgUrl != null) {
            profileImageView.setImage(new Image(imgUrl.toExternalForm()));
        }
    }

    @FXML
    private void onAddPerson(ActionEvent event) {
        String name = personNameField.getText();
        if (name == null || name.isBlank()) {
            statusLabel.setText("Name is required to add a person.");
            return;
        }
        Person p = new Person(0, name.trim(), personLevelChoiceBox.getValue(), personDobPicker.getValue());
        dbHelper.insertPerson(p);
        personData.add(p);
        statusLabel.setText("Added person: " + p.getName());
    }

    @FXML
    private void onUpdatePerson(ActionEvent event) {
        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a row in the table to update.");
            return;
        }
        selected.setName(personNameField.getText());
        selected.setLevel(personLevelChoiceBox.getValue());
        selected.setDob(personDobPicker.getValue());
        dbHelper.updatePerson(selected);
        personTable.refresh();
        statusLabel.setText("Updated person id=" + selected.getId());
    }

    @FXML
    private void onDeletePerson(ActionEvent event) {
        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a row in the table to delete.");
            return;
        }
        dbHelper.deletePerson(selected.getId());
        personData.remove(selected);
        statusLabel.setText("Deleted person id=" + selected.getId());
    }

    @FXML
    private void onChangeImage(ActionEvent event) {
        loadDefaultImage();
        statusLabel.setText("Image reset to default.");
    }

    @FXML
    private void onBrowseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            profileImageView.setImage(new Image(file.toURI().toString()));
            statusLabel.setText("Loaded image: " + file.getName());
        }
    }

    @FXML
    private void onSubmitHobbies(ActionEvent event) {
        StringBuilder sb = new StringBuilder("Hobbies: ");
        boolean any = false;
        if (readingCheck.isSelected()) { sb.append("Reading "); any = true; }
        if (gamingCheck.isSelected()) { sb.append("Gaming "); any = true; }
        if (travelingCheck.isSelected()) { sb.append("Traveling "); any = true; }
        hobbiesLabel.setText(any ? sb.toString().trim() : "Hobbies: none selected");
    }

    private void applyTheme(String theme) {
        if (theme == null) return;
        rootPane.getStyleClass().removeAll("theme-red", "theme-green", "theme-blue");
        switch (theme) {
            case "Red" -> rootPane.getStyleClass().add("theme-red");
            case "Green" -> rootPane.getStyleClass().add("theme-green");
            case "Blue" -> rootPane.getStyleClass().add("theme-blue");
        }
    }

    @FXML
    private void onDobChanged(ActionEvent event) {
        LocalDate value = dobPicker.getValue();
        dobLabel.setText(value == null ? "DOB: -" : "DOB: " + value.format(DOB_DISPLAY_FORMAT));
    }

    @FXML
    private void onClearNotes(ActionEvent event) {
        notesTextArea.clear();
    }

    @FXML
    private void onTogglePassword(ActionEvent event) {
        passwordVisible = !passwordVisible;
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        togglePasswordButton.setText(passwordVisible ? "Hide" : "Show");
    }

    @FXML
    private void onInfoAlert(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "CodeCanvas", "This is an informational message.");
    }

    @FXML
    private void onWarningAlert(ActionEvent event) {
        showAlert(Alert.AlertType.WARNING, "Warning", "CodeCanvas", "This is a warning message.");
    }

    @FXML
    private void onErrorAlert(ActionEvent event) {
        showAlert(Alert.AlertType.ERROR, "Error", "CodeCanvas", "This is an error message.");
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ============================================================ INIT: Tab 3

    private void initSyncTab() {
        traceIdColumn.setCellValueFactory(new PropertyValueFactory<>("traceId"));
        traceAlgoColumn.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        traceOpsColumn.setCellValueFactory(new PropertyValueFactory<>("comparisons"));
        traceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        traceTable.setItems(traceData);
    }

    @FXML
    private void onLoadTracesFromDb(ActionEvent event) {
        traceData.setAll(dbHelper.findAllTraces());
        syncStatusLabel.setText("Sync status: loaded " + traceData.size() + " trace(s) from SQLite.");
    }

    @FXML
    private void onFetchTracesFromApi(ActionEvent event) {
        syncStatusLabel.setText("Sync status: fetching from API...");
        Task<List<ExecutionTrace>> fetchTask = new Task<>() {
            @Override
            protected List<ExecutionTrace> call() throws Exception {
                return apiService.fetchSampleTraces();
            }
        };
        fetchTask.setOnSucceeded(e -> {
            lastFetchedTraces = fetchTask.getValue();
            traceData.setAll(lastFetchedTraces);
            jsonRawTextArea.setText(summarizeTraces(lastFetchedTraces));
            syncStatusLabel.setText("Sync status: fetched " + lastFetchedTraces.size() + " trace(s) from API.");
        });
        fetchTask.setOnFailed(e -> {
            Throwable ex = fetchTask.getException();
            syncStatusLabel.setText("Sync status: API fetch failed - " + (ex != null ? ex.getMessage() : "unknown error"));
        });
        Thread thread = new Thread(fetchTask, "codecanvas-api-fetch");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onSaveFetchedTraces(ActionEvent event) {
        if (lastFetchedTraces.isEmpty()) {
            syncStatusLabel.setText("Sync status: nothing to save - fetch from the API first.");
            return;
        }
        for (ExecutionTrace t : lastFetchedTraces) {
            dbHelper.insertTrace(t);
        }
        syncStatusLabel.setText("Sync status: saved " + lastFetchedTraces.size() + " trace(s) to SQLite.");
        onLoadTracesFromDb(event);
    }

    private String summarizeTraces(List<ExecutionTrace> traces) {
        StringBuilder sb = new StringBuilder();
        for (ExecutionTrace t : traces) {
            sb.append(t).append("\n");
        }
        return sb.toString();
    }

    // ============================================================ MenuBar handlers

    @FXML
    private void onFileNew(ActionEvent event) {
        personNameField.clear();
        personDobPicker.setValue(null);
        notesTextArea.clear();
        statusLabel.setText("New session started.");
    }

    @FXML
    private void onFileOpen(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        Stage stage = (Stage) rootPane.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            statusLabel.setText("Opened: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void onFileExit(ActionEvent event) {
        Platform.exit();
    }

    // ============================================================ Programmatic handlers

    /**
     * Demonstrates attaching event handlers purely in Java (no onAction= in FXML):
     * a button click handler and an Enter-key handler on a text field.
     */
    private void wireProgrammaticHandlers() {
        accumulateButton.setOnAction(this::onAccumulate); // programmatic setOnAction, overrides FXML wiring

        accumulateInputField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                runAccumulateStep();
            }
        });
    }
}
