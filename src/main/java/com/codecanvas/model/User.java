package com.codecanvas.model;

import java.time.LocalDate;

public class User {
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private String role; // "ADMIN" or "STUDENT"
    private String userLevel; // "Beginner", "Intermediate", "Expert"
    private int totalCorrectMcqs;
    private String skillLevel;
    private String country;
    private LocalDate dob;
    private String hobbies;
    private String profileImagePath;
    private String theme;

    public User() {
        this.role = "STUDENT";
        this.userLevel = "Beginner";
        this.totalCorrectMcqs = 0;
        this.skillLevel = "Beginner";
        this.theme = "Blue";
    }

    public User(String username, String passwordHash, String salt, String fullName, String email, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role : "STUDENT";
        this.userLevel = "Beginner";
        this.totalCorrectMcqs = 0;
        this.skillLevel = "Beginner";
        this.theme = "Blue";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || "Admin123_".equalsIgnoreCase(username);
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUserLevel() { return userLevel; }
    public void setUserLevel(String userLevel) { this.userLevel = userLevel; }

    public int getTotalCorrectMcqs() { return totalCorrectMcqs; }
    public void setTotalCorrectMcqs(int totalCorrectMcqs) { this.totalCorrectMcqs = totalCorrectMcqs; }

    public int getOverallScore() { return totalCorrectMcqs * 10; }

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
        return (fullName != null && !fullName.isBlank() ? fullName : username) + " [" + userLevel + "]";
    }
}
