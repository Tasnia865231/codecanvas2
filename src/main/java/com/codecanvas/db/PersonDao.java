package com.codecanvas.db;

import com.codecanvas.model.Person;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete DAO implementation for Person entities (Course Student Registry).
 * Implements Generic Dao<Person> satisfying academic OOP rubric.
 * Includes foreign key reference to users(username).
 */
public class PersonDao implements Dao<Person> {

    private final DBHelper dbHelper;

    public PersonDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Optional<Person> findById(Object id) {
        if (id == null) return Optional.empty();
        if (id instanceof Integer) return findById((Integer) id);
        try {
            return findById(Integer.parseInt(id.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Person> findById(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, username, name, level, dob FROM person WHERE id = ?";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dobStr = rs.getString("dob");
                    LocalDate dob = dobStr == null ? null : LocalDate.parse(dobStr);
                    String uName = rs.getString("username");
                    return Optional.of(new Person(rs.getInt("id"), uName != null ? uName : "student", rs.getString("name"), rs.getString("level"), dob));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Person> findAll() {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT id, username, name, level, dob FROM person ORDER BY id ASC";
        try (Connection c = dbHelper.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String dobStr = rs.getString("dob");
                LocalDate dob = dobStr == null ? null : LocalDate.parse(dobStr);
                String uName = rs.getString("username");
                list.add(new Person(rs.getInt("id"), uName != null ? uName : "student", rs.getString("name"), rs.getString("level"), dob));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Person p) {
        String sql = "INSERT INTO person(username, name, level, dob) VALUES (?, ?, ?, ?)";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String uName = p.getUsername() != null && !p.getUsername().isBlank() ? p.getUsername() : "student";
            ps.setString(1, uName);
            ps.setString(2, p.getName());
            ps.setString(3, p.getLevel());
            ps.setString(4, p.getDob() == null ? null : p.getDob().toString());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Person p) {
        String sql = "UPDATE person SET username = ?, name = ?, level = ?, dob = ? WHERE id = ?";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            String uName = p.getUsername() != null && !p.getUsername().isBlank() ? p.getUsername() : "student";
            ps.setString(1, uName);
            ps.setString(2, p.getName());
            ps.setString(3, p.getLevel());
            ps.setString(4, p.getDob() == null ? null : p.getDob().toString());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Object id) {
        if (id == null) return false;
        if (id instanceof Integer) return delete((Integer) id);
        try {
            return delete(Integer.parseInt(id.toString()));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM person WHERE id = ?";
        try (Connection c = dbHelper.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
