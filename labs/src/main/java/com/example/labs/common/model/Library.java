package com.example.labs.common.model;

import com.example.labs.common.patterns.iterator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Library implements Playable {
    private List<Track> tracks;
    private AudioIterator<Track> iterator;
    private boolean isShuffle = false;
    private boolean isPlaying = false;

    public Library() {
        this.tracks = new ArrayList<>();
        this.iterator = new LinearIterator<>(this.tracks);
    }

    public void addTrack(Track track) {
        if (track != null && !tracks.contains(track)) {
            tracks.add(track);
            updateIterator();
        }
    }

    public void removeTrack(Track track) {
        tracks.remove(track);
        updateIterator();
    }

    private void updateIterator() {
        if (isShuffle) {
            this.iterator = new ShuffleIterator<>(this.tracks);
        } else {
            this.iterator = new LinearIterator<>(this.tracks);
        }
    }

    @Override
    public void play() {
        if (!tracks.isEmpty()) {
            isPlaying = true;
        }
    }

    @Override
    public void pause() {
        isPlaying = false;
    }

    @Override
    public void stop() {
        isPlaying = false;
        updateIterator();
    }

    @Override
    public int next() {
        if (iterator.hasNext()) {
            Track t = iterator.next();
            return t != null ? t.getTrackID() : -1;
        }
        return -1;
    }

    @Override
    public int previous() {
        if (iterator.hasPrevious()) {
            Track t = iterator.previous();
            return t != null ? t.getTrackID() : -1;
        }
        return -1;
    }

    @Override
    public void setShuffle(boolean enable) {
        if (this.isShuffle != enable) {
            this.isShuffle = enable;
            updateIterator(); 
        }
    }

    @Override
    public boolean isShuffle() {
        return isShuffle;
    }

    @Override
    public List<Integer> getTracks() {
        return tracks.stream()
                .map(Track::getTrackID)
                .collect(Collectors.toList());
    }

    public List<Track> getFullTrackList() {
        return new ArrayList<>(tracks);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        Track foundTrack = tracks.stream()
                .filter(t -> t.getTrackID() == trackId)
                .findFirst()
                .orElse(null);

        if (foundTrack != null) {
            iterator.setCursor(foundTrack);
        }
    }
}