package com.codecanvas.controller;

import com.codecanvas.api.ApiService;
import com.codecanvas.db.DBHelper;
import com.codecanvas.model.AlgorithmItem;
import com.codecanvas.model.SavedItem;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for Figures 3 & 4: Algorithm Detail Hub & Resources.
 * Coordinates the three core views ([Algorithm], [Code], [Simulator]),
 * dynamic GitHub REST API fetching, clipboard actions, SQLite bookmarking,
 * and dynamic local video playback with missing-file fallbacks.
 */
public class AlgorithmDetailController implements Initializable, NavigationManager.ParameterConsumer {

    private final ApiService apiService = new ApiService();
    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    // Algorithm Header
    @FXML private Label algoTitleLabel;
    @FXML private Label categoryBadgeLabel;
    @FXML private Label timeComplexityLabel;
    @FXML private Label spaceComplexityLabel;

    // View triggers
    @FXML private ToggleButton algoViewTrigger;
    @FXML private ToggleButton codeViewTrigger;
    @FXML private ToggleButton simulatorViewTrigger;
    private ToggleGroup navGroup;

    // Actions
    @FXML private Button copyCodeBtn;
    @FXML private Button saveBookmarkBtn;
    @FXML private Label actionFeedbackLabel;

    // Content Panes
    @FXML private StackPane contentCardStack;
    @FXML private VBox algorithmPane;
    @FXML private VBox codePane;
    @FXML private VBox simulatorPane;

    // [Algorithm] View controls
    @FXML private TextArea algorithmMarkdownArea;
    @FXML private ProgressIndicator algoLoadingSpinner;

    // [Code] View controls
    @FXML private TextArea codeEditorArea;
    @FXML private ProgressIndicator codeLoadingSpinner;

    // [Simulator] View controls
    @FXML private VBox mediaViewContainer;
    @FXML private MediaView simulationMediaView;
    @FXML private Button videoPlayBtn;
    @FXML private Button videoPauseBtn;
    @FXML private Slider videoProgressSlider;
    @FXML private Label videoTimeLabel;
    @FXML private Slider videoVolumeSlider;
    @FXML private VBox fallbackBox;
    @FXML private Label videoUnavailableLabel;
    @FXML private Label fallbackDetailLabel;

    private AlgorithmItem currentAlgorithm;
    private MediaPlayer mediaPlayer;
    private boolean isUserDraggingSlider = false;
    private String currentViewType = "Algorithm";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navGroup = new ToggleGroup();
        algoViewTrigger.setToggleGroup(navGroup);
        codeViewTrigger.setToggleGroup(navGroup);
        simulatorViewTrigger.setToggleGroup(navGroup);
        algoViewTrigger.setSelected(true);

        algoViewTrigger.setOnAction(e -> switchView("Algorithm"));
        codeViewTrigger.setOnAction(e -> switchView("Code"));
        simulatorViewTrigger.setOnAction(e -> switchView("Simulator"));

        // Volume slider default
        videoVolumeSlider.setValue(80.0);
        videoVolumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newV.doubleValue() / 100.0);
            }
        });

        // Scrubber slider interaction
        videoProgressSlider.setOnMousePressed(e -> isUserDraggingSlider = true);
        videoProgressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(videoProgressSlider.getValue()));
            }
            isUserDraggingSlider = false;
        });

        videoPlayBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });

        videoPauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });

        copyCodeBtn.setOnAction(this::onCopyCodeClicked);
        saveBookmarkBtn.setOnAction(this::onSaveBookmarkClicked);
    }

    @Override
    public void setParameter(Object parameter) {
        if (parameter instanceof AlgorithmItem) {
            this.currentAlgorithm = (AlgorithmItem) parameter;
            populateAlgorithmData();
        }
    }

    private void populateAlgorithmData() {
        if (currentAlgorithm == null) return;

        algoTitleLabel.setText(currentAlgorithm.getName());
        categoryBadgeLabel.setText(currentAlgorithm.getCategory());
        timeComplexityLabel.setText("Time: " + currentAlgorithm.getTimeComplexity());
        spaceComplexityLabel.setText("Space: " + currentAlgorithm.getSpaceComplexity());

        // Default to Algorithm overview view
        switchView("Algorithm");
        fetchAlgorithmOverview();
        fetchAlgorithmSourceCode();
        checkAndPrepareVideo();
        updateSaveButtonState();
    }

    private void switchView(String viewName) {
        this.currentViewType = viewName;
        algorithmPane.setVisible(viewName.equals("Algorithm"));
        algorithmPane.setManaged(viewName.equals("Algorithm"));

        codePane.setVisible(viewName.equals("Code"));
        codePane.setManaged(viewName.equals("Code"));

        simulatorPane.setVisible(viewName.equals("Simulator"));
        simulatorPane.setManaged(viewName.equals("Simulator"));

        copyCodeBtn.setVisible(viewName.equals("Code"));
        copyCodeBtn.setManaged(viewName.equals("Code"));

        updateSaveButtonState();
    }

    private void fetchAlgorithmOverview() {
        algoLoadingSpinner.setVisible(true);
        algorithmMarkdownArea.setText("Fetching algorithm documentation from GitHub repository...");

        Task<String> docTask = new Task<>() {
            @Override
            protected String call() {
                return apiService.fetchAlgorithmMarkdown(currentAlgorithm);
            }
        };

        docTask.setOnSucceeded(e -> {
            algorithmMarkdownArea.setText(docTask.getValue());
            algoLoadingSpinner.setVisible(false);
        });

        docTask.setOnFailed(e -> {
            algorithmMarkdownArea.setText("# " + currentAlgorithm.getName() + "\n\n" + currentAlgorithm.getDescription());
            algoLoadingSpinner.setVisible(false);
        });

        Thread th = new Thread(docTask, "codecanvas-doc-fetch");
        th.setDaemon(true);
        th.start();
    }

    private void fetchAlgorithmSourceCode() {
        codeLoadingSpinner.setVisible(true);
        codeEditorArea.setText("// Fetching source code implementation from GitHub repository...");

        Task<String> codeTask = new Task<>() {
            @Override
            protected String call() {
                return apiService.fetchAlgorithmCode(currentAlgorithm);
            }
        };

        codeTask.setOnSucceeded(e -> {
            codeEditorArea.setText(codeTask.getValue());
            codeLoadingSpinner.setVisible(false);
        });

        codeTask.setOnFailed(e -> {
            codeEditorArea.setText("// Code preview unavailable.");
            codeLoadingSpinner.setVisible(false);
        });

        Thread th = new Thread(codeTask, "codecanvas-code-fetch");
        th.setDaemon(true);
        th.start();
    }

    /**
     * Simulator View & Video Fallback Logic:
     * Dynamic check for `videos123/{AlgorithmName}.mp4` locally before initializing MediaPlayer.
     */
    private void checkAndPrepareVideo() {
        disposeMediaPlayer();

        if (currentAlgorithm == null) return;

        // Check local videos123/ directory
        String fileName = currentAlgorithm.getVideoFileName() != null ? currentAlgorithm.getVideoFileName() : (currentAlgorithm.getName() + ".mp4");
        File videoFile = new File("videos123/" + fileName);

        if (!videoFile.exists()) {
            // Also try with sanitized name
            String sanitized = currentAlgorithm.getName().replaceAll("[^a-zA-Z0-9.-]", "") + ".mp4";
            videoFile = new File("videos123/" + sanitized);
        }

        if (videoFile.exists() && videoFile.isFile()) {
            // Video FOUND: render MediaView with full controls
            fallbackBox.setVisible(false);
            fallbackBox.setManaged(false);
            mediaViewContainer.setVisible(true);
            mediaViewContainer.setManaged(true);

            try {
                Media media = new Media(videoFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                simulationMediaView.setMediaPlayer(mediaPlayer);

                mediaPlayer.setOnReady(() -> {
                    Duration total = media.getDuration();
                    videoProgressSlider.setMin(0.0);
                    videoProgressSlider.setMax(total.toSeconds());
                    updateTimeLabel(Duration.ZERO, total);
                    mediaPlayer.setVolume(videoVolumeSlider.getValue() / 100.0);
                });

                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!isUserDraggingSlider && mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
                        videoProgressSlider.setValue(newTime.toSeconds());
                        updateTimeLabel(newTime, mediaPlayer.getTotalDuration());
                    }
                });

                mediaPlayer.setOnEndOfMedia(() -> {
                    mediaPlayer.pause();
                    mediaPlayer.seek(Duration.ZERO);
                });

            } catch (Exception ex) {
                showVideoFallback(fileName, "Error initializing media decoder: " + ex.getMessage());
            }

        } else {
            // Video NOT FOUND (e.g. Floyd-Warshall, Johnson's, etc.)
            showVideoFallback(fileName, "File 'videos123/" + fileName + "' does not exist locally.");
        }
    }

    private void showVideoFallback(String fileName, String reason) {
        mediaViewContainer.setVisible(false);
        mediaViewContainer.setManaged(false);
        fallbackBox.setVisible(true);
        fallbackBox.setManaged(true);
        videoUnavailableLabel.setText("Simulation video not available");
        fallbackDetailLabel.setText("Algorithm: " + currentAlgorithm.getName() + " (" + reason + ")\nPlace a valid MP4 into the videos123/ directory to view simulation.");
    }

    private void updateTimeLabel(Duration current, Duration total) {
        videoTimeLabel.setText(formatTime(current) + " / " + formatTime(total));
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) return "00:00";
        int seconds = (int) Math.floor(duration.toSeconds());
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    // ============================================================ ACTIONS

    @FXML
    private void onCopyCodeClicked(ActionEvent event) {
        String code = codeEditorArea.getText();
        if (code != null && !code.isBlank()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(code);
            clipboard.setContent(content);

            actionFeedbackLabel.setText("✔ Code copied to clipboard!");
            actionFeedbackLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    @FXML
    private void onSaveBookmarkClicked(ActionEvent event) {
        User user = navManager.getCurrentUser();
        if (user == null) {
            navManager.showErrorAlert("Not Logged In", "Authentication Required", "Please log in to save items to your profile.");
            return;
        }

        String type = currentViewType;
        String title = currentAlgorithm.getName() + " - " + type;
        String content = switch (type) {
            case "Code" -> codeEditorArea.getText();
            case "Simulator" -> "Simulation reference for " + currentAlgorithm.getName() + " (" + currentAlgorithm.getVideoFileName() + ")";
            default -> algorithmMarkdownArea.getText();
        };

        SavedItem item = new SavedItem(0, user.getUsername(), type, currentAlgorithm.getName(), title, content,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        dbHelper.insertSavedItem(item);
        actionFeedbackLabel.setText("✔ Saved [" + type + "] to your profile!");
        actionFeedbackLabel.setStyle("-fx-text-fill: #2980b9;");
        saveBookmarkBtn.setText("★ Saved");
    }

    private void updateSaveButtonState() {
        User user = navManager.getCurrentUser();
        if (user != null && currentAlgorithm != null) {
            boolean already = dbHelper.isSaved(user.getUsername(), currentAlgorithm.getName(), currentViewType);
            saveBookmarkBtn.setText(already ? "★ Saved" : "☆ Save");
        } else {
            saveBookmarkBtn.setText("☆ Save");
        }
    }
}
