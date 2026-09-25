package com.codecanvas.db;

import com.codecanvas.model.ExecutionTrace;
import com.codecanvas.model.Person;
import com.codecanvas.model.SavedItem;
import com.codecanvas.model.User;
import com.codecanvas.util.SecurityUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite JDBC helper for CodeCanvas. Manages tables:
 * - users: user accounts with hashed passwords and profile data
 * - saved_items: bookmarked algorithms, code snippets, and simulation references
 * - person: CS students / course persons for TableView integration
 * - execution_trace: algorithm trace logging and API sync
 */
public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:codecanvas.db";
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on classpath", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes all SQLite tables if they do not already exist, and seeds initial data.
     */
    public void initSchema() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                full_name TEXT,
                email TEXT,
                skill_level TEXT,
                country TEXT,
                dob TEXT,
                hobbies TEXT,
                profile_image_path TEXT,
                theme TEXT
            );
            """;

        String createSavedItems = """
            CREATE TABLE IF NOT EXISTS saved_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                type TEXT NOT NULL,
                algorithm_name TEXT NOT NULL,
                title TEXT,
                content TEXT,
                saved_at TEXT
            );
            """;

        String createPerson = """
            CREATE TABLE IF NOT EXISTS person (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                level TEXT NOT NULL,
                dob TEXT
            );
            """;

        String createTrace = """
            CREATE TABLE IF NOT EXISTS execution_trace (
                trace_id INTEGER PRIMARY KEY AUTOINCREMENT,
                algorithm_name TEXT NOT NULL,
                comparisons INTEGER NOT NULL,
                status TEXT NOT NULL
            );
            """;

        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute(createUsers);
            st.execute(createSavedItems);
            st.execute(createPerson);
            st.execute(createTrace);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }

        seedDefaultsIfEmpty();
    }

    private void seedDefaultsIfEmpty() {
        // Seed default demo user if empty
        if (getUser("student") == null) {
            User demo = new User("student", "", "", "Alex Johnson", "alex.johnson@university.edu");
            demo.setSkillLevel("Intermediate");
            demo.setCountry("United States");
            demo.setDob(LocalDate.of(2003, 5, 14));
            demo.setHobbies("Reading Gaming");
            demo.setTheme("Blue");
            registerUser(demo, "Student123!");
        }

        // Seed sample persons for TableView if empty
        if (findAllPersons().isEmpty()) {
            insertPerson(new Person(0, "Alice Smith", "Expert", LocalDate.of(2001, 3, 12)));
            insertPerson(new Person(0, "Bob Jones", "Beginner", LocalDate.of(2004, 7, 23)));
            insertPerson(new Person(0, "Carol White", "Intermediate", LocalDate.of(2002, 11, 5)));
            insertPerson(new Person(0, "David Brown", "Expert", LocalDate.of(2000, 9, 30)));
        }

        // Seed sample saved item for demo user if empty
        if (findSavedItems("student", null).isEmpty()) {
            insertSavedItem(new SavedItem(0, "student", "Algorithm", "BFS", "Breadth-First Search Overview",
                    "BFS explores graph level by level using a Queue with O(V + E) complexity.",
                    LocalDateTime.now().format(TS_FORMAT)));
            insertSavedItem(new SavedItem(0, "student", "Code", "Dijkstra", "Dijkstra Java Implementation",
                    "PriorityQueue-based shortest path finder on weighted graphs.",
                    LocalDateTime.now().format(TS_FORMAT)));
        }
    }

    // ============================================================ USER AUTH & PROFILE

    public boolean registerUser(User user, String plainPassword) {
        String checkSql = "SELECT username FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection c = connect(); PreparedStatement checkPs = c.prepareStatement(checkSql)) {
            checkPs.setString(1, user.getUsername());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return false; // username already exists
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username availability", e);
        }

        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPassword(plainPassword, salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);

        String sql = """
            INSERT INTO users (username, password_hash, salt, full_name, email, 
                               skill_level, country, dob, hobbies, profile_image_path, theme)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getSkillLevel());
            ps.setString(7, user.getCountry());
            ps.setString(8, user.getDob() != null ? user.getDob().toString() : null);
            ps.setString(9, user.getHobbies());
            ps.setString(10, user.getProfileImagePath());
            ps.setString(11, user.getTheme() != null ? user.getTheme() : "Blue");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public User authenticateUser(String username, String plainPassword) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String salt = rs.getString("salt");
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtil.verifyPassword(plainPassword, salt, storedHash)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate user", e);
        }
        return null;
    }

    public User getUser(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user: " + username, e);
        }
        return null;
    }

    public boolean updateUserProfile(User user) {
        String sql = """
            UPDATE users SET full_name = ?, email = ?, skill_level = ?, 
                             country = ?, dob = ?, hobbies = ?, 
                             profile_image_path = ?, theme = ? 
            WHERE LOWER(username) = LOWER(?)
            """;
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getSkillLevel());
            ps.setString(4, user.getCountry());
            ps.setString(5, user.getDob() != null ? user.getDob().toString() : null);
            ps.setString(6, user.getHobbies());
            ps.setString(7, user.getProfileImagePath());
            ps.setString(8, user.getTheme());
            ps.setString(9, user.getUsername());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setSkillLevel(rs.getString("skill_level"));
        u.setCountry(rs.getString("country"));
        String dobStr = rs.getString("dob");
        if (dobStr != null && !dobStr.isBlank()) {
            u.setDob(LocalDate.parse(dobStr));
        }
        u.setHobbies(rs.getString("hobbies"));
        u.setProfileImagePath(rs.getString("profile_image_path"));
        u.setTheme(rs.getString("theme"));
        return u;
    }

    // ============================================================ SAVED ITEMS CRUD

    public int insertSavedItem(SavedItem item) {
        String sql = "INSERT INTO saved_items (username, type, algorithm_name, title, content, saved_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getUsername());
            ps.setString(2, item.getType());
            ps.setString(3, item.getAlgorithmName());
            ps.setString(4, item.getTitle());
            ps.setString(5, item.getContent());
            ps.setString(6, item.getSavedAt() != null ? item.getSavedAt() : LocalDateTime.now().format(TS_FORMAT));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    item.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert saved item", e);
        }
        return -1;
    }

    public List<SavedItem> findSavedItems(String username, String typeFilter) {
        List<SavedItem> list = new ArrayList<>();
        String sql;
        boolean hasFilter = (typeFilter != null && !typeFilter.isBlank() && !typeFilter.equalsIgnoreCase("ALL"));
        if (hasFilter) {
            sql = "SELECT id, username, type, algorithm_name, title, content, saved_at FROM saved_items WHERE LOWER(username) = LOWER(?) AND LOWER(type) = LOWER(?) ORDER BY id DESC";
        } else {
            sql = "SELECT id, username, type, algorithm_name, title, content, saved_at FROM saved_items WHERE LOWER(username) = LOWER(?) ORDER BY id DESC";
        }

        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            if (hasFilter) {
                ps.setString(2, typeFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SavedItem(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("type"),
                            rs.getString("algorithm_name"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("saved_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load saved items", e);
        }
        return list;
    }

    public boolean isSaved(String username, String algorithmName, String type) {
        String sql = "SELECT id FROM saved_items WHERE LOWER(username) = LOWER(?) AND LOWER(algorithm_name) = LOWER(?) AND LOWER(type) = LOWER(?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, algorithmName);
            ps.setString(3, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteSavedItem(int id) {
        String sql = "DELETE FROM saved_items WHERE id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete saved item id=" + id, e);
        }
    }

    // ============================================================ PERSON CRUD

    public int insertPerson(Person p) {
        String sql = "INSERT INTO person(name, level, dob) VALUES (?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getLevel());
            ps.setString(3, p.getDob() == null ? null : p.getDob().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    p.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert person", e);
        }
        return -1;
    }

    public boolean updatePerson(Person p) {
        String sql = "UPDATE person SET name = ?, level = ?, dob = ? WHERE id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getLevel());
            ps.setString(3, p.getDob() == null ? null : p.getDob().toString());
            ps.setInt(4, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update person id=" + p.getId(), e);
        }
    }

    public boolean deletePerson(int id) {
        String sql = "DELETE FROM person WHERE id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete person id=" + id, e);
        }
    }

    public List<Person> findAllPersons() {
        List<Person> result = new ArrayList<>();
        String sql = "SELECT id, name, level, dob FROM person ORDER BY id";
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String dobStr = rs.getString("dob");
                LocalDate dob = dobStr == null ? null : LocalDate.parse(dobStr);
                result.add(new Person(rs.getInt("id"), rs.getString("name"), rs.getString("level"), dob));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load persons", e);
        }
        return result;
    }

    // ============================================================ EXECUTION TRACE CRUD

    public int insertTrace(ExecutionTrace t) {
        String sql = "INSERT INTO execution_trace(algorithm_name, comparisons, status) VALUES (?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getAlgorithmName());
            ps.setInt(2, t.getComparisons());
            ps.setString(3, t.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    t.setTraceId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert execution trace", e);
        }
        return -1;
    }

    public List<ExecutionTrace> findAllTraces() {
        List<ExecutionTrace> result = new ArrayList<>();
        String sql = "SELECT trace_id, algorithm_name, comparisons, status FROM execution_trace ORDER BY trace_id";
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new ExecutionTrace(
                        rs.getInt("trace_id"),
                        rs.getString("algorithm_name"),
                        rs.getInt("comparisons"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load execution traces", e);
        }
        return result;
    }

    public boolean deleteTrace(int traceId) {
        String sql = "DELETE FROM execution_trace WHERE trace_id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, traceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete trace id=" + traceId, e);
        }
    }
}
