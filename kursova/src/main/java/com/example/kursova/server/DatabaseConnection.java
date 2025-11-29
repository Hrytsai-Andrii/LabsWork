package com.example.kursova.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:music_player.db";
    private static volatile Connection instance;

    private DatabaseConnection() {}

    public static Connection connect() throws SQLException {
        if (instance == null || instance.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (instance == null || instance.isClosed()) {
                    instance = DriverManager.getConnection(URL);
                    configureConnection(instance);
                }
            }
        }
        return instance;
    }

    private static void configureConnection(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    public static void initSchema() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // 1. Треки з автоінкрементом
            stmt.execute("CREATE TABLE IF NOT EXISTS tracks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // ЗМІНЕНО
                    "title TEXT, " +
                    "artist TEXT, " +
                    "album TEXT, " +
                    "duration REAL, " +
                    "filepath TEXT)");

            // 2. Плейлисти з автоінкрементом
            stmt.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // ЗМІНЕНО
                    "name TEXT)");

            // ... таблиці зв'язків та юзерів залишаються без змін ...
            stmt.execute("CREATE TABLE IF NOT EXISTS playlist_tracks (" +
                    "playlist_id INTEGER, track_id INTEGER, " +
                    "FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(track_id) REFERENCES tracks(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Юзери теж
                    "name TEXT, email TEXT UNIQUE, password TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS users_tracks (" +
                    "user_id INTEGER, track_id INTEGER, " +
                    "PRIMARY KEY (user_id, track_id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(track_id) REFERENCES tracks(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS users_playlists (" +
                    "user_id INTEGER, playlist_id INTEGER, " +
                    "PRIMARY KEY (user_id, playlist_id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE)");

            stmt.execute("INSERT OR IGNORE INTO users (id, name, email, password) VALUES (1, 'Default User', 'default@mp.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918')");
            stmt.execute("CREATE TABLE IF NOT EXISTS playback_state (" +
                    "user_id INTEGER PRIMARY KEY, " +
                    "volume INTEGER, " +
                    "is_shuffle INTEGER, " +
                    "is_repeat INTEGER, " +
                    "current_track_id INTEGER, " +
                    "source_type INTEGER, " +
                    "source_id INTEGER, " +
                    "current_time REAL, " + // <-- НОВА КОЛОНКА
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}