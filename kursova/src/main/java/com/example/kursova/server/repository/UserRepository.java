package com.example.kursova.server.repository;

import com.example.kursova.common.model.User;
import com.example.kursova.server.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements IRepository<User> {

    @Override
    public User findByID(int id) {
        String sql = "SELECT id, name, email, password FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Метод для пошуку по Email
    public User findByEmail(String email) {
        String sql = "SELECT id, name, email, password FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRowToUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, password FROM users";
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // ВАЖЛИВО: Цей метод часто є причиною помилки
    public int createUser(User user) {
        // Ми НЕ передаємо 'id' (він AUTOINCREMENT) і НЕ передаємо 'settings' (ми їх видалили)
        String sql = "INSERT INTO users(name, email, password) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1); // Повертаємо новий ID
                }
            }
        } catch (SQLException e) {
            // Якщо тут помилка - у консолі буде причина, а метод поверне -1
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET name=?, email=?, password=? WHERE id=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getUserID());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.connect().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password")
                // settings видалено, тут його бути не повинно
        );
    }
}