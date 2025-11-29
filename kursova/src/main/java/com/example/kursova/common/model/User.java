package com.example.kursova.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {

    private int userID;
    private String name;
    private String email;
    private String password;
    // settings видалено

    private List<Playlist> playlists;

    public User() {
        this.playlists = new ArrayList<>();
    }

    // Оновлений конструктор без settings
    public User(int userID, String name, String email, String password) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.playlists = new ArrayList<>();
    }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Playlist> getPlaylists() { return playlists; }
    public void setPlaylists(List<Playlist> playlists) { this.playlists = playlists; }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userID == user.userID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }
}