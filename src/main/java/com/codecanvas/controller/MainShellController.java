package com.codecanvas.controller;

import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the outer application shell. Holds the back-button navigation header,
 * theme selector, user profile trigger, content host StackPane, and global status bar.
 */
public class MainShellController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @FXML private BorderPane shellRoot;
    @FXML private Button backButton;
    @FXML private Label screenTitleLabel;
    @FXML private ChoiceBox<String> globalThemeChoiceBox;
    @FXML private Button profileHeaderButton;
    @FXML private Button logoutButton;
    @FXML private StackPane contentHost;
    @FXML private Label globalStatusLabel;

    private final NavigationManager navManager = NavigationManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Theme choicebox
        globalThemeChoiceBox.setItems(FXCollections.observableArrayList("Red", "Green", "Blue"));
        globalThemeChoiceBox.setValue("Blue");
        globalThemeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                navManager.applyTheme(newV);
                setStatus("Theme switched to: " + newV);
            }
        });

        // Wire navigation state callbacks
        navManager.setBackButtonStateListener(canGoBack -> {
            backButton.setDisable(!canGoBack);
            backButton.setVisible(canGoBack);
            backButton.setManaged(canGoBack);
        });

        navManager.setScreenTitleListener(title -> {
            screenTitleLabel.setText(title);
        });

        navManager.setUserStateListener(this::updateUserDisplay);

        backButton.setOnAction(e -> navManager.goBack());

        setStatus("CodeCanvas Initialized • " + LocalDate.now().format(DATE_FORMAT));
    }

    public StackPane getContentHost() {
        return contentHost;
    }

    public void setStatus(String message) {
        if (globalStatusLabel != null) {
            globalStatusLabel.setText(message);
        }
    }

    private void updateUserDisplay(User user) {
        if (user != null) {
            profileHeaderButton.setText("👤 " + (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername()));
            profileHeaderButton.setVisible(true);
            profileHeaderButton.setManaged(true);
            logoutButton.setVisible(true);
            logoutButton.setManaged(true);
            if (user.getTheme() != null) {
                globalThemeChoiceBox.setValue(user.getTheme());
            }
        } else {
            profileHeaderButton.setVisible(false);
            profileHeaderButton.setManaged(false);
            logoutButton.setVisible(false);
            logoutButton.setManaged(false);
        }
    }

    @FXML
    private void onProfileClicked(ActionEvent event) {
        navManager.navigateTo(NavigationManager.Screen.PROFILE);
    }

    @FXML
    private void onLogoutClicked(ActionEvent event) {
        navManager.setCurrentUser(null);
        navManager.clearBackStack();
        navManager.navigateTo(NavigationManager.Screen.AUTH);
        setStatus("Logged out successfully.");
    }

    @FXML
    private void onExitClicked(ActionEvent event) {
        Platform.exit();
    }
}
