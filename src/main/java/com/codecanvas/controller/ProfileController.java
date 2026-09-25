package com.codecanvas.controller;

import com.codecanvas.db.DBHelper;
import com.codecanvas.model.Person;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
 * Controller for Figure 5 (User Profile) and the Integrated Course UI Components Matrix.
 * Covers TableView (Person model), ImageView & FileChooser, RadioButton ToggleGroup,
 * CheckBoxes, Theme ChoiceBox, ComboBox, DatePicker (dd-MMM-yyyy), ColorPicker,
 * ListView & TreeView, ProgressBar + TextField accumulator, Slider & Spinner,
 * Alert Dialogs, TextArea with Clear, PasswordField with toggle, and programmatic handlers.
 */
public class ProfileController implements Initializable {

    private static final DateTimeFormatter DOB_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    // ----- Section 1: User Profile Header (Figure 5) -----
    @FXML private ImageView profileImageView;
    @FXML private Button browseImageBtn;
    @FXML private Button resetImageBtn;
    @FXML private Label usernameDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Button openSavedHubBtn;
    @FXML private Button saveProfileChangesBtn;
    @FXML private Label profileSaveStatusLabel;

    // ----- Section 2: TableView & Person CRUD -----
    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, Integer> idColumn;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> levelColumn;
    @FXML private TableColumn<Person, LocalDate> dobColumn;
    @FXML private TextField personNameField;
    @FXML private ChoiceBox<String> personLevelChoiceBox;
    @FXML private DatePicker personDobPicker;
    @FXML private Button addPersonBtn;
    @FXML private Button updatePersonBtn;
    @FXML private Button deletePersonBtn;

    // ----- Section 3: RadioButton & Skill Level ToggleGroup -----
    @FXML private RadioButton beginnerRadio;
    @FXML private RadioButton intermediateRadio;
    @FXML private RadioButton expertRadio;
    @FXML private Label skillLevelLabel;
    private ToggleGroup skillToggleGroup;

    // ----- Section 4: CheckBoxes & Hobbies -----
    @FXML private CheckBox readingCheck;
    @FXML private CheckBox gamingCheck;
    @FXML private CheckBox travelingCheck;
    @FXML private Button submitHobbiesBtn;
    @FXML private Label hobbiesLabel;

    // ----- Section 5: Theme, Country, DOB -----
    @FXML private ChoiceBox<String> themeChoiceBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private DatePicker dobPicker;
    @FXML private Label dobLabel;

    // ----- Section 6: ColorPicker & Text Styling -----
    @FXML private ColorPicker labelColorPicker;
    @FXML private Label colorTargetLabel;

    // ----- Section 7: ListView & TreeView -----
    @FXML private ListView<String> algoListView;
    @FXML private Label algoSelectionLabel;
    @FXML private TreeView<String> categoryTreeView;
    @FXML private Label treeSelectionLabel;

    // ----- Section 8: ProgressBar + Accumulator -----
    @FXML private TextField accumulateInputField;
    @FXML private Button accumulateButton;
    @FXML private ProgressBar accumulateProgressBar;
    @FXML private Label accumulateStatusLabel;
    private int accumulatedCount = 0;

    // ----- Section 9: Slider & Spinner -----
    @FXML private Slider fontSizeSlider;
    @FXML private Label sliderTargetLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button showQuantityBtn;
    @FXML private Label quantityLabel;

    // ----- Section 10: TextArea & PasswordField Toggle -----
    @FXML private TextArea notesTextArea;
    @FXML private Button clearNotesBtn;
    @FXML private PasswordField profilePasswordField;
    @FXML private TextField profilePasswordVisibleField;
    @FXML private Button togglePasswordBtn;
    private boolean passwordVisible = false;

    // ----- Section 11: Alert Dialog Triggers -----
    @FXML private Button infoAlertBtn;
    @FXML private Button warningAlertBtn;
    @FXML private Button errorAlertBtn;

    private final ObservableList<Person> personList = FXCollections.observableArrayList();
    private String customImagePath = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initUserProfile();
        initPersonTableView();
        initSkillLevelToggleGroup();
        initHobbies();
        initControlsThemeCountryDob();
        initColorPicker();
        initListAndTreeView();
        initSliderAndSpinner();
        initNotesAndPassword();
        initAlertButtons();
        wireProgrammaticHandlers();
    }

    // ============================================================ USER PROFILE (Figure 5)

    private void initUserProfile() {
        User user = navManager.getCurrentUser();
        loadProfileImage(user != null ? user.getProfileImagePath() : null);

        if (user != null) {
            usernameDisplayLabel.setText(user.getUsername() + " (" + (user.getFullName() != null ? user.getFullName() : "CS Student") + ")");
            emailDisplayLabel.setText(user.getEmail() != null ? user.getEmail() : "no-email@campus.edu");

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

        dbHelper.updateUserProfile(user);
        profileSaveStatusLabel.setText("✔ Profile updated successfully!");
    }

    // ============================================================ TABLEVIEW & PERSON CRUD

    private void initPersonTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));

        // Custom cell formatting for Date of Birth (dd-MMM-yyyy)
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
            if (newV != null) {
                personNameField.setText(newV.getName());
                personLevelChoiceBox.setValue(newV.getLevel());
                personDobPicker.setValue(newV.getDob());
            }
        });
    }

    @FXML
    private void onAddPerson(ActionEvent event) {
        String name = personNameField.getText();
        if (name == null || name.isBlank()) {
            navManager.showErrorAlert("Validation", "Name Required", "Please enter a valid person name.");
            return;
        }
        Person p = new Person(0, name.trim(), personLevelChoiceBox.getValue(), personDobPicker.getValue());
        dbHelper.insertPerson(p);
        personList.add(p);
        personNameField.clear();
        personDobPicker.setValue(null);
    }

    @FXML
    private void onUpdatePerson(ActionEvent event) {
        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            navManager.showErrorAlert("Selection", "No Selection", "Please select a row in the table to update.");
            return;
        }
        selected.setName(personNameField.getText().trim());
        selected.setLevel(personLevelChoiceBox.getValue());
        selected.setDob(personDobPicker.getValue());
        dbHelper.updatePerson(selected);
        personTable.refresh();
    }

    @FXML
    private void onDeletePerson(ActionEvent event) {
        Person selected = personTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            navManager.showErrorAlert("Selection", "No Selection", "Please select a row in the table to delete.");
            return;
        }
        dbHelper.deletePerson(selected.getId());
        personList.remove(selected);
        personNameField.clear();
        personDobPicker.setValue(null);
    }

    // ============================================================ RADIO BUTTON & SKILL LEVEL

    private void initSkillLevelToggleGroup() {
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
    }

    // ============================================================ CHECKBOXES & HOBBIES

    private void initHobbies() {
        submitHobbiesBtn.setOnAction(this::onSubmitHobbies);
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

    // ============================================================ THEME, COUNTRY, DOB

    private void initControlsThemeCountryDob() {
        themeChoiceBox.setItems(FXCollections.observableArrayList("Red", "Green", "Blue"));
        themeChoiceBox.setValue("Blue");
        themeChoiceBox.setOnAction(e -> navManager.applyTheme(themeChoiceBox.getValue()));

        countryComboBox.setItems(FXCollections.observableArrayList(
                "United States", "Bangladesh", "United Kingdom", "Canada", "Germany", "India", "Australia", "Japan"
        ));
        countryComboBox.setValue("United States");

        dobPicker.setOnAction(e -> {
            LocalDate val = dobPicker.getValue();
            dobLabel.setText(val == null ? "Formatted DOB: -" : "Formatted DOB: " + val.format(DOB_DISPLAY_FORMAT));
        });
    }

    // ============================================================ COLOR PICKER

    private void initColorPicker() {
        labelColorPicker.setValue(Color.web("#2c3e50"));
        labelColorPicker.setOnAction(e -> colorTargetLabel.setTextFill(labelColorPicker.getValue()));
    }

    // ============================================================ LISTVIEW & TREEVIEW

    private void initListAndTreeView() {
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
        graph.getChildren().addAll(new TreeItem<>("BFS"), new TreeItem<>("DFS"), new TreeItem<>("Dijkstra"), new TreeItem<>("Bellman-Ford"));

        TreeItem<String> dp = new TreeItem<>("Dynamic Programming");
        dp.getChildren().addAll(new TreeItem<>("Floyd-Warshall"));

        TreeItem<String> sorting = new TreeItem<>("Divide & Conquer");
        sorting.getChildren().addAll(new TreeItem<>("Quick Sort"), new TreeItem<>("Merge Sort"));

        root.getChildren().addAll(graph, dp, sorting);
        categoryTreeView.setRoot(root);

        categoryTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) treeSelectionLabel.setText("Tree Node Selected: " + newV.getValue());
        });
    }

    // ============================================================ SLIDER & SPINNER

    private void initSliderAndSpinner() {
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
    }

    // ============================================================ TEXTAREA & PASSWORD

    private void initNotesAndPassword() {
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
    }

    // ============================================================ ALERT DIALOGS

    private void initAlertButtons() {
        infoAlertBtn.setOnAction(e -> navManager.showInfoAlert("Information Dialog", "CodeCanvas Info", "The course lab algorithms have loaded with full SQLite persistence."));
        warningAlertBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText("Resource Alert");
            alert.setContentText("Simulation video for certain algorithms might be missing in videos123/.");
            alert.showAndWait();
        });
        errorAlertBtn.setOnAction(e -> navManager.showErrorAlert("Error Dialog", "Validation Exception", "Input must be a valid positive integer."));
    }

    // ============================================================ PROGRAMMATIC HANDLERS & ACCUMULATOR

    /**
     * Attaches setOnAction programmatically in Java controller for button
     * and onAction (Enter key) on TextField.
     */
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
            accumulatedCount = 0; // Reset for repeatable testing
        }
    }
}
