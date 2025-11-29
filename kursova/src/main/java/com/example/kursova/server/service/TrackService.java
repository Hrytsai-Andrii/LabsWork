package com.example.kursova.server.service;

import com.example.kursova.common.model.Track;
import com.example.kursova.server.repository.TrackRepository;
import java.util.List;

public class TrackService {

    private TrackRepository trackRepository;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public void addTrack(Track track) {
        if (track != null && track.getFilePath() != null) {
            trackRepository.update(track);
        }
    }

    public void updateTrack(Track track) {
        trackRepository.update(track);
    }

    public void deleteTrack(int id) {
        trackRepository.delete(id);
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track getTrackByID(int id) {
        return trackRepository.findByID(id);
    }

    public List<Track> getAllTracks(int userId) {
        // Викликаємо специфічний метод для юзера
        return trackRepository.findAllByUserId(userId);
    }

    public void linkTrackToUser(int userId, int trackId) {
        trackRepository.linkTrackToUser(userId, trackId);
    }
}
