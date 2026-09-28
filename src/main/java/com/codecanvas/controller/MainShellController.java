package com.codecanvas.controller;

import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the outer application shell.
 * Implements a SINGLE unified header bar, consolidating redundant profile controls
 * into ONE clean user profile chip with dynamic window property bindings for responsive scaling,
 * plus a unified header search bar that navigates and filters the Algorithm Dashboard.
 */
public class MainShellController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @FXML private BorderPane shellRoot;
    @FXML private HBox navHeaderHBox;
    @FXML private Button backButton;
    @FXML private Label brandLabel;
    @FXML private Label screenTitleLabel;
    @FXML private TextField headerSearchField;
    @FXML private ChoiceBox<String> globalThemeChoiceBox;
    @FXML private Button userProfileChip;
    @FXML private Button logoutButton;
    @FXML private StackPane contentHost;
    @FXML private Label globalStatusLabel;

    private final NavigationManager navManager = NavigationManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Theme choicebox with modern theme options
        globalThemeChoiceBox.setItems(FXCollections.observableArrayList(
                "Light Blue", "Dark Slate", "Emerald Green", "Crimson Red"
        ));
        globalThemeChoiceBox.setValue("Light Blue");
        globalThemeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                navManager.applyTheme(newV);
                setStatus("Active Theme: " + newV);
            }
        });

        // Dynamic Property Bindings: Scalable title based on window width
        screenTitleLabel.styleProperty().bind(
                Bindings.concat("-fx-font-size: ",
                        Bindings.min(16, Bindings.max(13, shellRoot.widthProperty().divide(75))),
                        "px; -fx-font-weight: 700;")
        );

        // Unified Header Search: filters algorithms on Dashboard
        headerSearchField.textProperty().addListener((obs, oldV, newV) -> {
            if (navManager.getCurrentUser() != null && newV != null) {
                if (navManager.getCurrentEntry() == null || navManager.getCurrentEntry().getScreen() != NavigationManager.Screen.DASHBOARD) {
                    navManager.navigateTo(NavigationManager.Screen.DASHBOARD);
                }
                navManager.triggerGlobalSearch(newV);
            }
        });

        headerSearchField.setOnAction(e -> {
            String text = headerSearchField.getText();
            if (navManager.getCurrentUser() != null && text != null) {
                if (navManager.getCurrentEntry() == null || navManager.getCurrentEntry().getScreen() != NavigationManager.Screen.DASHBOARD) {
                    navManager.navigateTo(NavigationManager.Screen.DASHBOARD);
                }
                navManager.triggerGlobalSearch(text);
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

        navManager.setUserStateListener(this::updateUserProfileChip);

        backButton.setOnAction(e -> navManager.goBack());

        setStatus("CodeCanvas Academic Ready • " + LocalDate.now().format(DATE_FORMAT));
    }

    public StackPane getContentHost() {
        return contentHost;
    }

    public void setStatus(String message) {
        if (globalStatusLabel != null) {
            globalStatusLabel.setText(message);
        }
    }

    /**
     * Consolidates user identity into a single profile chip in the unified header bar.
     */
    private void updateUserProfileChip(User user) {
        if (user != null) {
            String displayName = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
            String icon = user.isAdmin() ? "🛡️ " : "👤 ";
            
            // Format chip nicely: e.g. "👤 Tasnia Rahman"
            userProfileChip.setText(icon + displayName);
            userProfileChip.setVisible(true);
            userProfileChip.setManaged(true);
            logoutButton.setVisible(true);
            logoutButton.setManaged(true);
            headerSearchField.setVisible(true);
            headerSearchField.setManaged(true);

            if (user.getTheme() != null) {
                for (String t : globalThemeChoiceBox.getItems()) {
                    if (t.toLowerCase().contains(user.getTheme().toLowerCase())) {
                        globalThemeChoiceBox.setValue(t);
                        break;
                    }
                }
            }
        } else {
            userProfileChip.setVisible(false);
            userProfileChip.setManaged(false);
            logoutButton.setVisible(false);
            logoutButton.setManaged(false);
            headerSearchField.setVisible(false);
            headerSearchField.setManaged(false);
        }
    }

    @FXML
    private void onProfileClicked(ActionEvent event) {
        navManager.navigateTo(NavigationManager.Screen.PROFILE);
    }

    @FXML
    private void onLogoutClicked(ActionEvent event) {
        headerSearchField.clear();
        navManager.setCurrentUser(null);
        navManager.clearBackStack();
        navManager.navigateTo(NavigationManager.Screen.AUTH);
        setStatus("Logged out. Please sign in.");
    }
}
