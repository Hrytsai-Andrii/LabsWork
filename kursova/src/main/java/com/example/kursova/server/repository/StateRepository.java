package com.example.kursova.server.repository;

import com.example.kursova.common.patterns.memento.PlayerMemento;
import com.example.kursova.server.DatabaseConnection;

import java.sql.*;

public class StateRepository {
    // com.example.kursova.server.repository.StateRepository

    public void saveState(int userId, PlayerMemento memento) {
        String sql = "INSERT OR REPLACE INTO playback_state (user_id, volume, is_shuffle, is_repeat, current_track_id, source_type, source_id, current_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, memento.getVolume());
            pstmt.setInt(3, memento.isShuffle() ? 1 : 0);
            pstmt.setInt(4, memento.isRepeat() ? 1 : 0);
            pstmt.setInt(5, memento.getCurrentTrackId());
            pstmt.setInt(6, memento.getSourceType());
            pstmt.setInt(7, memento.getSourceId());
            pstmt.setDouble(8, memento.getCurrentTime()); // <-- Зберігаємо час

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerMemento loadState(int userId) {
        String sql = "SELECT volume, is_shuffle, is_repeat, current_track_id, source_type, source_id, current_time FROM playback_state WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new PlayerMemento(
                        rs.getInt("volume"),
                        rs.getBoolean("is_shuffle"),
                        rs.getBoolean("is_repeat"),
                        rs.getInt("current_track_id"),
                        rs.getInt("source_type"),
                        rs.getInt("source_id"),
                        rs.getDouble("current_time") // <-- Завантажуємо час
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}