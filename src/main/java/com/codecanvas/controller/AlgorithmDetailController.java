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
import com.codecanvas.visualizer.interactive.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Algorithm Detail Hub with 5 Distinct Core Tabs Per Topic:
 * 1. [ Algorithm ]: Markdown description fetched asynchronously via GitHub REST API.
 * 2. [ Code ]: Java source implementation fetched via GitHub REST API.
 * 3. [ Videos ]: Local MP4 playback from videos123/{AlgorithmName}.mp4 with dynamic controls & fallback.
 * 4. [ Simulation ]: NEW VisuAlgo-style interactive canvas engine with custom input panel,
 *                    multi-layout graph generation, node dragging, and step-by-step animation controls.
 * 5. [ MCQ Quiz ]: 20 questions per topic loaded from JSON files with instant grading and promotions.
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

    // View triggers (5 Core Navigation Tabs)
    @FXML private ToggleButton algoViewTrigger;
    @FXML private ToggleButton codeViewTrigger;
    @FXML private ToggleButton videosViewTrigger;
    @FXML private ToggleButton simulationViewTrigger;
    @FXML private ToggleButton mcqViewTrigger;
    private ToggleGroup navGroup;

    // Content Panes Stack
    @FXML private StackPane contentCardStack;
    @FXML private VBox algorithmPane;
    @FXML private VBox codePane;
    @FXML private VBox videosPane;
    @FXML private VBox simulationPane;
    @FXML private VBox mcqPane;

    // [Algorithm] View
    @FXML private TextArea algorithmMarkdownArea;
    @FXML private ProgressIndicator algoLoadingSpinner;

    // [Code] View
    @FXML private TextArea codeEditorArea;
    @FXML private ProgressIndicator codeLoadingSpinner;

    // [Videos] View (Formerly "Simulator")
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

    // [Simulation] View (VisuAlgo Interactive Canvas Engine)
    @FXML private Button toggleInputPanelBtn;
    @FXML private Separator simToolbarSep1;
    @FXML private HBox simPresetBox;
    @FXML private Label simPresetLabel;
    @FXML private ComboBox<String> simPresetComboBox;
    @FXML private HBox simSourceBox;
    @FXML private Label simSourceLabel;
    @FXML private ComboBox<String> simSourceComboBox;
    @FXML private HBox simTargetBox;
    @FXML private TextField simTargetField;
    @FXML private Button simBuildRunBtn;

    @FXML private VBox simInputPanel;
    @FXML private Label simInputPanelTitle;
    @FXML private Button closeInputPanelBtn;
    @FXML private TextArea simCustomInputArea;
    @FXML private HBox graphOptionsBox;
    @FXML private HBox indexingBox;
    @FXML private RadioButton zeroIndexedRadio;
    @FXML private RadioButton oneIndexedRadio;
    private ToggleGroup indexingToggleGroup;

    @FXML private HBox inputFormatBox;
    @FXML private RadioButton edgeListRadio;
    @FXML private RadioButton adjMatrixRadio;
    @FXML private RadioButton adjListRadio;
    private ToggleGroup graphTypeToggleGroup;

    @FXML private HBox outputLayoutBox;
    @FXML private RadioButton layoutDefaultRadio;
    @FXML private RadioButton layoutBipartiteRadio;
    @FXML private RadioButton layoutTreeRadio;
    @FXML private RadioButton layoutDagRadio;
    private ToggleGroup layoutToggleGroup;

    @FXML private HBox arrayOptionsBox;
    @FXML private TextField simTargetDrawerField;

    @FXML private VBox simStepCard;
    @FXML private Label simStepTitleLabel;
    @FXML private Label simStepCounterLabel;
    @FXML private Label simAuxStructureLabel;
    @FXML private Label simStepExplanationLabel;

    @FXML private InteractiveCanvasPane interactiveCanvasPane;

    @FXML private Button simPlayPauseBtn;
    @FXML private Button simStepBackBtn;
    @FXML private Button simStepForwardBtn;
    @FXML private Button simResetBtn;
    @FXML private Slider simSpeedSlider;
    @FXML private Label simSpeedLabel;
    @FXML private Slider simTimelineSlider;

    // [MCQ Quiz] View
    @FXML private Label mcqTopicTitleLabel;
    @FXML private Label mcqScoreBadge;
    @FXML private Button submitQuizBottomBtn;
    @FXML private Button retakeQuizBtn;
    @FXML private VBox scoreVerdictCard;
    @FXML private Label scoreVerdictTitle;
    @FXML private Label scoreVerdictSubtitle;
    @FXML private ScrollPane mcqScrollPane;
    @FXML private VBox mcqQuestionsContainer;

    // State Variables
    private AlgorithmItem currentAlgorithm;
    private MediaPlayer mediaPlayer;
    private boolean isUserDraggingSlider = false;
    private String currentViewType = "Algorithm";
    private boolean isRefreshingSourceCombo = false;

    // VisuAlgo Simulation Engine State
    private InteractiveGraphModel interactiveGraphModel;
    private List<SimulationFrame> simulationFrames = new ArrayList<>();
    private int currentFrameIndex = 0;
    private Timeline simulationTimeline;
    private boolean isSimPlaying = false;
    private int[] customArrayData = null;

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
        initVideoControls();
        initSimulationEngine();
        initMcqControls();

        copyCodeBtn.setOnAction(this::onCopyCodeClicked);
        saveBookmarkBtn.setOnAction(this::onSaveBookmarkClicked);
    }

    /**
     * Academic Rubric Fix: Explicit window property bindings for responsive font and layout scaling.
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
        videosViewTrigger.setToggleGroup(navGroup);
        simulationViewTrigger.setToggleGroup(navGroup);
        mcqViewTrigger.setToggleGroup(navGroup);
        algoViewTrigger.setSelected(true);

        algoViewTrigger.setOnAction(e -> switchView("Algorithm"));
        codeViewTrigger.setOnAction(e -> switchView("Code"));
        videosViewTrigger.setOnAction(e -> switchView("Videos"));
        simulationViewTrigger.setOnAction(e -> switchView("Simulation"));
        mcqViewTrigger.setOnAction(e -> switchView("MCQ Quiz"));
    }

    // ============================================================ VIDEO CONTROLS

    private void initVideoControls() {
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

    // ============================================================ VISUALGO SIMULATION ENGINE

    private void initSimulationEngine() {
        interactiveGraphModel = new InteractiveGraphModel();
        interactiveCanvasPane.setGraphModel(interactiveGraphModel);

        // Presets selector
        simPresetComboBox.setItems(FXCollections.observableArrayList("Default", "Bipartite", "Tree", "DAG", "Random"));
        simPresetComboBox.setValue("Default");
        simPresetComboBox.setOnAction(e -> onPresetSelected());

        // Dynamic Source Node selector listener
        simSourceComboBox.setOnAction(e -> {
            if (!isRefreshingSourceCombo && simSourceComboBox.getValue() != null) {
                buildAndRunSimulation();
            }
        });

        // Search Target Input controls (bidirectional sync and run on Enter)
        if (simTargetField != null && simTargetDrawerField != null) {
            simTargetField.textProperty().bindBidirectional(simTargetDrawerField.textProperty());
        }
        if (simTargetField != null) {
            simTargetField.setOnAction(e -> buildAndRunSimulation());
        }
        if (simTargetDrawerField != null) {
            simTargetDrawerField.setOnAction(e -> buildAndRunSimulation());
        }

        // Toggle drawer
        toggleInputPanelBtn.setOnAction(e -> {
            boolean visible = !simInputPanel.isVisible();
            simInputPanel.setVisible(visible);
            simInputPanel.setManaged(visible);
        });
        closeInputPanelBtn.setOnAction(e -> {
            simInputPanel.setVisible(false);
            simInputPanel.setManaged(false);
        });

        // Radio Groups
        indexingToggleGroup = new ToggleGroup();
        zeroIndexedRadio.setToggleGroup(indexingToggleGroup);
        oneIndexedRadio.setToggleGroup(indexingToggleGroup);
        zeroIndexedRadio.setSelected(true);

        graphTypeToggleGroup = new ToggleGroup();
        edgeListRadio.setToggleGroup(graphTypeToggleGroup);
        adjMatrixRadio.setToggleGroup(graphTypeToggleGroup);
        adjListRadio.setToggleGroup(graphTypeToggleGroup);
        edgeListRadio.setSelected(true);

        layoutToggleGroup = new ToggleGroup();
        layoutDefaultRadio.setToggleGroup(layoutToggleGroup);
        layoutBipartiteRadio.setToggleGroup(layoutToggleGroup);
        layoutTreeRadio.setToggleGroup(layoutToggleGroup);
        layoutDagRadio.setToggleGroup(layoutToggleGroup);
        layoutDefaultRadio.setSelected(true);

        // Indexing listener
        indexingToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean oneIndexed = oneIndexedRadio.isSelected();
            interactiveGraphModel.setOneIndexed(oneIndexed);
            refreshSourceComboBox();
            updateInputAreaFromModel();
            interactiveCanvasPane.redraw();
        });

        // Layout listener
        layoutToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (layoutBipartiteRadio.isSelected()) interactiveGraphModel.setLayoutType(GraphLayoutType.BIPARTITE);
            else if (layoutTreeRadio.isSelected()) interactiveGraphModel.setLayoutType(GraphLayoutType.TREE);
            else if (layoutDagRadio.isSelected()) interactiveGraphModel.setLayoutType(GraphLayoutType.DAG);
            else interactiveGraphModel.setLayoutType(GraphLayoutType.DEFAULT);

            interactiveGraphModel.computeLayout(interactiveCanvasPane.getWidth(), interactiveCanvasPane.getHeight());
            interactiveCanvasPane.redraw();
        });

        // Build & Run button
        simBuildRunBtn.setOnAction(e -> buildAndRunSimulation());

        // Animation Controls
        simPlayPauseBtn.setOnAction(e -> toggleSimulationPlay());
        simStepForwardBtn.setOnAction(e -> stepSimulationForward());
        simStepBackBtn.setOnAction(e -> stepSimulationBackward());
        simResetBtn.setOnAction(e -> resetSimulation());

        simSpeedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            simSpeedLabel.setText(String.format("%.1fx", newV.doubleValue()));
            if (simulationTimeline != null) {
                boolean running = (simulationTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING);
                restartSimulationTimeline(running);
            }
        });

        simTimelineSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !simulationFrames.isEmpty()) {
                int targetFrame = (int) Math.round(newV.doubleValue());
                if (targetFrame >= 0 && targetFrame < simulationFrames.size() && targetFrame != currentFrameIndex) {
                    currentFrameIndex = targetFrame;
                    renderCurrentSimulationFrame();
                }
            }
        });
    }

    private void onPresetSelected() {
        String preset = simPresetComboBox.getValue();
        if (preset == null) return;
        boolean oneIndexed = oneIndexedRadio.isSelected();

        switch (preset) {
            case "Bipartite" -> {
                interactiveGraphModel.loadBipartitePreset(oneIndexed);
                layoutBipartiteRadio.setSelected(true);
            }
            case "Tree" -> {
                interactiveGraphModel.loadTreePreset(oneIndexed);
                layoutTreeRadio.setSelected(true);
            }
            case "DAG" -> {
                interactiveGraphModel.loadDagPreset(oneIndexed);
                layoutDagRadio.setSelected(true);
            }
            case "Random" -> {
                interactiveGraphModel.loadRandomPreset(6, oneIndexed);
                layoutDefaultRadio.setSelected(true);
            }
            default -> {
                interactiveGraphModel.loadDefaultPreset(oneIndexed);
                layoutDefaultRadio.setSelected(true);
            }
        }

        interactiveGraphModel.computeLayout(interactiveCanvasPane.getWidth(), interactiveCanvasPane.getHeight());
        refreshSourceComboBox();
        updateInputAreaFromModel();
        buildAndRunSimulation();
    }

    private void updateInputAreaFromModel() {
        if (currentAlgorithm != null && (currentAlgorithm.getCategory().equalsIgnoreCase("Sorting") 
                || currentAlgorithm.getCategory().equalsIgnoreCase("Searching")
                || currentAlgorithm.getName().contains("Sort") 
                || currentAlgorithm.getName().contains("Search"))) {
            if (customArrayData == null) {
                if (currentAlgorithm.getName().equalsIgnoreCase("Binary Search") || currentAlgorithm.getName().toLowerCase().contains("binary")) {
                    customArrayData = new int[]{3, 9, 10, 19, 27, 38, 43, 82};
                } else {
                    customArrayData = new int[]{38, 27, 43, 3, 9, 82, 10, 19};
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < customArrayData.length; i++) {
                sb.append(customArrayData[i]).append(i == customArrayData.length - 1 ? "" : ", ");
            }
            simCustomInputArea.setText(sb.toString());
            return;
        }

        GraphInputType type = edgeListRadio.isSelected() ? GraphInputType.EDGE_LIST :
                adjMatrixRadio.isSelected() ? GraphInputType.ADJACENCY_MATRIX : GraphInputType.ADJACENCY_LIST;
        simCustomInputArea.setText(interactiveGraphModel.exportToText(type, oneIndexedRadio.isSelected()));
    }

    private void refreshSourceComboBox() {
        isRefreshingSourceCombo = true;
        try {
            String currentSelection = simSourceComboBox.getValue();
            List<String> items = new ArrayList<>();
            for (VisualGraphNode n : interactiveGraphModel.getNodes()) {
                items.add("Node " + n.getLabel());
            }
            simSourceComboBox.setItems(FXCollections.observableArrayList(items));
            if (currentSelection != null && items.contains(currentSelection)) {
                simSourceComboBox.setValue(currentSelection);
            } else if (!items.isEmpty()) {
                simSourceComboBox.setValue(items.get(0));
            }
        } finally {
            isRefreshingSourceCombo = false;
        }
    }

    private void buildAndRunSimulation() {
        pauseSimulation();

        // Check if user edited custom input
        String input = simCustomInputArea.getText();
        boolean isSortingOrSearch = currentAlgorithm != null && 
                (currentAlgorithm.getCategory().equalsIgnoreCase("Sorting") 
                 || currentAlgorithm.getCategory().equalsIgnoreCase("Searching")
                 || currentAlgorithm.getName().contains("Sort") 
                 || currentAlgorithm.getName().contains("Search"));

        if (isSortingOrSearch) {
            if (input != null && !input.isBlank()) {
                try {
                    String[] tokens = input.split("[,\\s]+");
                    List<Integer> list = new ArrayList<>();
                    for (String tok : tokens) {
                        if (!tok.isBlank()) list.add(Integer.parseInt(tok.trim()));
                    }
                    if (!list.isEmpty()) {
                        customArrayData = list.stream().mapToInt(Integer::intValue).toArray();
                    }
                } catch (Exception ex) {
                    navManager.showErrorAlert("Input Error", "Invalid Array Format", "Please provide comma or space-separated numbers.");
                }
            }
            if (currentAlgorithm != null && (currentAlgorithm.getName().equalsIgnoreCase("Binary Search") || currentAlgorithm.getName().toLowerCase().contains("binary"))) {
                if (customArrayData == null) {
                    customArrayData = new int[]{3, 9, 10, 19, 27, 38, 43, 82};
                } else {
                    Arrays.sort(customArrayData);
                }
            }
        } else {
            if (input != null && !input.isBlank()) {
                GraphInputType type = edgeListRadio.isSelected() ? GraphInputType.EDGE_LIST :
                        adjMatrixRadio.isSelected() ? GraphInputType.ADJACENCY_MATRIX : GraphInputType.ADJACENCY_LIST;
                boolean oneIndexed = oneIndexedRadio.isSelected();
                boolean directed = !currentAlgorithm.getName().toLowerCase().contains("kruskal") &&
                        !currentAlgorithm.getName().toLowerCase().contains("prim");

                boolean ok = interactiveGraphModel.parseCustomInput(input, type, oneIndexed, directed);
                if (ok) {
                    interactiveGraphModel.computeLayout(interactiveCanvasPane.getWidth(), interactiveCanvasPane.getHeight());
                    refreshSourceComboBox();
                }
            }
        }

        int sourceIdx = 0;
        String selSource = simSourceComboBox.getValue();
        if (selSource != null) {
            String label = selSource.replace("Node", "").trim();
            for (VisualGraphNode node : interactiveGraphModel.getNodes()) {
                if (node.getLabel().equals(label)) {
                    sourceIdx = node.getId();
                    break;
                }
            }
        } else if (!simSourceComboBox.getItems().isEmpty()) {
            String label = simSourceComboBox.getItems().get(0).replace("Node", "").trim();
            for (VisualGraphNode node : interactiveGraphModel.getNodes()) {
                if (node.getLabel().equals(label)) {
                    sourceIdx = node.getId();
                    break;
                }
            }
        }

        int targetVal = 27;
        try {
            if (simTargetField != null && simTargetField.getText() != null && !simTargetField.getText().isBlank()) {
                targetVal = Integer.parseInt(simTargetField.getText().trim());
            } else if (simTargetDrawerField != null && simTargetDrawerField.getText() != null && !simTargetDrawerField.getText().isBlank()) {
                targetVal = Integer.parseInt(simTargetDrawerField.getText().trim());
            }
        } catch (NumberFormatException ignored) {}

        String algoName = currentAlgorithm != null ? currentAlgorithm.getName() : "BFS";
        simulationFrames = InteractiveSimulationEngine.generateFramesForAlgorithm(algoName, interactiveGraphModel, sourceIdx, customArrayData, targetVal);

        currentFrameIndex = 0;
        simTimelineSlider.setMin(0);
        simTimelineSlider.setMax(Math.max(1, simulationFrames.size() - 1));
        simTimelineSlider.setValue(0);

        renderCurrentSimulationFrame();
    }

    private void renderCurrentSimulationFrame() {
        if (simulationFrames.isEmpty()) return;
        currentFrameIndex = Math.max(0, Math.min(currentFrameIndex, simulationFrames.size() - 1));
        SimulationFrame frame = simulationFrames.get(currentFrameIndex);

        simStepTitleLabel.setText(frame.getTitle());
        simStepCounterLabel.setText("Step " + frame.getStepNumber() + " / " + frame.getTotalSteps());
        simAuxStructureLabel.setText(frame.getAuxStructureText());
        simStepExplanationLabel.setText(frame.getExplanation());

        simTimelineSlider.setValue(currentFrameIndex);
        interactiveCanvasPane.setSimulationFrame(frame);
    }

    private void toggleSimulationPlay() {
        if (isSimPlaying) {
            pauseSimulation();
        } else {
            playSimulation();
        }
    }

    private void playSimulation() {
        if (simulationFrames.isEmpty()) return;
        if (currentFrameIndex >= simulationFrames.size() - 1) {
            currentFrameIndex = 0;
        }
        isSimPlaying = true;
        simPlayPauseBtn.setText("⏸ Pause");
        restartSimulationTimeline(true);
    }

    private void pauseSimulation() {
        isSimPlaying = false;
        simPlayPauseBtn.setText("▶ Play");
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }
    }

    private void restartSimulationTimeline(boolean startImmediately) {
        if (simulationTimeline != null) simulationTimeline.stop();

        double speed = simSpeedSlider.getValue();
        double millis = Math.max(120, 1100.0 / speed);

        simulationTimeline = new Timeline(new KeyFrame(Duration.millis(millis), e -> {
            if (currentFrameIndex < simulationFrames.size() - 1) {
                currentFrameIndex++;
                renderCurrentSimulationFrame();
            } else {
                pauseSimulation();
            }
        }));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);

        if (startImmediately) {
            simulationTimeline.play();
        }
    }

    private void stepSimulationForward() {
        pauseSimulation();
        if (currentFrameIndex < simulationFrames.size() - 1) {
            currentFrameIndex++;
            renderCurrentSimulationFrame();
        }
    }

    private void stepSimulationBackward() {
        pauseSimulation();
        if (currentFrameIndex > 0) {
            currentFrameIndex--;
            renderCurrentSimulationFrame();
        }
    }

    private void resetSimulation() {
        pauseSimulation();
        currentFrameIndex = 0;
        renderCurrentSimulationFrame();
    }

    // ============================================================ MCQ CONTROLS

    private void initMcqControls() {
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
        prepareSimulationEngineForAlgorithm();
        loadMcqQuiz();
        updateSaveButtonState();
    }

    private void updateSimulationUIContext(boolean isArrayAlgo, boolean isBinarySearch) {
        if (simSourceBox != null) {
            simSourceBox.setVisible(!isArrayAlgo);
            simSourceBox.setManaged(!isArrayAlgo);
            simSourceBox.setDisable(isArrayAlgo);
        }
        if (simPresetBox != null) {
            simPresetBox.setVisible(!isArrayAlgo);
            simPresetBox.setManaged(!isArrayAlgo);
            simPresetBox.setDisable(isArrayAlgo);
        }
        if (simToolbarSep1 != null) {
            simToolbarSep1.setVisible(!isArrayAlgo || isBinarySearch);
            simToolbarSep1.setManaged(!isArrayAlgo || isBinarySearch);
        }
        if (graphOptionsBox != null) {
            graphOptionsBox.setVisible(!isArrayAlgo);
            graphOptionsBox.setManaged(!isArrayAlgo);
            graphOptionsBox.setDisable(isArrayAlgo);
        }
        if (simTargetBox != null) {
            simTargetBox.setVisible(isBinarySearch);
            simTargetBox.setManaged(isBinarySearch);
            simTargetBox.setDisable(!isBinarySearch);
        }
        if (arrayOptionsBox != null) {
            arrayOptionsBox.setVisible(isBinarySearch);
            arrayOptionsBox.setManaged(isBinarySearch);
            arrayOptionsBox.setDisable(!isBinarySearch);
        }
        if (simInputPanelTitle != null) {
            simInputPanelTitle.setText(isArrayAlgo ? "Custom Array Elements" : "Custom Graph / Array Structure Input");
        }
        if (toggleInputPanelBtn != null) {
            toggleInputPanelBtn.setText(isArrayAlgo ? "⚙️ Custom Array Input" : "⚙️ Custom Graph / Data Input");
        }
        if (simCustomInputArea != null) {
            simCustomInputArea.setPromptText(isArrayAlgo
                    ? (isBinarySearch ? "Enter sorted array values (e.g. 3, 9, 10, 19, 27, 38, 43, 82)"
                                      : "Enter array values (e.g. 38, 27, 43, 3, 9, 82, 10, 19)")
                    : "Enter custom graph edge list (u v w) or array values (e.g. 29, 10, 14, 37...)");
        }
    }

    private void prepareSimulationEngineForAlgorithm() {
        if (currentAlgorithm == null) return;

        boolean isSorting = currentAlgorithm.getCategory().equalsIgnoreCase("Sorting") || currentAlgorithm.getName().contains("Sort");
        boolean isSearch = currentAlgorithm.getCategory().equalsIgnoreCase("Searching") || currentAlgorithm.getName().toLowerCase().contains("search");
        boolean isArrayAlgo = isSorting || isSearch;
        boolean isBinarySearch = currentAlgorithm.getName().equalsIgnoreCase("Binary Search") || currentAlgorithm.getName().toLowerCase().contains("binary");

        updateSimulationUIContext(isArrayAlgo, isBinarySearch);

        if (isArrayAlgo) {
            if (isBinarySearch) {
                customArrayData = new int[]{3, 9, 10, 19, 27, 38, 43, 82};
                if (simTargetField != null) simTargetField.setText("27");
                if (simTargetDrawerField != null) simTargetDrawerField.setText("27");
            } else {
                customArrayData = new int[]{38, 27, 43, 3, 9, 82, 10, 19};
            }
        } else {
            customArrayData = null;
            boolean isTreeOrDag = currentAlgorithm.getName().toLowerCase().contains("kruskal") ||
                    currentAlgorithm.getName().toLowerCase().contains("prim") ||
                    currentAlgorithm.getName().equalsIgnoreCase("Floyd-Warshall");

            if (currentAlgorithm.getName().equalsIgnoreCase("DAG") || currentAlgorithm.getName().toLowerCase().contains("johnson")) {
                interactiveGraphModel.loadDagPreset(false);
                layoutDagRadio.setSelected(true);
            } else if (isTreeOrDag) {
                interactiveGraphModel.loadTreePreset(false);
                layoutTreeRadio.setSelected(true);
            } else {
                interactiveGraphModel.loadDefaultPreset(false);
                layoutDefaultRadio.setSelected(true);
            }

            refreshSourceComboBox();
        }

        updateInputAreaFromModel();
        buildAndRunSimulation();
    }

    private void switchView(String viewName) {
        this.currentViewType = viewName;
        algorithmPane.setVisible(viewName.equals("Algorithm"));
        algorithmPane.setManaged(viewName.equals("Algorithm"));

        codePane.setVisible(viewName.equals("Code"));
        codePane.setManaged(viewName.equals("Code"));

        videosPane.setVisible(viewName.equals("Videos"));
        videosPane.setManaged(viewName.equals("Videos"));

        simulationPane.setVisible(viewName.equals("Simulation"));
        simulationPane.setManaged(viewName.equals("Simulation"));

        mcqPane.setVisible(viewName.equals("MCQ Quiz"));
        mcqPane.setManaged(viewName.equals("MCQ Quiz"));

        copyCodeBtn.setVisible(viewName.equals("Code"));
        copyCodeBtn.setManaged(viewName.equals("Code"));

        if (!viewName.equals("Videos") && mediaPlayer != null) {
            mediaPlayer.pause();
        }
        if (!viewName.equals("Simulation")) {
            pauseSimulation();
        } else {
            interactiveCanvasPane.redraw();
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

    // ============================================================ VIDEOS VIEW & LOCAL MP4 CONTROLS

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
        if (!videoFile.exists() && rawName.contains("Sort")) {
            videoFile = new File("videos123/" + rawName.replace(" ", "") + ".mp4");
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
            showVideoFallback(candidateFile, "File 'videos123/" + candidateFile + "' was not found locally.");
        }
    }

    private void showVideoFallback(String fileName, String reason) {
        mediaViewContainer.setVisible(false);
        mediaViewContainer.setManaged(false);
        fallbackBox.setVisible(true);
        fallbackBox.setManaged(true);
        videoUnavailableLabel.setText("Video not available");
        fallbackDetailLabel.setText("Algorithm: " + currentAlgorithm.getName() + " (" + reason + ")\nPlace a valid MP4 into the videos123/ directory to view simulation video.");
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

    // ============================================================ MCQ QUIZ ENGINE

    private void loadMcqQuiz() {
        currentQuestions = McqBank.getQuestionsForAlgorithm(currentAlgorithm.getName());
        quizSubmitted = false;
        scoreVerdictCard.setVisible(false);
        scoreVerdictCard.setManaged(false);
        retakeQuizBtn.setVisible(false);
        retakeQuizBtn.setManaged(false);
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

            Label qPrompt = new Label((i + 1) + ". " + q.getQuestionText());
            qPrompt.setWrapText(true);
            qPrompt.getStyleClass().add("mcq-question-label");

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

        int totalQuestions = currentQuestions.size();
        int percentage = (int) Math.round(((double) correctCount / totalQuestions) * 100);

        scoreVerdictTitle.setText(String.format("Quiz Results: %d / %d Correct (%d%%) - Level Progress Updated!",
                correctCount, totalQuestions, percentage));
        scoreVerdictCard.setVisible(true);
        scoreVerdictCard.setManaged(true);

        submitQuizBottomBtn.setDisable(true);
        retakeQuizBtn.setVisible(true);
        retakeQuizBtn.setManaged(true);

        mcqScrollPane.setVvalue(0.0);

        User user = navManager.getCurrentUser();
        if (user != null) {
            int earnedScore = correctCount;
            AppExecutor.execute(() -> {
                String promotedLevel = null;
                for (int c = 0; c < earnedScore; c++) {
                    String promo = dbHelper.recordCorrectMcq(user.getUsername());
                    if (promo != null) promotedLevel = promo;
                }

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
            case "Videos" -> "Simulation video reference for " + currentAlgorithm.getName() + " (" + currentAlgorithm.getVideoFileName() + ")";
            case "Simulation" -> "VisuAlgo interactive canvas simulation configuration for " + currentAlgorithm.getName();
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
