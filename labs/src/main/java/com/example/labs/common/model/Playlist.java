package com.example.labs.common.model;

import com.example.labs.common.patterns.iterator.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Playlist implements Playable, Serializable {

    private int playlistID;
    private String name;
    private List<Integer> trackList;
    private transient AudioIterator<Integer> iterator;

    private boolean isShuffle = false;
    private boolean isPlaying = false;

    public Playlist(int playlistID, String name) {
        this.playlistID = playlistID;
        this.name = name;
        this.trackList = new ArrayList<>();
        this.iterator = new LinearIterator<>(this.trackList);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); 
        updateIterator();       
    }

    public void addTrack(int trackID) {
        trackList.add(trackID);
        
        updateIterator();
    }

    public void removeTrack(int trackID) {
        
        trackList.remove(Integer.valueOf(trackID));
        updateIterator();
    }

    private void updateIterator() {
        
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
        
        updateIterator();
    }

    @Override
    public int next() {
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
            
        }
        return -1;
    }

    @Override
    public int previous() {
        if (iterator != null && iterator.hasPrevious()) {
            return iterator.previous();
            
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
        
        return new ArrayList<>(trackList);
    }

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
        
        if (iterator == null) updateIterator();
        iterator.setCursor(trackId);
    }
}