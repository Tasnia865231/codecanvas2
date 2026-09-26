package com.codecanvas.controller;

import com.codecanvas.api.ApiService;
import com.codecanvas.db.DBHelper;
import com.codecanvas.model.AlgorithmItem;
import com.codecanvas.model.McqBank;
import com.codecanvas.model.McqQuestion;
import com.codecanvas.model.SavedItem;
import com.codecanvas.model.User;
import com.codecanvas.navigation.NavigationManager;
import com.codecanvas.util.AppExecutor;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Figures 3 & 4 + Upgraded MCQ Quiz Module & Video Controls.
 * Features:
 * 1. 4 core view triggers ([Algorithm], [Code], [Simulator], [MCQ Quiz]).
 * 2. High-contrast TextArea display for code and documentation.
 * 3. Video player with dynamic Play/Pause toggle icon, speed selector (0.5x - 2.0x), and "Simulation video not available" fallback.
 * 4. 20-Question MCQ Quiz engine loaded from JSON files with RadioButton groups,
 *    Prominent Score Verdict Card directly above questions, green/red highlight feedback containers,
 *    and automated SQLite level promotion (100+ correct answers = Expert) with celebratory Alert.
 * 5. Dynamic Property Bindings for responsive scaling.
 */
public class AlgorithmDetailController implements Initializable, NavigationManager.ParameterConsumer {

    private final ApiService apiService = new ApiService();
    private final DBHelper dbHelper = new DBHelper();
    private final NavigationManager navManager = NavigationManager.getInstance();

    // Top Metadata Bar
    @FXML private Label algoTitleLabel;
    @FXML private Label categoryBadgeLabel;
    @FXML private Label timeComplexityLabel;
    @FXML private Label spaceComplexityLabel;
    @FXML private Button copyCodeBtn;
    @FXML private Button saveBookmarkBtn;
    @FXML private Label actionFeedbackLabel;

    // View triggers (Figure 3 core navigation)
    @FXML private ToggleButton algoViewTrigger;
    @FXML private ToggleButton codeViewTrigger;
    @FXML private ToggleButton simulatorViewTrigger;
    @FXML private ToggleButton mcqViewTrigger;
    private ToggleGroup navGroup;

    // Content Panes Stack
    @FXML private StackPane contentCardStack;
    @FXML private VBox algorithmPane;
    @FXML private VBox codePane;
    @FXML private VBox simulatorPane;
    @FXML private VBox mcqPane;

    // [Algorithm] View
    @FXML private TextArea algorithmMarkdownArea;
    @FXML private ProgressIndicator algoLoadingSpinner;

    // [Code] View
    @FXML private TextArea codeEditorArea;
    @FXML private ProgressIndicator codeLoadingSpinner;

    // [Simulator] View with Upgraded Media Controls
    @FXML private VBox mediaViewContainer;
    @FXML private MediaView simulationMediaView;
    @FXML private Button videoPlayPauseBtn;
    @FXML private ComboBox<String> videoSpeedComboBox;
    @FXML private Slider videoProgressSlider;
    @FXML private Label videoTimeLabel;
    @FXML private Slider videoVolumeSlider;
    @FXML private VBox fallbackBox;
    @FXML private Label videoUnavailableLabel;
    @FXML private Label fallbackDetailLabel;

    // [MCQ Quiz] View: 20 Questions, RadioChoices, Prominent Verdict Card
    @FXML private Label mcqTopicTitleLabel;
    @FXML private Label mcqScoreBadge;
    @FXML private Button submitQuizTopBtn;
    @FXML private Button submitQuizBottomBtn;
    @FXML private Button retakeQuizBtn;
    @FXML private VBox scoreVerdictCard;
    @FXML private Label scoreVerdictTitle;
    @FXML private Label scoreVerdictSubtitle;
    @FXML private ScrollPane mcqScrollPane;
    @FXML private VBox mcqQuestionsContainer;

    private AlgorithmItem currentAlgorithm;
    private MediaPlayer mediaPlayer;
    private boolean isUserDraggingSlider = false;
    private String currentViewType = "Algorithm";

    // MCQ Questions & UI State
    private List<McqQuestion> currentQuestions = new ArrayList<>();
    private final List<ToggleGroup> questionToggleGroups = new ArrayList<>();
    private final List<VBox> questionCardNodes = new ArrayList<>();
    private final List<VBox> feedbackCardNodes = new ArrayList<>();
    private final List<Label> verdictLabels = new ArrayList<>();
    private final List<Label> explanationLabels = new ArrayList<>();
    private boolean quizSubmitted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDynamicPropertyBindings();
        initNavigationTriggers();
        initSimulatorControls();
        initMcqControls();

        copyCodeBtn.setOnAction(this::onCopyCodeClicked);
        saveBookmarkBtn.setOnAction(this::onSaveBookmarkClicked);
    }

    /**
     * Academic Rubric Fix: Explicit window property bindings for responsive font scaling.
     */
    private void setupDynamicPropertyBindings() {
        if (contentCardStack != null) {
            algoTitleLabel.styleProperty().bind(
                    Bindings.concat("-fx-font-size: ",
                            Bindings.min(22.0, Bindings.max(16.0, contentCardStack.widthProperty().divide(45.0))),
                            "px; -fx-font-weight: 800;")
            );
        }
    }

    private void initNavigationTriggers() {
        navGroup = new ToggleGroup();
        algoViewTrigger.setToggleGroup(navGroup);
        codeViewTrigger.setToggleGroup(navGroup);
        simulatorViewTrigger.setToggleGroup(navGroup);
        mcqViewTrigger.setToggleGroup(navGroup);
        algoViewTrigger.setSelected(true);

        algoViewTrigger.setOnAction(e -> switchView("Algorithm"));
        codeViewTrigger.setOnAction(e -> switchView("Code"));
        simulatorViewTrigger.setOnAction(e -> switchView("Simulator"));
        mcqViewTrigger.setOnAction(e -> switchView("MCQ Quiz"));
    }

    private void initSimulatorControls() {
        // Speed selector dropdown (0.5x, 1.0x, 1.25x, 1.5x, 2.0x)
        videoSpeedComboBox.setItems(FXCollections.observableArrayList("0.5x", "1.0x", "1.25x", "1.5x", "2.0x"));
        videoSpeedComboBox.setValue("1.0x");
        videoSpeedComboBox.setOnAction(e -> {
            String selected = videoSpeedComboBox.getValue();
            if (selected != null && mediaPlayer != null) {
                try {
                    double rate = Double.parseDouble(selected.replace("x", ""));
                    mediaPlayer.setRate(rate);
                } catch (NumberFormatException ignored) {}
            }
        });

        // Volume control
        videoVolumeSlider.setValue(80.0);
        videoVolumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newV.doubleValue() / 100.0);
            }
        });

        // Timeline progress seek
        videoProgressSlider.setOnMousePressed(e -> isUserDraggingSlider = true);
        videoProgressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(videoProgressSlider.getValue()));
            }
            isUserDraggingSlider = false;
        });

        // Dynamic Play/Pause State Toggle
        videoPlayPauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            }
        });
    }

    private void initMcqControls() {
        submitQuizTopBtn.setOnAction(e -> submitQuiz());
        submitQuizBottomBtn.setOnAction(e -> submitQuiz());
        retakeQuizBtn.setOnAction(e -> resetQuiz());
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

        switchView("Algorithm");
        fetchAlgorithmOverview();
        fetchAlgorithmSourceCode();
        checkAndPrepareVideo();
        loadMcqQuiz();
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

        mcqPane.setVisible(viewName.equals("MCQ Quiz"));
        mcqPane.setManaged(viewName.equals("MCQ Quiz"));

        copyCodeBtn.setVisible(viewName.equals("Code"));
        copyCodeBtn.setManaged(viewName.equals("Code"));

        if (!viewName.equals("Simulator") && mediaPlayer != null) {
            mediaPlayer.pause();
        }

        updateSaveButtonState();
    }

    // ============================================================ ASYNC GITHUB API (ExecutorService)

    private void fetchAlgorithmOverview() {
        algoLoadingSpinner.setVisible(true);
        algorithmMarkdownArea.setText("Fetching documentation from GitHub REST API via AppExecutor...");

        AppExecutor.execute(() -> {
            String doc = apiService.fetchAlgorithmMarkdown(currentAlgorithm);
            Platform.runLater(() -> {
                algorithmMarkdownArea.setText(doc);
                algoLoadingSpinner.setVisible(false);
            });
        });
    }

    private void fetchAlgorithmSourceCode() {
        codeLoadingSpinner.setVisible(true);
        codeEditorArea.setText("// Fetching Java source code from GitHub REST API via AppExecutor...");

        AppExecutor.execute(() -> {
            String code = apiService.fetchAlgorithmCode(currentAlgorithm);
            Platform.runLater(() -> {
                codeEditorArea.setText(code);
                codeLoadingSpinner.setVisible(false);
            });
        });
    }

    // ============================================================ SIMULATOR & LOCAL VIDEO CONTROLS

    private void checkAndPrepareVideo() {
        disposeMediaPlayer();
        if (currentAlgorithm == null) return;

        String rawName = currentAlgorithm.getName();
        String candidateFile = currentAlgorithm.getVideoFileName();
        if (candidateFile == null || candidateFile.isBlank()) {
            candidateFile = rawName + ".mp4";
        }

        File videoFile = new File("videos123/" + candidateFile);

        // Sanitize check (e.g. Kruskal's -> Kruskal.mp4, Prim's -> Prims.mp4)
        if (!videoFile.exists()) {
            String sanitized = rawName.replace("'", "").replace("’", "").trim() + ".mp4";
            videoFile = new File("videos123/" + sanitized);
        }
        if (!videoFile.exists()) {
            String simplified = rawName.replaceAll("[^a-zA-Z0-9.-]", "") + ".mp4";
            videoFile = new File("videos123/" + simplified);
        }

        if (videoFile.exists() && videoFile.isFile()) {
            fallbackBox.setVisible(false);
            fallbackBox.setManaged(false);
            mediaViewContainer.setVisible(true);
            mediaViewContainer.setManaged(true);

            try {
                Media media = new Media(videoFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                simulationMediaView.setMediaPlayer(mediaPlayer);

                // Sync dynamic Play/Pause toggle with MediaPlayer.Status
                mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    Platform.runLater(() -> {
                        if (newStatus == MediaPlayer.Status.PLAYING) {
                            videoPlayPauseBtn.setText("⏸ Pause");
                        } else {
                            videoPlayPauseBtn.setText("▶ Play");
                        }
                    });
                });

                mediaPlayer.setOnReady(() -> {
                    Duration total = media.getDuration();
                    videoProgressSlider.setMin(0.0);
                    videoProgressSlider.setMax(total.toSeconds());
                    updateTimeLabel(Duration.ZERO, total);
                    mediaPlayer.setVolume(videoVolumeSlider.getValue() / 100.0);
                    
                    // Apply speed
                    String speedStr = videoSpeedComboBox.getValue();
                    if (speedStr != null) {
                        try {
                            mediaPlayer.setRate(Double.parseDouble(speedStr.replace("x", "")));
                        } catch (Exception ignored) {}
                    }
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
                    videoPlayPauseBtn.setText("▶ Play");
                });

            } catch (Exception ex) {
                showVideoFallback(candidateFile, "Decoder error: " + ex.getMessage());
            }

        } else {
            // Missing file fallback (Exact requirement label: "Simulation video not available")
            showVideoFallback(candidateFile, "File 'videos123/" + candidateFile + "' was not found locally.");
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
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    // ============================================================ MCQ QUIZ ENGINE (20 Questions & Promotions)

    private void loadMcqQuiz() {
        currentQuestions = McqBank.getQuestionsForAlgorithm(currentAlgorithm.getName());
        quizSubmitted = false;
        scoreVerdictCard.setVisible(false);
        scoreVerdictCard.setManaged(false);
        retakeQuizBtn.setVisible(false);
        retakeQuizBtn.setManaged(false);
        submitQuizTopBtn.setDisable(false);
        submitQuizBottomBtn.setDisable(false);

        mcqTopicTitleLabel.setText("20-Question MCQ Quiz: " + currentAlgorithm.getName());
        updateUserQuizBadge(0);

        buildQuestionsUI();
    }

    private void updateUserQuizBadge(int sessionCorrect) {
        User user = navManager.getCurrentUser();
        int totalMcqs = user != null ? user.getTotalCorrectMcqs() : 0;
        String level = user != null ? user.getUserLevel() : "Beginner";
        mcqScoreBadge.setText(String.format("Session: %d/20 | Total Correct: %d | Rank: %s", sessionCorrect, totalMcqs, level));
    }

    private void buildQuestionsUI() {
        mcqQuestionsContainer.getChildren().clear();
        questionToggleGroups.clear();
        questionCardNodes.clear();
        feedbackCardNodes.clear();
        verdictLabels.clear();
        explanationLabels.clear();

        for (int i = 0; i < currentQuestions.size(); i++) {
            McqQuestion q = currentQuestions.get(i);

            VBox questionCard = new VBox(8);
            questionCard.getStyleClass().add("mcq-question-card");
            questionCard.setPadding(new Insets(12, 16, 12, 16));

            // Question prompt
            Label qPrompt = new Label((i + 1) + ". " + q.getQuestionText());
            qPrompt.setWrapText(true);
            qPrompt.getStyleClass().add("mcq-question-label");

            // Option radio buttons
            VBox optionsBox = new VBox(6);
            optionsBox.getStyleClass().add("mcq-options-container");

            ToggleGroup group = new ToggleGroup();
            List<String> opts = q.getOptions();
            for (int j = 0; j < opts.size(); j++) {
                RadioButton rb = new RadioButton(opts.get(j));
                rb.setWrapText(true);
                rb.setToggleGroup(group);
                rb.getStyleClass().add("mcq-radio");
                rb.setUserData(j);
                optionsBox.getChildren().add(rb);
            }

            // Feedback box below each question (revealed on Submit Quiz)
            VBox feedbackBox = new VBox(4);
            feedbackBox.getStyleClass().add("mcq-feedback-box");
            feedbackBox.setVisible(false);
            feedbackBox.setManaged(false);

            Label verdictLabel = new Label();
            verdictLabel.getStyleClass().add("verdict-label");

            Label explanationLabel = new Label();
            explanationLabel.setWrapText(true);
            explanationLabel.getStyleClass().add("explanation-label");

            feedbackBox.getChildren().addAll(verdictLabel, explanationLabel);

            questionCard.getChildren().addAll(qPrompt, optionsBox, feedbackBox);

            mcqQuestionsContainer.getChildren().add(questionCard);

            questionToggleGroups.add(group);
            questionCardNodes.add(questionCard);
            feedbackCardNodes.add(feedbackBox);
            verdictLabels.add(verdictLabel);
            explanationLabels.add(explanationLabel);
        }
    }

    private void submitQuiz() {
        if (quizSubmitted) return;

        int answeredCount = 0;
        for (ToggleGroup g : questionToggleGroups) {
            if (g.getSelectedToggle() != null) answeredCount++;
        }

        if (answeredCount < currentQuestions.size()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Incomplete Quiz");
            confirm.setHeaderText("You have answered " + answeredCount + " of " + currentQuestions.size() + " questions.");
            confirm.setContentText("Do you want to submit the quiz now? Unanswered questions will be counted as incorrect.");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        quizSubmitted = true;
        int correctCount = 0;

        for (int i = 0; i < currentQuestions.size(); i++) {
            McqQuestion q = currentQuestions.get(i);
            ToggleGroup group = questionToggleGroups.get(i);
            VBox card = questionCardNodes.get(i);
            VBox feedbackBox = feedbackCardNodes.get(i);
            Label verdictLabel = verdictLabels.get(i);
            Label explanationLabel = explanationLabels.get(i);

            // Disable options after submission
            for (Toggle t : group.getToggles()) {
                if (t instanceof RadioButton) ((RadioButton) t).setDisable(true);
            }

            Toggle selected = group.getSelectedToggle();
            int selectedIndex = (selected != null && selected.getUserData() instanceof Integer)
                    ? (Integer) selected.getUserData() : -1;

            boolean correct = q.isCorrect(selectedIndex);
            if (correct) {
                correctCount++;
                card.getStyleClass().removeAll("mcq-question-card", "mcq-card-incorrect");
                card.getStyleClass().add("mcq-card-correct");

                verdictLabel.setText("✔ Correct! Outstanding grasp of algorithmic theory.");
                verdictLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 800;");
            } else {
                card.getStyleClass().removeAll("mcq-question-card", "mcq-card-correct");
                card.getStyleClass().add("mcq-card-incorrect");

                String correctOptionText = q.getOptions().get(q.getCorrectOptionIndex());
                verdictLabel.setText("✖ Incorrect. The correct answer is: " + correctOptionText);
                verdictLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 800;");
            }

            explanationLabel.setText(q.getExplanation());
            feedbackBox.setVisible(true);
            feedbackBox.setManaged(true);
        }

        // Calculate score percentage
        int totalQuestions = currentQuestions.size();
        int percentage = (int) Math.round(((double) correctCount / totalQuestions) * 100);

        // Display Prominent Score Verdict Card directly above the questions
        scoreVerdictTitle.setText(String.format("Quiz Results: %d / %d Correct (%d%%) - Level Progress Updated!",
                correctCount, totalQuestions, percentage));
        scoreVerdictCard.setVisible(true);
        scoreVerdictCard.setManaged(true);

        submitQuizTopBtn.setDisable(true);
        submitQuizBottomBtn.setDisable(true);
        retakeQuizBtn.setVisible(true);
        retakeQuizBtn.setManaged(true);

        // Scroll to top to view verdict card immediately
        mcqScrollPane.setVvalue(0.0);

        // Asynchronously update SQLite state and trigger level promotion if >= 100 correct MCQs
        User user = navManager.getCurrentUser();
        if (user != null) {
            int earnedScore = correctCount;
            AppExecutor.execute(() -> {
                String promotedLevel = null;
                for (int c = 0; c < earnedScore; c++) {
                    String promo = dbHelper.recordCorrectMcq(user.getUsername());
                    if (promo != null) promotedLevel = promo;
                }

                // Refresh updated user from SQLite
                User updated = dbHelper.getUser(user.getUsername());
                String finalPromotion = promotedLevel;
                Platform.runLater(() -> {
                    if (updated != null) {
                        user.setTotalCorrectMcqs(updated.getTotalCorrectMcqs());
                        user.setUserLevel(updated.getUserLevel());
                    }
                    scoreVerdictSubtitle.setText(String.format("All-time Correct MCQs: %d • Active Rank: %s",
                            user.getTotalCorrectMcqs(), user.getUserLevel()));
                    updateUserQuizBadge(earnedScore);
                    navManager.notifyUserUpdated();

                    // Level Promotion Alert (100+ correct MCQs across all topics promotes user to Expert)
                    if (finalPromotion != null && finalPromotion.equalsIgnoreCase("Expert")) {
                        showCelebratoryPromotionAlert(finalPromotion);
                    }
                });
            });
        }
    }

    private void showCelebratoryPromotionAlert(String newLevel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🏆 Level Promotion Milestone!");
        alert.setHeaderText("Outstanding Achievement, " + navManager.getCurrentUser().getFullName() + "!");
        alert.setContentText("You have accumulated 100+ correct MCQs across topics!\n" +
                "Your CodeCanvas proficiency level has been officially promoted to: " + newLevel.toUpperCase() + ".\n" +
                "You now hold Expert standing across all algorithm visualizer modules!");
        alert.showAndWait();
    }

    private void resetQuiz() {
        quizSubmitted = false;
        scoreVerdictCard.setVisible(false);
        scoreVerdictCard.setManaged(false);
        retakeQuizBtn.setVisible(false);
        retakeQuizBtn.setManaged(false);
        submitQuizTopBtn.setDisable(false);
        submitQuizBottomBtn.setDisable(false);

        buildQuestionsUI();
        mcqScrollPane.setVvalue(0.0);
    }

    // ============================================================ ACTIONS (Copy & Save)

    @FXML
    private void onCopyCodeClicked(ActionEvent event) {
        String code = codeEditorArea.getText();
        if (code != null && !code.isBlank()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(code);
            clipboard.setContent(content);

            actionFeedbackLabel.setText("✔ Code copied to clipboard!");
            actionFeedbackLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onSaveBookmarkClicked(ActionEvent event) {
        User user = navManager.getCurrentUser();
        if (user == null) {
            navManager.showErrorAlert("Not Logged In", "Authentication Required", "Please log in to save items to your profile.");
            return;
        }

        String type = currentViewType.equals("MCQ Quiz") ? "Algorithm" : currentViewType;
        String title = currentAlgorithm.getName() + " - " + type;
        String content = switch (type) {
            case "Code" -> codeEditorArea.getText();
            case "Simulator" -> "Simulation reference for " + currentAlgorithm.getName() + " (" + currentAlgorithm.getVideoFileName() + ")";
            default -> algorithmMarkdownArea.getText();
        };

        SavedItem item = new SavedItem(0, user.getUsername(), type, currentAlgorithm.getName(), title, content,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        AppExecutor.execute(() -> {
            dbHelper.insertSavedItem(item);
            Platform.runLater(() -> {
                actionFeedbackLabel.setText("✔ Saved [" + type + "] to profile!");
                actionFeedbackLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                saveBookmarkBtn.setText("★ Saved");
            });
        });
    }

    private void updateSaveButtonState() {
        User user = navManager.getCurrentUser();
        if (user != null && currentAlgorithm != null) {
            String checkType = currentViewType.equals("MCQ Quiz") ? "Algorithm" : currentViewType;
            boolean already = dbHelper.isSaved(user.getUsername(), currentAlgorithm.getName(), checkType);
            saveBookmarkBtn.setText(already ? "★ Saved" : "☆ Save");
        } else {
            saveBookmarkBtn.setText("☆ Save");
        }
    }
}
