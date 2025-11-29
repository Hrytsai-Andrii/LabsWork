package com.example.kursova.common.model;

import com.example.kursova.common.patterns.iterator.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Playlist implements Playable, Serializable {

    private int playlistID;
    private String name;
    // Зберігає ID треків
    private List<Integer> trackList;

    // --- ВИПРАВЛЕННЯ 1: Додано transient ---
    // Це поле не буде передаватися через мережу
    private transient AudioIterator<Integer> iterator;

    private boolean isShuffle = false;
    private boolean isPlaying = false;

    public Playlist(int playlistID, String name) {
        this.playlistID = playlistID;
        this.name = name;
        this.trackList = new ArrayList<>();

        // За замовчуванням - лінійний ітератор
        this.iterator = new LinearIterator<>(this.trackList);
    }

    // --- ВИПРАВЛЕННЯ 2: Метод відновлення після десеріалізації ---
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Читаємо стандартні поля (ID, name, trackList)
        updateIterator();       // Створюємо ітератор наново
    }

    public void addTrack(int trackID) {
        trackList.add(trackID);
        // Оновлюємо ітератор, щоб він врахував новий трек
        updateIterator();
    }

    public void removeTrack(int trackID) {
        // Видаляємо об'єкт (ID), а не за індексом
        trackList.remove(Integer.valueOf(trackID));
        updateIterator();
    }

    /**
     * Оновлює стратегію обходу списку.
     * Вибір лише між Shuffle та Linear (без Repeat).
     */
    private void updateIterator() {
        // Перевірка на null потрібна, якщо метод викликається з readObject до ініціалізації списку (хоча defaultReadObject це робить)
        if (this.trackList == null) this.trackList = new ArrayList<>();

        if (isShuffle) {
            this.iterator = new ShuffleIterator<>(this.trackList);
        } else {
            this.iterator = new LinearIterator<>(this.trackList);
        }
    }

    @Override
    public void play() {
        if (!trackList.isEmpty()) {
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
        // Скидання стану навігації (перестворення ітератора)
        updateIterator();
    }

    @Override
    public int next() {
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
            // Повертає Integer (ID)
        }
        return -1;
    }

    @Override
    public int previous() {
        if (iterator != null && iterator.hasPrevious()) {
            return iterator.previous();
            // Повертає Integer (ID)
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
        // Повертаємо копію списку для безпеки
        return new ArrayList<>(trackList);
    }

    // --- Геттери та Сеттери ---

    public int getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(int playlistID) {
        this.playlistID = playlistID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void setCurrentTrack(int trackId) {
        // Просто передаємо ID, бо ітератор типізований як Integer
        if (iterator == null) updateIterator();
        iterator.setCursor(trackId);
    }
}