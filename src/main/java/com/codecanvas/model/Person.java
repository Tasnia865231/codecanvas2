package com.codecanvas.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Domain model backing the TableView, the SQLite "person" table,
 * and objects mapped from the JSON API.
 * Contains Foreign Key reference to users(username).
 */
public class Person {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty username = new SimpleStringProperty(this, "username", "student");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty level = new SimpleStringProperty(this, "level");
    private final ObjectProperty<LocalDate> dob = new SimpleObjectProperty<>(this, "dob");

    public Person() {
    }

    public Person(int id, String name, String level, LocalDate dob) {
        this(id, "student", name, level, dob);
    }

    public Person(int id, String username, String name, String level, LocalDate dob) {
        setId(id);
        setUsername(username != null && !username.isBlank() ? username : "student");
        setName(name);
        setLevel(level);
        setDob(dob);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getLevel() { return level.get(); }
    public void setLevel(String value) { level.set(value); }
    public StringProperty levelProperty() { return level; }

    public LocalDate getDob() { return dob.get(); }
    public void setDob(LocalDate value) { dob.set(value); }
    public ObjectProperty<LocalDate> dobProperty() { return dob; }

    @Override
    public String toString() {
        return "Person{id=" + getId() + ", username='" + getUsername() + "', name='" + getName() + "', level='" + getLevel() + "', dob=" + getDob() + "}";
    }
}
