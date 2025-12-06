package com.example.labs.common.patterns.memento;

import java.io.Serializable;
import java.util.List;

public class PlayerMemento implements Serializable {
    private static final long serialVersionUID = 1L; 

    private final int volume;
    private final boolean isShuffle;
    private final boolean isRepeat;
    private final int currentTrackId;
    private final int sourceType;
    private final int sourceId;
    private final double currentTime;
    private final List<Double> eqBands; // Нове поле

    public PlayerMemento(int volume, boolean isShuffle, boolean isRepeat,
                         int currentTrackId, int sourceType, int sourceId,
                         double currentTime, List<Double> eqBands) {
        this.volume = volume;
        this.isShuffle = isShuffle;
        this.isRepeat = isRepeat;
        this.currentTrackId = currentTrackId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.currentTime = currentTime;
        this.eqBands = eqBands;
    }

    public int getVolume() { return volume; }
    public boolean isShuffle() { return isShuffle; }
    public boolean isRepeat() { return isRepeat; }
    public int getCurrentTrackId() { return currentTrackId; }
    public int getSourceType() { return sourceType; }
    public int getSourceId() { return sourceId; }
    public double getCurrentTime() { return currentTime; }
    public List<Double> getEqBands() { return eqBands; }
}