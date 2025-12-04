package com.example.labs.server.repository;

import com.example.labs.common.model.Playlist;
import com.example.labs.server.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PlaylistRepository implements IRepository<Playlist> {
    @Override
    public Playlist findByID(int id) {
        String sql = "SELECT id, name FROM playlists WHERE id = ?";
        Playlist playlist = null;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                playlist = new Playlist(rs.getInt("id"), rs.getString("name"));
                loadPlaylistTracks(playlist, conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlist;
    }

    @Override
    public List<Playlist> findAll() {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT id, name FROM playlists";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Playlist pl = new Playlist(rs.getInt("id"), rs.getString("name"));
                loadPlaylistTracks(pl, conn);
                playlists.add(pl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    @Override
    public void update(Playlist playlist) {
        if (playlist.getPlaylistID() == 0) {
            String sql = "INSERT INTO playlists(name) VALUES(?)";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, playlist.getName());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            playlist.setPlaylistID(rs.getInt(1)); 
                        }
                    }
                }
                
                savePlaylistTracks(playlist, conn);

            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            
            String sql = "UPDATE playlists SET name=? WHERE id=?";
            try (Connection conn = DatabaseConnection.connect()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, playlist.getName());
                    pstmt.setInt(2, playlist.getPlaylistID());
                    pstmt.executeUpdate();
                }
                
                savePlaylistTracks(playlist, conn);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    
    private void savePlaylistTracks(Playlist playlist, Connection conn) throws SQLException {
        String sqlDelete = "DELETE FROM playlist_tracks WHERE playlist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
            pstmt.setInt(1, playlist.getPlaylistID());
            pstmt.executeUpdate();
        }

        if (!playlist.getTracks().isEmpty()) {
            String sqlInsert = "INSERT INTO playlist_tracks(playlist_id, track_id) VALUES(?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                for (Integer trackId : playlist.getTracks()) {
                    pstmt.setInt(1, playlist.getPlaylistID());
                    pstmt.setInt(2, trackId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM playlists WHERE id = ?";

        try (PreparedStatement pstmt = DatabaseConnection.connect().prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaylistTracks(Playlist playlist, Connection conn) throws SQLException {
        String sql = "SELECT track_id FROM playlist_tracks WHERE playlist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlist.getPlaylistID());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                playlist.addTrack(rs.getInt("track_id"));
            }
        }
    }

    public List<Playlist> findPlaylistsByUserId(int userId) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT p.* FROM playlists p JOIN users_playlists up ON p.id = up.playlist_id WHERE up.user_id = ?";
        return playlists;
    }

    public void linkPlaylistToUser(int userId, int playlistId) {
        String sql = "INSERT OR IGNORE INTO users_playlists (user_id, playlist_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, playlistId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Playlist> findAllByUserId(int userId) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT p.id, p.name " +
                "FROM playlists p " +
                "JOIN users_playlists up ON p.id = up.playlist_id " +
                "WHERE up.user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Playlist pl = new Playlist(rs.getInt("id"), rs.getString("name"));
                loadPlaylistTracks(pl, conn);
                playlists.add(pl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }
}
