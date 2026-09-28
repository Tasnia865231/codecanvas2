package com.codecanvas.db;

import com.codecanvas.model.User;
import com.codecanvas.util.SecurityUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete DAO implementation for User entities using SQLite persistence.
 * Implements Generic Dao<User> satisfying academic OOP rubric.
 */
public class UserDao implements Dao<User> {

    private final DBHelper dbHelper;

    public UserDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Optional<User> findById(Object id) {
        if (id == null) return Optional.empty();
        return findById(id.toString());
    }

    public Optional<User> findById(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDao.findById exception: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY total_correct_mcqs DESC, username ASC";
        try (Connection c = dbHelper.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("UserDao.findAll exception: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean insert(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }

        // Check for duplicate username
        String checkSql = "SELECT username FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection c = dbHelper.getConnection(); PreparedStatement checkPs = c.prepareStatement(checkSql)) {
            checkPs.setString(1, user.getUsername().trim());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    System.err.println("Registration failed: username '" + user.getUsername() + "' already exists.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Duplicate check error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        String sql = """
            INSERT INTO users (username, password_hash, salt, full_name, email, 
                               role, user_level, total_correct_mcqs, skill_level, country, dob, hobbies, profile_image_path, theme)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole() != null ? user.getRole() : "STUDENT");
            ps.setString(7, user.getUserLevel() != null ? user.getUserLevel() : "Beginner");
            ps.setInt(8, user.getTotalCorrectMcqs());
            ps.setString(9, user.getSkillLevel());
            ps.setString(10, user.getCountry());
            ps.setString(11, user.getDob() != null ? user.getDob().toString() : null);
            ps.setString(12, user.getHobbies());
            ps.setString(13, user.getProfileImagePath());
            ps.setString(14, user.getTheme() != null ? user.getTheme() : "Blue");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during user insert: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getUsername() == null) return false;
        String sql = """
            UPDATE users SET full_name = ?, email = ?, role = ?, user_level = ?, total_correct_mcqs = ?,
                             skill_level = ?, country = ?, dob = ?, hobbies = ?, 
                             profile_image_path = ?, theme = ? 
            WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))
            """;
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getUserLevel());
            ps.setInt(5, user.getTotalCorrectMcqs());
            ps.setString(6, user.getSkillLevel());
            ps.setString(7, user.getCountry());
            ps.setString(8, user.getDob() != null ? user.getDob().toString() : null);
            ps.setString(9, user.getHobbies());
            ps.setString(10, user.getProfileImagePath());
            ps.setString(11, user.getTheme());
            ps.setString(12, user.getUsername().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDao.update exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMcqScore(String username, int newTotalCorrect, String newLevel) {
        if (username == null) return false;
        String sql = "UPDATE users SET total_correct_mcqs = ?, user_level = ? WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, newTotalCorrect);
            ps.setString(2, newLevel);
            ps.setString(3, username.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDao.updateMcqScore exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Object id) {
        if (id == null) return false;
        return delete(id.toString());
    }

    public boolean delete(String username) {
        if (username == null) return false;
        String sql = "DELETE FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDao.delete exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Optional<User> authenticate(String username, String plainPassword) {
        if (username == null || plainPassword == null) return Optional.empty();
        String cleanUser = username.trim();
        String cleanPass = plainPassword.trim();
        if (cleanUser.isEmpty() || cleanPass.isEmpty()) return Optional.empty();

        Optional<User> opt = findById(cleanUser);
        if (opt.isPresent()) {
            User u = opt.get();
            if (SecurityUtil.verifyPassword(cleanPass, u.getSalt(), u.getPasswordHash())) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUsername(getStringSafe(rs, "username"));
        u.setPasswordHash(getStringSafe(rs, "password_hash"));
        u.setSalt(getStringSafe(rs, "salt"));
        u.setFullName(getStringSafe(rs, "full_name"));
        u.setEmail(getStringSafe(rs, "email"));

        String role = getStringSafe(rs, "role");
        u.setRole(role != null && !role.isBlank() ? role : "STUDENT");

        String level = getStringSafe(rs, "user_level");
        u.setUserLevel(level != null && !level.isBlank() ? level : "Beginner");

        u.setTotalCorrectMcqs(getIntSafe(rs, "total_correct_mcqs"));
        u.setSkillLevel(getStringSafe(rs, "skill_level"));
        u.setCountry(getStringSafe(rs, "country"));

        String dobStr = getStringSafe(rs, "dob");
        if (dobStr != null && !dobStr.isBlank()) {
            try {
                u.setDob(LocalDate.parse(dobStr));
            } catch (Exception ignored) {}
        }
        u.setHobbies(getStringSafe(rs, "hobbies"));
        u.setProfileImagePath(getStringSafe(rs, "profile_image_path"));
        u.setTheme(getStringSafe(rs, "theme"));
        return u;
    }

    private String getStringSafe(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private int getIntSafe(ResultSet rs, String column) {
        try {
            return rs.getInt(column);
        } catch (SQLException e) {
            return 0;
        }
    }
}
