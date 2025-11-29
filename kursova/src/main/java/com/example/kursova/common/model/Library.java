package com.example.kursova.common.model;

import com.example.kursova.common.patterns.iterator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Library implements Playable {
    private List<Track> tracks;

    // Використовуємо узагальнений ітератор для об'єктів Track
    private AudioIterator<Track> iterator;

    private boolean isShuffle = false;
    private boolean isPlaying = false;

    public Library() {
        this.tracks = new ArrayList<>();
        // За замовчуванням - лінійний порядок
        this.iterator = new LinearIterator<>(this.tracks);
    }

    public void addTrack(Track track) {
        if (track != null && !tracks.contains(track)) {
            tracks.add(track);
            // Оновлюємо ітератор, щоб він врахував нові дані
            updateIterator();
        }
    }

    public void removeTrack(Track track) {
        tracks.remove(track);
        updateIterator();
    }

    /**
     * Фабричний метод для оновлення стратегії обходу.
     * Тут лише Linear або Shuffle, без Repeat.
     */
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
            // Повертаємо ID, оскільки інтерфейс Playable вимагає int
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
            updateIterator(); // Зміна режиму змінює тип ітератора
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
        // Шукаємо трек у списку за ID
        Track foundTrack = tracks.stream()
                .filter(t -> t.getTrackID() == trackId)
                .findFirst()
                .orElse(null);

        if (foundTrack != null) {
            iterator.setCursor(foundTrack);
        }
    }
}