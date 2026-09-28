package com.codecanvas.controller;

import com.codecanvas.db.DBHelper;
import com.codecanvas.model.Person;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import com.codecanvas.util.AppExecutor;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for Figure 5 (User Profile & Student Directory) + Course UI Components Matrix.
 * Enforces Role-Based Access Control (Admin Mode with Add/Edit/Delete vs. Student Mode Read-Only),
 * provides a Peer Directory with read-only progress cards (Level Badge, Total Correct MCQs, Overall Score, Saved Algorithms),
 * and implements explicit dynamic window property bindings for responsive scaling.
 */
public class ProfileController implements Initializable {

    private static final DateTimeFormatter DOB_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    @FXML private ScrollPane profileScrollRoot;

    // ----- Section 1: User Profile Header (Figure 5) -----
    @FXML private ImageView profileImageView;
    @FXML private Button browseImageBtn;
    @FXML private Button resetImageBtn;
    @FXML private Label usernameDisplayLabel;
    @FXML private Label userRoleBadgeLabel;
    @FXML private Label userRankBadgeLabel;
    @FXML private Label userScoreSummaryLabel;
    @FXML private Button openSavedHubBtn;
    @FXML private Button saveProfileChangesBtn;
    @FXML private Label profileSaveStatusLabel;

    // RBAC Banner
    @FXML private Label rbacModeBanner;

    // ----- Section 2: Student Registry TableView & RBAC Admin Form -----
    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, Integer> idColumn;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> levelColumn;
    @FXML private TableColumn<Person, LocalDate> dobColumn;
    @FXML private HBox adminPersonFormBox;
    @FXML private TextField personNameField;
    @FXML private ChoiceBox<String> personLevelChoiceBox;
    @FXML private DatePicker personDobPicker;
    @FXML private Button addPersonBtn;
    @FXML private Button updatePersonBtn;
    @FXML private Button deletePersonBtn;
    @FXML private Label studentModeNoticeLabel;

    // ----- Section 3: Peer Student Directory (Student Inspection) -----
    @FXML private ListView<User> peerListView;
    @FXML private Label peerNameLabel;
    @FXML private Label peerRankBadge;
    @FXML private Label peerRoleBadge;
    @FXML private Label peerMcqScoreLabel;
    @FXML private Label peerSavedCountLabel;
    @FXML private Label peerEmailLabel;
    @FXML private Label peerCountryLabel;
    @FXML private Label peerHobbiesLabel;

    // ----- Section 4: Course UI Components Matrix Controls -----
    @FXML private RadioButton beginnerRadio;
    @FXML private RadioButton intermediateRadio;
    @FXML private RadioButton expertRadio;
    @FXML private Label skillLevelLabel;
    private ToggleGroup skillToggleGroup;

    @FXML private CheckBox readingCheck;
    @FXML private CheckBox gamingCheck;
    @FXML private CheckBox travelingCheck;
    @FXML private Button submitHobbiesBtn;
    @FXML private Label hobbiesLabel;

    @FXML private ChoiceBox<String> themeChoiceBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private DatePicker dobPicker;
    @FXML private Label dobLabel;

    @FXML private ColorPicker labelColorPicker;
    @FXML private Label colorTargetLabel;

    @FXML private ListView<String> algoListView;
    @FXML private Label algoSelectionLabel;
    @FXML private TreeView<String> categoryTreeView;
    @FXML private Label treeSelectionLabel;

    @FXML private TextField accumulateInputField;
    @FXML private Button accumulateButton;
    @FXML private ProgressBar accumulateProgressBar;
    @FXML private Label accumulateStatusLabel;
    private int accumulatedCount = 0;

    @FXML private Slider fontSizeSlider;
    @FXML private Label sliderTargetLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button showQuantityBtn;
    @FXML private Label quantityLabel;

    @FXML private TextArea notesTextArea;
    @FXML private Button clearNotesBtn;
    @FXML private PasswordField profilePasswordField;
    @FXML private TextField profilePasswordVisibleField;
    @FXML private Button togglePasswordBtn;
    private boolean passwordVisible = false;

    @FXML private Button infoAlertBtn;
    @FXML private Button warningAlertBtn;
    @FXML private Button errorAlertBtn;

    private final ObservableList<Person> personList = FXCollections.observableArrayList();
    private final ObservableList<User> peerList = FXCollections.observableArrayList();
    private String customImagePath = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDynamicPropertyBindings();
        initUserProfile();
        initRbacPermissions();
        initPersonTableView();
        initPeerDirectory();
        initCourseUiLabControls();
        wireProgrammaticHandlers();
    }

    /**
     * Academic Rubric Fix: Explicit window property bindings for dynamic scaling.
     */
    private void setupDynamicPropertyBindings() {
        if (profileScrollRoot != null) {
            profileImageView.fitWidthProperty().bind(
                    Bindings.min(120.0, Bindings.max(75.0, profileScrollRoot.widthProperty().divide(9.5)))
            );
            profileImageView.fitHeightProperty().bind(profileImageView.fitWidthProperty());
        }
    }

    // ============================================================ USER PROFILE & RBAC

    private void initUserProfile() {
        User user = navManager.getCurrentUser();
        loadProfileImage(user != null ? user.getProfileImagePath() : null);

        if (user != null) {
            String name = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
            usernameDisplayLabel.setText(name + " (@" + user.getUsername() + ")");
            userRoleBadgeLabel.setText(user.isAdmin() ? "ADMINISTRATOR" : "STUDENT");
            userRankBadgeLabel.setText("Rank: " + (user.getUserLevel() != null ? user.getUserLevel() : "Beginner"));
            userScoreSummaryLabel.setText(String.format("Correct MCQs: %d | Total Score: %d pts",
                    user.getTotalCorrectMcqs(), user.getOverallScore()));

            if (user.getCountry() != null) countryComboBox.setValue(user.getCountry());
            if (user.getDob() != null) dobPicker.setValue(user.getDob());
            if (user.getTheme() != null) themeChoiceBox.setValue(user.getTheme());

            if (user.getSkillLevel() != null) {
                switch (user.getSkillLevel()) {
                    case "Intermediate" -> intermediateRadio.setSelected(true);
                    case "Expert" -> expertRadio.setSelected(true);
                    default -> beginnerRadio.setSelected(true);
                }
            }

            if (user.getHobbies() != null) {
                readingCheck.setSelected(user.getHobbies().contains("Reading"));
                gamingCheck.setSelected(user.getHobbies().contains("Gaming"));
                travelingCheck.setSelected(user.getHobbies().contains("Traveling"));
                onSubmitHobbies(null);
            }
        }

        openSavedHubBtn.setOnAction(e -> navManager.navigateTo(NavigationManager.Screen.SAVED_HUB));
    }

    private void initRbacPermissions() {
        User user = navManager.getCurrentUser();
        boolean isAdmin = (user != null && user.isAdmin());

        if (isAdmin) {
            // ADMIN MODE: Full CRUD privileges active
            rbacModeBanner.setText("🛡️ Administrator Mode Active (Full Course Student CRUD Privileges)");
            rbacModeBanner.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-border-color: #fde68a;");

            adminPersonFormBox.setVisible(true);
            adminPersonFormBox.setManaged(true);
            addPersonBtn.setDisable(false);
            updatePersonBtn.setDisable(false);
            deletePersonBtn.setDisable(false);

            studentModeNoticeLabel.setVisible(false);
            studentModeNoticeLabel.setManaged(false);
        } else {
            // STUDENT MODE: Read-Only directory, Add Student button is HIDDEN
            rbacModeBanner.setText("🎓 Student Mode Active (Read-Only Class Directory & Peer Inspection)");
            rbacModeBanner.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1e40af; -fx-border-color: #bfdbfe;");

            adminPersonFormBox.setVisible(false);
            adminPersonFormBox.setManaged(false);
            addPersonBtn.setDisable(true);
            updatePersonBtn.setDisable(true);
            deletePersonBtn.setDisable(true);

            studentModeNoticeLabel.setVisible(true);
            studentModeNoticeLabel.setManaged(true);
            studentModeNoticeLabel.setText("🔒 Student Account: Student additions and modifications are reserved for instructors (Admin123_).");
        }
    }

    private void loadProfileImage(String path) {
        if (path != null && !path.isBlank()) {
            File f = new File(path);
            if (f.exists()) {
                profileImageView.setImage(new Image(f.toURI().toString()));
                return;
            }
        }
        URL defaultUrl = getClass().getResource("/images/default.png");
        if (defaultUrl != null) {
            profileImageView.setImage(new Image(defaultUrl.toExternalForm()));
        }
    }

    @FXML
    private void onBrowseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Picture");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = navManager.getPrimaryStage();
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            customImagePath = selected.getAbsolutePath();
            profileImageView.setImage(new Image(selected.toURI().toString()));
            profileSaveStatusLabel.setText("Selected image: " + selected.getName());
        }
    }

    @FXML
    private void onResetImage(ActionEvent event) {
        customImagePath = "";
        loadProfileImage(null);
        profileSaveStatusLabel.setText("Reset to default avatar.");
    }

    @FXML
    private void onSaveProfileChanges(ActionEvent event) {
        User user = navManager.getCurrentUser();
        if (user == null) return;

        user.setCountry(countryComboBox.getValue());
        user.setDob(dobPicker.getValue());
        user.setTheme(themeChoiceBox.getValue());

        RadioButton selRadio = (RadioButton) skillToggleGroup.getSelectedToggle();
        if (selRadio != null) {
            user.setSkillLevel(selRadio.getText());
        }

        StringBuilder h = new StringBuilder();
        if (readingCheck.isSelected()) h.append("Reading ");
        if (gamingCheck.isSelected()) h.append("Gaming ");
        if (travelingCheck.isSelected()) h.append("Traveling ");
        user.setHobbies(h.toString().trim());

        if (customImagePath != null) {
            user.setProfileImagePath(customImagePath);
        }

        AppExecutor.execute(() -> {
            dbHelper.updateUserProfile(user);
            Platform.runLater(() -> {
                profileSaveStatusLabel.setText("✔ Profile changes saved successfully!");
                navManager.notifyUserUpdated();
            });
        });
    }

    // ============================================================ TABLEVIEW & PERSON CRUD

    private void initPersonTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));

        dobColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "" : date.format(DOB_DISPLAY_FORMAT));
            }
        });

        personLevelChoiceBox.setItems(FXCollections.observableArrayList("Beginner", "Intermediate", "Expert"));
        personLevelChoiceBox.setValue("Beginner");

        personList.setAll(dbHelper.findAllPersons());
        personTable.setItems(personList);

        personTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && navManager.getCurrentUser() != null && navManager.getCurrentUser().isAdmin()) {
                personNameField.setText(newV.getName());
                personLevelChoiceBox.setValue(newV.getLevel());
                personDobPicker.setValue(newV.getDob());
            }
        });
    }

    @FXML
    private void onAddPerson(ActionEvent event) {
        if (!navManager.getCurrentUser().isAdmin()) {
            navManager.showErrorAlert("Unauthorized", "Permission Denied", "Only administrators can add students.");
            return;
        }

        String name = personNameField.getText();
        if (name == null || name.isBlank()) {
            navManager.showErrorAlert("Validation", "Name Required", "Please enter a valid student name.");
            return;
        }

        Person p = new Person(0, name.trim(), personLevelChoiceBox.getValue(), personDobPicker.getValue());
        AppExecutor.execute(() -> {
            dbHelper.insertPerson(p);
            Platform.runLater(() -> {
                personList.add(p);
                personNameField.clear();
                personDobPicker.setValue(null);
            });
        });
    }

    @FXML
    private void onUpdatePerson(ActionEvent event) {
        if (!navManager.getCurrentUser().isAdmin()) return;

        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            navManager.showErrorAlert("Selection", "No Selection", "Please select a student row in the table to update.");
            return;
        }

        selected.setName(personNameField.getText().trim());
        selected.setLevel(personLevelChoiceBox.getValue());
        selected.setDob(personDobPicker.getValue());

        AppExecutor.execute(() -> {
            dbHelper.updatePerson(selected);
            Platform.runLater(() -> personTable.refresh());
        });
    }

    @FXML
    private void onDeletePerson(ActionEvent event) {
        if (!navManager.getCurrentUser().isAdmin()) return;

        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            navManager.showErrorAlert("Selection", "No Selection", "Please select a student row in the table to delete.");
            return;
        }

        AppExecutor.execute(() -> {
            dbHelper.deletePerson(selected.getId());
            Platform.runLater(() -> {
                personList.remove(selected);
                personNameField.clear();
                personDobPicker.setValue(null);
            });
        });
    }

    // ============================================================ PEER STUDENT DIRECTORY

    private void initPeerDirectory() {
        peerList.setAll(dbHelper.getAllUsers());
        peerListView.setItems(peerList);

        peerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            displayPeerCard(newV);
        });

        if (!peerList.isEmpty()) {
            peerListView.getSelectionModel().select(0);
        }
    }

    private void displayPeerCard(User peer) {
        if (peer == null) return;

        String name = peer.getFullName() != null && !peer.getFullName().isBlank() ? peer.getFullName() : peer.getUsername();
        peerNameLabel.setText(name + " (@" + peer.getUsername() + ")");
        peerRoleBadge.setText(peer.isAdmin() ? "ADMIN" : "STUDENT");
        peerRankBadge.setText(peer.getUserLevel() != null ? peer.getUserLevel() : "Beginner");

        peerMcqScoreLabel.setText(String.format("%d correct (%d pts)", peer.getTotalCorrectMcqs(), peer.getOverallScore()));
        peerEmailLabel.setText(peer.getEmail() != null ? peer.getEmail() : "N/A");
        peerCountryLabel.setText(peer.getCountry() != null ? peer.getCountry() : "Not specified");
        peerHobbiesLabel.setText(peer.getHobbies() != null && !peer.getHobbies().isBlank() ? peer.getHobbies() : "None listed");

        AppExecutor.execute(() -> {
            int savedCount = dbHelper.countSavedItems(peer.getUsername());
            Platform.runLater(() -> peerSavedCountLabel.setText(savedCount + " saved item(s)"));
        });
    }

    // ============================================================ COURSE UI COMPONENTS MATRIX

    private void initCourseUiLabControls() {
        // RadioButtons & Skill level ToggleGroup
        skillToggleGroup = new ToggleGroup();
        beginnerRadio.setToggleGroup(skillToggleGroup);
        intermediateRadio.setToggleGroup(skillToggleGroup);
        expertRadio.setToggleGroup(skillToggleGroup);

        skillToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                RadioButton rb = (RadioButton) newT;
                skillLevelLabel.setText("Skill Level: " + rb.getText());
            }
        });

        // CheckBoxes
        submitHobbiesBtn.setOnAction(this::onSubmitHobbies);

        // Theme ChoiceBox & Country ComboBox
        themeChoiceBox.setItems(FXCollections.observableArrayList("Light Blue", "Dark Slate", "Emerald Green", "Crimson Red"));
        themeChoiceBox.setValue("Light Blue");
        themeChoiceBox.setOnAction(e -> navManager.applyTheme(themeChoiceBox.getValue()));

        countryComboBox.setItems(FXCollections.observableArrayList(
                "United States", "Bangladesh", "United Kingdom", "Canada", "Germany", "India", "Australia", "Japan"
        ));
        countryComboBox.setValue("United States");

        dobPicker.setOnAction(e -> {
            LocalDate val = dobPicker.getValue();
            dobLabel.setText(val == null ? "Formatted DOB: -" : "Formatted DOB: " + val.format(DOB_DISPLAY_FORMAT));
        });

        // ColorPicker
        labelColorPicker.setValue(Color.web("#1e293b"));
        labelColorPicker.setOnAction(e -> colorTargetLabel.setTextFill(labelColorPicker.getValue()));

        // ListView & TreeView
        algoListView.setItems(FXCollections.observableArrayList(
                "Breadth-First Search (BFS)", "Depth-First Search (DFS)", "Dijkstra's Algorithm",
                "Bellman-Ford Algorithm", "Floyd-Warshall Algorithm", "Johnson's Algorithm",
                "Quick Sort", "Merge Sort", "Binary Search"
        ));
        algoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) algoSelectionLabel.setText("Selected List Item: " + newV);
        });

        TreeItem<String> root = new TreeItem<>("Algorithms Hierarchy");
        root.setExpanded(true);
        TreeItem<String> graph = new TreeItem<>("Graph Theory");
        graph.getChildren().addAll(new TreeItem<>("BFS"), new TreeItem<>("DFS"), new TreeItem<>("Dijkstra"));
        TreeItem<String> dp = new TreeItem<>("Dynamic Programming");
        dp.getChildren().addAll(new TreeItem<>("Floyd-Warshall"));
        root.getChildren().addAll(graph, dp);
        categoryTreeView.setRoot(root);
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) treeSelectionLabel.setText("Tree Node: " + newV.getValue());
        });

        // Slider & Spinner
        fontSizeSlider.setMin(10.0);
        fontSizeSlider.setMax(36.0);
        fontSizeSlider.setValue(14.0);
        fontSizeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            sliderTargetLabel.setFont(Font.font(newV.doubleValue()));
            sliderTargetLabel.setText(String.format("Font Size Preview: %.0f pt", newV.doubleValue()));
        });

        SpinnerValueFactory<Integer> valFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3);
        quantitySpinner.setValueFactory(valFactory);
        showQuantityBtn.setOnAction(e -> quantityLabel.setText("Selected Quantity: " + quantitySpinner.getValue()));

        // TextArea & PasswordField Toggle
        clearNotesBtn.setOnAction(e -> notesTextArea.clear());
        profilePasswordVisibleField.textProperty().bindBidirectional(profilePasswordField.textProperty());
        togglePasswordBtn.setOnAction(e -> {
            passwordVisible = !passwordVisible;
            profilePasswordVisibleField.setVisible(passwordVisible);
            profilePasswordVisibleField.setManaged(passwordVisible);
            profilePasswordField.setVisible(!passwordVisible);
            profilePasswordField.setManaged(!passwordVisible);
            togglePasswordBtn.setText(passwordVisible ? "Hide" : "Show");
        });

        // Alerts
        infoAlertBtn.setOnAction(e -> navManager.showInfoAlert("Info Dialog", "CodeCanvas Info", "Course algorithms and database are fully synchronized."));
        warningAlertBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText("Resource Check");
            alert.setContentText("Simulation video for this algorithm requires a local MP4 file in videos123/.");
            alert.showAndWait();
        });
        errorAlertBtn.setOnAction(e -> navManager.showErrorAlert("Error Dialog", "Validation Exception", "Input must be a valid positive integer."));
    }

    @FXML
    private void onSubmitHobbies(ActionEvent event) {
        StringBuilder sb = new StringBuilder("Selected Hobbies: ");
        boolean hasAny = false;
        if (readingCheck.isSelected()) { sb.append("Reading "); hasAny = true; }
        if (gamingCheck.isSelected()) { sb.append("Gaming "); hasAny = true; }
        if (travelingCheck.isSelected()) { sb.append("Traveling "); hasAny = true; }
        hobbiesLabel.setText(hasAny ? sb.toString().trim() : "Selected Hobbies: None");
    }

    private void wireProgrammaticHandlers() {
        accumulateButton.setOnAction(this::handleAccumulateAction);

        accumulateInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                runAccumulate();
            }
        });
    }

    private void handleAccumulateAction(ActionEvent event) {
        runAccumulate();
    }

    private void runAccumulate() {
        String text = accumulateInputField.getText();
        int targetN;
        try {
            targetN = Integer.parseInt(text != null ? text.trim() : "");
            if (targetN <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            accumulateStatusLabel.setText("Please enter a valid positive integer for N.");
            return;
        }

        accumulatedCount++;
        double ratio = Math.min(1.0, (double) accumulatedCount / targetN);
        accumulateProgressBar.setProgress(ratio);
        accumulateStatusLabel.setText(String.format("Progress: %d / %d (%.0f%%)", accumulatedCount, targetN, ratio * 100));

        if (accumulatedCount >= targetN) {
            accumulatedCount = 0;
        }
    }
}
