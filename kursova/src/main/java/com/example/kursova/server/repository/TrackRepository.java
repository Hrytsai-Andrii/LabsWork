package com.example.kursova.server.repository;


import com.example.kursova.common.model.Track;
import com.example.kursova.server.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackRepository implements IRepository<Track> {
    @Override
    public Track findByID(int id) {
        String sql = "SELECT id, title, artist, album, duration, filepath FROM tracks WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToTrack(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Track> findAll() {
        List<Track> tracks = new ArrayList<>();
        String sql = "SELECT id, title, artist, album, duration, filepath FROM tracks";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tracks.add(mapRowToTrack(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tracks;
    }

    @Override
    public void update(Track track) {
        if (track.getTrackID() == 0) {
            // --- INSERT (Створення) ---
            String sql = "INSERT INTO tracks(title, artist, album, duration, filepath) VALUES(?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, track.getTitle());
                pstmt.setString(2, track.getArtist());
                pstmt.setString(3, track.getAlbum());
                pstmt.setDouble(4, track.getDuration());
                pstmt.setString(5, track.getFilePath());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            track.setTrackID(rs.getInt(1)); // Встановлюємо згенерований ID об'єкту
                        }
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            // --- UPDATE (Оновлення існуючого) ---
            String sql = "UPDATE tracks SET title=?, artist=?, album=?, duration=?, filepath=? WHERE id=?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, track.getTitle());
                pstmt.setString(2, track.getArtist());
                pstmt.setString(3, track.getAlbum());
                pstmt.setDouble(4, track.getDuration());
                pstmt.setString(5, track.getFilePath());
                pstmt.setInt(6, track.getTrackID());

                pstmt.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tracks WHERE id = ?";

        try (PreparedStatement pstmt = DatabaseConnection.connect().prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Track mapRowToTrack(ResultSet rs) throws SQLException {
        return new Track(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("album"),
                rs.getDouble("duration"),
                rs.getString("filepath")
        );
    }

    public List<Track> findTracksByUserId(int userId) {
        List<Track> tracks = new ArrayList<>();
        String sql = "SELECT t.* FROM tracks t JOIN users_tracks ut ON t.id = ut.track_id WHERE ut.user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) tracks.add(mapRowToTrack(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return tracks;
    }

    public void linkTrackToUser(int userId, int trackId) {
        String sql = "INSERT OR IGNORE INTO users_tracks (user_id, track_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, trackId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // У файлі com/example/kursova/repository/TrackRepository.java

    public List<Track> findAllByUserId(int userId) {
        List<Track> tracks = new ArrayList<>();
        // Запит об'єднує таблицю треків і таблицю зв'язків
        String sql = "SELECT t.id, t.title, t.artist, t.album, t.duration, t.filepath " +
                "FROM tracks t " +
                "JOIN users_tracks ut ON t.id = ut.track_id " +
                "WHERE ut.user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Використовуємо існуючий метод маппінгу
                tracks.add(mapRowToTrack(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tracks;
    }
}
