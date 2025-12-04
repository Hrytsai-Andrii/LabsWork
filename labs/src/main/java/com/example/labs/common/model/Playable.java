package com.example.labs.common.model;

import java.util.List;

public interface Playable {
    void play();
    void pause();
    void stop();
    int next();
    int previous();
    void setShuffle(boolean enable);
    boolean isShuffle();
    List<Integer> getTracks();
    void setCurrentTrack(int trackId);
}