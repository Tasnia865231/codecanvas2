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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SQLite JDBC helper with automatic schema migration, Foreign Key constraints,
 * and guaranteed credential seeding for Admin123_ and student accounts.
 */
public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:codecanvas.db";
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private UserDao userDao;
    private PersonDao personDao;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on classpath", e);
        }
    }

    public DBHelper() {
        this.userDao = new UserDao(this);
        this.personDao = new PersonDao(this);
    }

    public Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(DB_URL);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return c;
    }

    public UserDao getUserDao() {
        if (userDao == null) userDao = new UserDao(this);
        return userDao;
    }

    public PersonDao getPersonDao() {
        if (personDao == null) personDao = new PersonDao(this);
        return personDao;
    }

    /**
     * Initializes all SQLite tables, performs auto-migration for missing columns,
     * and guarantees default accounts are seeded and synchronized.
     */
    public void initSchema() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                full_name TEXT,
                email TEXT,
                role TEXT DEFAULT 'STUDENT',
                user_level TEXT DEFAULT 'Beginner',
                total_correct_mcqs INTEGER DEFAULT 0,
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
                saved_at TEXT,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            );
            """;

        String createPerson = """
            CREATE TABLE IF NOT EXISTS person (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL DEFAULT 'student',
                name TEXT NOT NULL,
                level TEXT NOT NULL,
                dob TEXT,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
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

        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute(createUsers);
            st.execute(createSavedItems);
            st.execute(createPerson);
            st.execute(createTrace);

            // Run automatic column migrations on users, saved_items, and person
            migrateUsersTable(c);
            migrateSavedItemsTable(c);
            migratePersonTable(c);

        } catch (SQLException e) {
            System.err.println("Database schema initialization failed: " + e.getMessage());
            e.printStackTrace();
        }

        seedAccountsAndData();
    }

    /**
     * Inspects existing columns in 'users' and adds any that were introduced in newer versions.
     */
    private void migrateUsersTable(Connection c) {
        Set<String> columns = new HashSet<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(users)")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addColumnIfMissing(c, "users", columns, "role", "TEXT DEFAULT 'STUDENT'");
        addColumnIfMissing(c, "users", columns, "user_level", "TEXT DEFAULT 'Beginner'");
        addColumnIfMissing(c, "users", columns, "total_correct_mcqs", "INTEGER DEFAULT 0");
        addColumnIfMissing(c, "users", columns, "full_name", "TEXT");
        addColumnIfMissing(c, "users", columns, "email", "TEXT");
        addColumnIfMissing(c, "users", columns, "skill_level", "TEXT DEFAULT 'Beginner'");
        addColumnIfMissing(c, "users", columns, "country", "TEXT");
        addColumnIfMissing(c, "users", columns, "dob", "TEXT");
        addColumnIfMissing(c, "users", columns, "hobbies", "TEXT");
        addColumnIfMissing(c, "users", columns, "profile_image_path", "TEXT");
        addColumnIfMissing(c, "users", columns, "theme", "TEXT DEFAULT 'Blue'");
    }

    private void migrateSavedItemsTable(Connection c) {
        Set<String> columns = new HashSet<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(saved_items)")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addColumnIfMissing(c, "saved_items", columns, "username", "TEXT NOT NULL DEFAULT 'student'");
        addColumnIfMissing(c, "saved_items", columns, "type", "TEXT NOT NULL DEFAULT 'Algorithm'");
        addColumnIfMissing(c, "saved_items", columns, "algorithm_name", "TEXT NOT NULL DEFAULT ''");
        addColumnIfMissing(c, "saved_items", columns, "title", "TEXT");
        addColumnIfMissing(c, "saved_items", columns, "content", "TEXT");
        addColumnIfMissing(c, "saved_items", columns, "saved_at", "TEXT");
    }

    private void migratePersonTable(Connection c) {
        Set<String> columns = new HashSet<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(person)")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addColumnIfMissing(c, "person", columns, "username", "TEXT NOT NULL DEFAULT 'student' REFERENCES users(username) ON DELETE CASCADE");
    }

    private void addColumnIfMissing(Connection c, String tableName, Set<String> existingColumns, String colName, String colDef) {
        if (!existingColumns.contains(colName.toLowerCase())) {
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE " + tableName + " ADD COLUMN " + colName + " " + colDef);
                System.out.println("Migrated " + tableName + ": added column " + colName);
            } catch (SQLException e) {
                System.err.println("Failed to add column " + colName + " to " + tableName + ": " + e.getMessage());
            }
        }
    }

    private void seedAccountsAndData() {
        // Guarantee Admin123_ credentials ALWAYS work
        ensureAccount("Admin123_", "Admin123_", "System Administrator", "admin@codecanvas.edu", "ADMIN", "Expert", 250);

        // Guarantee student credentials ALWAYS work
        ensureAccount("student", "Student123!", "Tasnia Rahman", "tasnia.rahman@university.edu", "STUDENT", "Intermediate", 35);

        // Guarantee peer student directory accounts
        ensureAccount("s_arman", "Student123!", "Arman Hossain", "arman.h@university.edu", "STUDENT", "Beginner", 14);
        ensureAccount("s_farhana", "Student123!", "Farhana Yasmin", "farhana.y@university.edu", "STUDENT", "Expert", 120);
        ensureAccount("s_tanvir", "Student123!", "Tanvir Ahmed", "tanvir.a@university.edu", "STUDENT", "Intermediate", 58);

        // Sample persons for Course TableView
        if (findAllPersons().isEmpty()) {
            insertPerson(new Person(0, "Alice Smith", "Expert", LocalDate.of(2001, 3, 12)));
            insertPerson(new Person(0, "Bob Jones", "Beginner", LocalDate.of(2004, 7, 23)));
            insertPerson(new Person(0, "Carol White", "Intermediate", LocalDate.of(2002, 11, 5)));
            insertPerson(new Person(0, "David Brown", "Expert", LocalDate.of(2000, 9, 30)));
        }

        // Seed default saved items
        if (findSavedItems("student", null).isEmpty()) {
            insertSavedItem(new SavedItem(0, "student", "Algorithm", "BFS", "Breadth-First Search Overview",
                    "BFS explores graph level by level using a Queue with O(V + E) complexity.",
                    LocalDateTime.now().format(TS_FORMAT)));
            insertSavedItem(new SavedItem(0, "student", "Code", "Dijkstra", "Dijkstra Java Implementation",
                    "PriorityQueue-based shortest path finder on weighted graphs.",
                    LocalDateTime.now().format(TS_FORMAT)));
        }
    }

    /**
     * Inserts the account if missing, or refreshes password hash to guarantee credentials always work.
     */
    public void ensureAccount(String username, String plainPassword, String fullName, String email, String role, String level, int mcqs) {
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPassword(plainPassword, salt);

        User existing = getUser(username);
        if (existing == null) {
            User u = new User(username, hash, salt, fullName, email, role);
            u.setUserLevel(level);
            u.setTotalCorrectMcqs(mcqs);
            u.setSkillLevel(level);
            u.setCountry("United States");
            u.setTheme("Blue");
            getUserDao().insert(u);
        } else {
            // Update password hash, salt, and role to ensure the hardcoded credentials work
            String updateSql = "UPDATE users SET password_hash = ?, salt = ?, role = ?, user_level = ?, full_name = ? WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(updateSql)) {
                ps.setString(1, hash);
                ps.setString(2, salt);
                ps.setString(3, role);
                ps.setString(4, level);
                ps.setString(5, fullName);
                ps.setString(6, username.trim());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ============================================================ USER REGISTRATION & AUTH

    public boolean registerUser(User user, String plainPassword) {
        if (user == null || plainPassword == null || plainPassword.trim().isEmpty()) {
            return false;
        }
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPassword(plainPassword.trim(), salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        return getUserDao().insert(user);
    }

    public User authenticateUser(String username, String plainPassword) {
        return getUserDao().authenticate(username, plainPassword).orElse(null);
    }

    public User getUser(String username) {
        return getUserDao().findById(username).orElse(null);
    }

    public List<User> getAllUsers() {
        return getUserDao().findAll();
    }

    public boolean updateUserProfile(User user) {
        return getUserDao().update(user);
    }

    public String recordCorrectMcq(String username) {
        User u = getUser(username);
        if (u == null) return null;

        int newTotal = u.getTotalCorrectMcqs() + 1;
        String oldLevel = u.getUserLevel() != null ? u.getUserLevel() : "Beginner";
        String newLevel = calculateLevel(newTotal);

        u.setTotalCorrectMcqs(newTotal);
        u.setUserLevel(newLevel);
        getUserDao().updateMcqScore(username, newTotal, newLevel);

        if (!newLevel.equalsIgnoreCase(oldLevel)) {
            return newLevel;
        }
        return null;
    }

    public static String calculateLevel(int correctCount) {
        if (correctCount >= 100) return "Expert";
        if (correctCount >= 20) return "Intermediate";
        return "Beginner";
    }

    // ============================================================ SAVED ITEMS CRUD

    public int insertSavedItem(SavedItem item) {
        String sql = "INSERT INTO saved_items (username, type, algorithm_name, title, content, saved_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            e.printStackTrace();
        }
        return -1;
    }

    public List<SavedItem> findSavedItems(String username, String typeFilter) {
        List<SavedItem> list = new ArrayList<>();
        String sql;
        boolean hasFilter = (typeFilter != null && !typeFilter.isBlank() && !typeFilter.equalsIgnoreCase("ALL"));
        if (hasFilter) {
            sql = "SELECT id, username, type, algorithm_name, title, content, saved_at FROM saved_items WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) AND LOWER(TRIM(type)) = LOWER(TRIM(?)) ORDER BY id DESC";
        } else {
            sql = "SELECT id, username, type, algorithm_name, title, content, saved_at FROM saved_items WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) ORDER BY id DESC";
        }

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            if (hasFilter) {
                ps.setString(2, typeFilter.trim());
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
            e.printStackTrace();
        }
        return list;
    }

    public int countSavedItems(String username) {
        String sql = "SELECT COUNT(*) FROM saved_items WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isSaved(String username, String algorithmName, String type) {
        String sql = "SELECT id FROM saved_items WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) AND LOWER(TRIM(algorithm_name)) = LOWER(TRIM(?)) AND LOWER(TRIM(type)) = LOWER(TRIM(?))";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, algorithmName.trim());
            ps.setString(3, type.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteSavedItem(int id) {
        String sql = "DELETE FROM saved_items WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================ PERSON DELEGATIONS (DAO)

    public int insertPerson(Person p) {
        boolean ok = getPersonDao().insert(p);
        return ok ? p.getId() : -1;
    }

    public boolean updatePerson(Person p) {
        return getPersonDao().update(p);
    }

    public boolean deletePerson(int id) {
        return getPersonDao().delete(id);
    }

    public List<Person> findAllPersons() {
        return getPersonDao().findAll();
    }

    // ============================================================ TRACE CRUD

    public int insertTrace(ExecutionTrace t) {
        String sql = "INSERT INTO execution_trace(algorithm_name, comparisons, status) VALUES (?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            e.printStackTrace();
        }
        return -1;
    }

    public List<ExecutionTrace> findAllTraces() {
        List<ExecutionTrace> result = new ArrayList<>();
        String sql = "SELECT trace_id, algorithm_name, comparisons, status FROM execution_trace ORDER BY trace_id";
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new ExecutionTrace(
                        rs.getInt("trace_id"),
                        rs.getString("algorithm_name"),
                        rs.getInt("comparisons"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
