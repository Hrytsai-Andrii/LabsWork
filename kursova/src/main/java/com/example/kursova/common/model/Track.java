package com.example.kursova.common.model;

import com.example.kursova.common.patterns.visitor.Visitable;
import com.example.kursova.common.patterns.visitor.Visitor;

import java.io.Serializable;
import java.util.Objects;

public class Track implements Visitable, Serializable {

    private int trackID;
    private String title;
    private String artist;
    private String album;
    private double duration; // Тривалість у секундах або хвилинах (згідно з діаграмою тип double)
    private String filePath;

    public Track() {
    }

    public Track(int trackID, String title, String artist, String album, double duration, String filePath) {
        this.trackID = trackID;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.filePath = filePath;
    }


    public int getTrackID() {
        return trackID;
    }

    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + trackID +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return trackID == track.trackID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackID);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitTrack(this);
    }
}