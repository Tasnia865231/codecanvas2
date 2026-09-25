package com.codecanvas.model;

import java.time.LocalDate;

public class User {
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private String skillLevel;
    private String country;
    private LocalDate dob;
    private String hobbies;
    private String profileImagePath;
    private String theme;

    public User() {
        this.skillLevel = "Beginner";
        this.theme = "Blue";
    }

    public User(String username, String passwordHash, String salt, String fullName, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.skillLevel = "Beginner";
        this.theme = "Blue";
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }

    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    @Override
    public String toString() {
        return "User{" + "username='" + username + '\'' + ", fullName='" + fullName + '\'' + '}';
    }
}
