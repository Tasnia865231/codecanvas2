package com.codecanvas.model;

import java.util.List;

public class McqQuestion {
    private int id;
    private String algorithmName;
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;

    public McqQuestion(int id, String algorithmName, String questionText, List<String> options, int correctOptionIndex, String explanation) {
        this.id = id;
        this.algorithmName = algorithmName;
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public String getAlgorithmName() { return algorithmName; }
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public String getExplanation() { return explanation; }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctOptionIndex;
    }
}
