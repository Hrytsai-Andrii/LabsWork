package com.example.kursova.common.patterns.memento;

import java.io.Serializable;

public class PlayerMemento implements Serializable {
    private static final long serialVersionUID = 1L; // <--- Бажано додати ID версії

    private final int volume;
    private final boolean isShuffle;
    private final boolean isRepeat;
    private final int currentTrackId;
    private final int sourceType;
    private final int sourceId;
    private final double currentTime;

    public PlayerMemento(int volume, boolean isShuffle, boolean isRepeat, int currentTrackId, int sourceType, int sourceId, double currentTime) {
        this.volume = volume;
        this.isShuffle = isShuffle;
        this.isRepeat = isRepeat;
        this.currentTrackId = currentTrackId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.currentTime = currentTime;
    }

    public int getVolume() { return volume; }
    public boolean isShuffle() { return isShuffle; }
    public boolean isRepeat() { return isRepeat; }
    public int getCurrentTrackId() { return currentTrackId; }
    public int getSourceType() { return sourceType; }
    public int getSourceId() { return sourceId; }

    public double getCurrentTime() { return currentTime; } // <-- НОВИЙ ГЕТТЕР
}