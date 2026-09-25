package com.codecanvas;

import com.codecanvas.controller.MainShellController;
import com.codecanvas.db.DBHelper;
import com.codecanvas.navigation.NavigationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

/**
 * Application entry point for CodeCanvas - Production-ready Algorithm Visualizer.
 * Initializes SQLite persistence schema, creates videos123/ directory, loads the
 * main navigation shell, and displays the Authentication System (Figure 1).
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Ensure SQLite schema exists (users, saved_items, person, execution_trace)
        new DBHelper().initSchema();

        // 2. Ensure local video directory exists for dynamic simulation video detection
        File videoDir = new File("videos123");
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        // 3. Load Main Shell layout (Navigation header, back-button stack, theme, content host)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codecanvas/main_shell.fxml"));
        Parent root = loader.load();
        MainShellController shellController = loader.getController();

        Scene scene = new Scene(root, 1140, 780);

        // 4. Initialize central navigation manager with primary stage and content host
        NavigationManager navManager = NavigationManager.getInstance();
        navManager.initialize(primaryStage, shellController.getContentHost(), scene);

        // 5. Navigate to Authentication System (Figure 1)
        navManager.navigateTo(NavigationManager.Screen.AUTH);

        primaryStage.setTitle("CodeCanvas - Interactive CS Algorithm Visualizer");
        
        // Window icon
        URL iconUrl = getClass().getResource("/images/default.png");
        if (iconUrl != null) {
            primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }

        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
