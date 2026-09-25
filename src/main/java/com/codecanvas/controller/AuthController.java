package com.codecanvas.controller;

import com.codecanvas.db.DBHelper;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import com.codecanvas.util.PasswordValidator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Figure 1: Authentication System (Login & Sign-Up).
 * Enforces strong password validation with real-time requirement indicators,
 * SQLite credential persistence, and password reveal toggles.
 */
public class AuthController implements Initializable {

    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    // Mode switching
    @FXML private TabPane authTabPane;
    @FXML private Tab loginTab;
    @FXML private Tab signUpTab;

    // ----- Login Controls -----
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField loginPasswordVisibleField;
    @FXML private Button loginPasswordToggleBtn;
    @FXML private Label loginErrorLabel;
    @FXML private Button loginSubmitBtn;

    // ----- Sign-Up Controls -----
    @FXML private TextField signUpFullNameField;
    @FXML private TextField signUpUsernameField;
    @FXML private TextField signUpEmailField;
    @FXML private PasswordField signUpPasswordField;
    @FXML private TextField signUpPasswordVisibleField;
    @FXML private Button signUpPasswordToggleBtn;
    @FXML private PasswordField signUpConfirmPasswordField;
    @FXML private Label signUpErrorLabel;
    @FXML private Button signUpSubmitBtn;

    // Real-time password requirements indicators
    @FXML private Label reqLengthLabel;
    @FXML private Label reqUpperLabel;
    @FXML private Label reqLowerLabel;
    @FXML private Label reqDigitLabel;
    @FXML private Label reqSpecialLabel;

    private boolean loginPasswordVisible = false;
    private boolean signUpPasswordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Sync password plain-text toggles
        loginPasswordVisibleField.textProperty().bindBidirectional(loginPasswordField.textProperty());
        signUpPasswordVisibleField.textProperty().bindBidirectional(signUpPasswordField.textProperty());

        // Real-time password validator on sign-up password field
        signUpPasswordField.textProperty().addListener((obs, oldV, newV) -> updatePasswordRequirementsDisplay(newV));

        // Programmatic Enter key on login field to trigger login
        loginPasswordField.setOnAction(e -> onLoginSubmit(e));
        loginUsernameField.setOnAction(e -> loginPasswordField.requestFocus());
    }

    private void updatePasswordRequirementsDisplay(String password) {
        PasswordValidator.ValidationResult result = PasswordValidator.validate(password);

        styleRequirement(reqLengthLabel, result.isMinLength());
        styleRequirement(reqUpperLabel, result.isHasUpper());
        styleRequirement(reqLowerLabel, result.isHasLower());
        styleRequirement(reqDigitLabel, result.isHasDigit());
        styleRequirement(reqSpecialLabel, result.isHasSpecial());
    }

    private void styleRequirement(Label label, boolean met) {
        if (label == null) return;
        if (met) {
            label.setText(label.getText().replace("○", "●").replace("✖", "✔"));
            label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            label.setText(label.getText().replace("●", "○").replace("✔", "✖"));
            label.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    // ============================================================ LOGIN HANDLERS

    @FXML
    private void onToggleLoginPassword(ActionEvent event) {
        loginPasswordVisible = !loginPasswordVisible;
        loginPasswordVisibleField.setVisible(loginPasswordVisible);
        loginPasswordVisibleField.setManaged(loginPasswordVisible);
        loginPasswordField.setVisible(!loginPasswordVisible);
        loginPasswordField.setManaged(!loginPasswordVisible);
        loginPasswordToggleBtn.setText(loginPasswordVisible ? "Hide" : "Show");
    }

    @FXML
    private void onLoginSubmit(ActionEvent event) {
        loginErrorLabel.setText("");
        String username = loginUsernameField.getText() == null ? "" : loginUsernameField.getText().trim();
        String password = loginPasswordField.getText() == null ? "" : loginPasswordField.getText();

        if (username.isBlank() || password.isBlank()) {
            loginErrorLabel.setText("Please enter both username and password.");
            return;
        }

        User user = dbHelper.authenticateUser(username, password);
        if (user != null) {
            navManager.setCurrentUser(user);
            navManager.clearBackStack();
            navManager.navigateTo(NavigationManager.Screen.DASHBOARD);
        } else {
            loginErrorLabel.setText("Invalid username or password. Please try again.");
        }
    }

    // ============================================================ SIGN-UP HANDLERS

    @FXML
    private void onToggleSignUpPassword(ActionEvent event) {
        signUpPasswordVisible = !signUpPasswordVisible;
        signUpPasswordVisibleField.setVisible(signUpPasswordVisible);
        signUpPasswordVisibleField.setManaged(signUpPasswordVisible);
        signUpPasswordField.setVisible(!signUpPasswordVisible);
        signUpPasswordField.setManaged(!signUpPasswordVisible);
        signUpPasswordToggleBtn.setText(signUpPasswordVisible ? "Hide" : "Show");
    }

    @FXML
    private void onSignUpSubmit(ActionEvent event) {
        signUpErrorLabel.setText("");

        String fullName = signUpFullNameField.getText() == null ? "" : signUpFullNameField.getText().trim();
        String username = signUpUsernameField.getText() == null ? "" : signUpUsernameField.getText().trim();
        String email = signUpEmailField.getText() == null ? "" : signUpEmailField.getText().trim();
        String password = signUpPasswordField.getText() == null ? "" : signUpPasswordField.getText();
        String confirmPassword = signUpConfirmPasswordField.getText() == null ? "" : signUpConfirmPasswordField.getText();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            signUpErrorLabel.setText("Please fill out all required fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            signUpErrorLabel.setText("Passwords do not match.");
            return;
        }

        PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
        if (!result.isValid()) {
            signUpErrorLabel.setText(result.getSummaryErrorMessage());
            return;
        }

        User newUser = new User(username, "", "", fullName, email);
        boolean created = dbHelper.registerUser(newUser, password);
        if (!created) {
            signUpErrorLabel.setText("Username '" + username + "' is already taken. Please choose another.");
            return;
        }

        // Successfully created user!
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created");
        alert.setHeaderText("Welcome to CodeCanvas, " + (fullName.isBlank() ? username : fullName) + "!");
        alert.setContentText("Your account has been registered successfully. Logging you in now...");
        alert.showAndWait();

        User authUser = dbHelper.authenticateUser(username, password);
        navManager.setCurrentUser(authUser != null ? authUser : newUser);
        navManager.clearBackStack();
        navManager.navigateTo(NavigationManager.Screen.DASHBOARD);
    }
}
