package com.codecanvas.navigation;

import com.codecanvas.model.AlgorithmItem;
import com.codecanvas.model.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * Central navigation and state coordinator for CodeCanvas.
 * Implements a back-button stack mechanism across Figures 1–7 and coordinates
 * session state (currentUser, active theme, scene container).
 */
public class NavigationManager {

    public enum Screen {
        AUTH("/com/codecanvas/auth_view.fxml", "Authentication"),
        DASHBOARD("/com/codecanvas/dashboard_view.fxml", "Algorithm Dashboard"),
        DETAIL("/com/codecanvas/algorithm_detail_view.fxml", "Algorithm Detail Hub"),
        ALGORITHM_DETAIL("/com/codecanvas/algorithm_detail_view.fxml", "Algorithm Detail Hub"),
        PROFILE("/com/codecanvas/profile_view.fxml", "Profile & Student Directory"),
        SAVED_HUB("/com/codecanvas/saved_hub_view.fxml", "Saved Bookmarks Hub");

        private final String fxmlPath;
        private final String defaultTitle;

        Screen(String fxmlPath, String defaultTitle) {
            this.fxmlPath = fxmlPath;
            this.defaultTitle = defaultTitle;
        }

        public String getFxmlPath() { return fxmlPath; }
        public String getDefaultTitle() { return defaultTitle; }
    }

    public static class NavigationEntry {
        private final Screen screen;
        private final Object parameter;
        private final String title;

        public NavigationEntry(Screen screen, Object parameter, String title) {
            this.screen = screen;
            this.parameter = parameter;
            this.title = title;
        }

        public Screen getScreen() { return screen; }
        public Object getParameter() { return parameter; }
        public String getTitle() { return title; }
    }

    private static final NavigationManager INSTANCE = new NavigationManager();

    private Stage primaryStage;
    private Scene primaryScene;
    private StackPane contentHost;
    private User currentUser;
    private String currentTheme = "Blue";

    private final Deque<NavigationEntry> backStack = new ArrayDeque<>();
    private NavigationEntry currentEntry;

    private Consumer<Boolean> backButtonStateListener;
    private Consumer<String> screenTitleListener;
    private Consumer<User> userStateListener;
    private Consumer<String> globalSearchListener;

    private NavigationManager() {
    }

    public static NavigationManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Stage stage, StackPane contentHost, Scene scene) {
        this.primaryStage = stage;
        this.contentHost = contentHost;
        this.primaryScene = scene;
        applyTheme(currentTheme);
    }

    public Stage getPrimaryStage() { return primaryStage; }
    public Scene getPrimaryScene() { return primaryScene; }
    public User getCurrentUser() { return currentUser; }
    public NavigationEntry getCurrentEntry() { return currentEntry; }

    public void setGlobalSearchListener(Consumer<String> listener) {
        this.globalSearchListener = listener;
    }

    public void triggerGlobalSearch(String query) {
        if (globalSearchListener != null) {
            globalSearchListener.accept(query);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getTheme() != null) {
            applyTheme(user.getTheme());
        }
        notifyUserUpdated();
    }

    public void notifyUserUpdated() {
        if (userStateListener != null) {
            Platform.runLater(() -> userStateListener.accept(currentUser));
        }
    }

    public void setBackButtonStateListener(Consumer<Boolean> listener) {
        this.backButtonStateListener = listener;
    }

    public void setScreenTitleListener(Consumer<String> listener) {
        this.screenTitleListener = listener;
    }

    public void setUserStateListener(Consumer<User> listener) {
        this.userStateListener = listener;
    }

    /**
     * Navigates to target screen, pushing the previous screen onto the back-button stack.
     */
    public void navigateTo(Screen screen, Object parameter) {
        if (currentEntry != null) {
            // Do not keep Auth in the back stack if transitioning to Dashboard
            if (currentEntry.getScreen() != Screen.AUTH || screen != Screen.DASHBOARD) {
                backStack.push(currentEntry);
            }
        }
        loadScreen(new NavigationEntry(screen, parameter, screen.getDefaultTitle()));
    }

    public void navigateTo(Screen screen) {
        navigateTo(screen, null);
    }

    /**
     * Pops previous screen from stack and navigates back.
     */
    public void goBack() {
        if (!backStack.isEmpty()) {
            NavigationEntry previous = backStack.pop();
            loadScreen(previous);
        }
    }

    public boolean canGoBack() {
        return !backStack.isEmpty();
    }

    public void clearBackStack() {
        backStack.clear();
        updateBackStatus();
    }

    private void loadScreen(NavigationEntry entry) {
        this.currentEntry = entry;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(entry.getScreen().getFxmlPath()));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ParameterConsumer) {
                ((ParameterConsumer) controller).setParameter(entry.getParameter());
            }

            contentHost.getChildren().setAll(view);

            if (screenTitleListener != null) {
                String title = entry.getTitle();
                if (entry.getParameter() instanceof AlgorithmItem) {
                    title = "Detail: " + ((AlgorithmItem) entry.getParameter()).getName();
                }
                screenTitleListener.accept(title);
            }

            updateBackStatus();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not load screen: " + entry.getScreen(), e.getMessage());
        }
    }

    private void updateBackStatus() {
        if (backButtonStateListener != null) {
            backButtonStateListener.accept(canGoBack());
        }
    }

    public void applyTheme(String theme) {
        if (theme == null) return;
        this.currentTheme = theme;
        if (currentUser != null) {
            currentUser.setTheme(theme);
        }
        if (primaryScene != null && primaryScene.getRoot() != null) {
            Node root = primaryScene.getRoot();
            root.getStyleClass().removeAll("theme-red", "theme-green", "theme-blue", "theme-slate", "theme-light", "theme-dark-slate");
            String lower = theme.toLowerCase();
            if (lower.contains("slate") || lower.contains("dark")) {
                root.getStyleClass().addAll("theme-dark-slate", "theme-slate");
            } else if (lower.contains("red") || lower.contains("crimson")) {
                root.getStyleClass().add("theme-red");
            } else if (lower.contains("green") || lower.contains("emerald")) {
                root.getStyleClass().add("theme-green");
            } else {
                root.getStyleClass().addAll("theme-light", "theme-blue");
            }
        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (primaryStage != null) alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (primaryStage != null) alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public interface ParameterConsumer {
        void setParameter(Object parameter);
    }
}
