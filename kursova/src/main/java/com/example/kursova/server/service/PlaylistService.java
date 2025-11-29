package com.example.kursova.server.service;


import com.example.kursova.common.model.Playlist;
import com.example.kursova.server.repository.PlaylistRepository;
import java.util.List;

public class PlaylistService {

    private PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    public Playlist createPlaylist(Playlist playlist) {
        if (playlist != null) {
            playlistRepository.update(playlist);
            return playlist;
        }
        return null;
    }

    public void updatePlaylist(Playlist playlist) {
        // Логіка оновлення вмісту плейлиста в БД
        playlistRepository.update(playlist);
    }

    public void deletePlaylist(int id) {
        playlistRepository.delete(id);
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    public Playlist getPlaylistByID(int id) {
        return playlistRepository.findByID(id);
    }

    public List<Playlist> getAllPlaylists(int userId) {
        return playlistRepository.findAllByUserId(userId);
    }

    public void linkPlaylistToUser(int userId, int playlistId) {
        playlistRepository.linkPlaylistToUser(userId, playlistId);
    }
}