package com.codecanvas.model;

import javafx.beans.property.*;

public class SavedItem {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty username = new SimpleStringProperty(this, "username");
    private final StringProperty type = new SimpleStringProperty(this, "type");
    private final StringProperty algorithmName = new SimpleStringProperty(this, "algorithmName");
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty content = new SimpleStringProperty(this, "content");
    private final StringProperty savedAt = new SimpleStringProperty(this, "savedAt");

    public SavedItem() {
    }

    public SavedItem(int id, String username, String type, String algorithmName, String title, String content, String savedAt) {
        setId(id);
        setUsername(username);
        setType(type);
        setAlgorithmName(algorithmName);
        setTitle(title);
        setContent(content);
        setSavedAt(savedAt);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }

    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public StringProperty typeProperty() { return type; }

    public String getAlgorithmName() { return algorithmName.get(); }
    public void setAlgorithmName(String value) { algorithmName.set(value); }
    public StringProperty algorithmNameProperty() { return algorithmName; }

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getContent() { return content.get(); }
    public void setContent(String value) { content.set(value); }
    public StringProperty contentProperty() { return content; }

    public String getSavedAt() { return savedAt.get(); }
    public void setSavedAt(String value) { savedAt.set(value); }
    public StringProperty savedAtProperty() { return savedAt; }

    @Override
    public String toString() {
        return "[" + getType() + "] " + getAlgorithmName() + " - " + getTitle();
    }
}
