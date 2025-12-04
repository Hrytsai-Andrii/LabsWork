package com.example.labs.client;

import com.example.labs.common.model.MusicPlayer;
import com.example.labs.common.NetworkRequest;
import com.example.labs.common.NetworkResponse;
import com.example.labs.common.model.*;
import com.example.labs.common.patterns.memento.PlayerMemento;
import com.example.labs.server.service.*;

import java.util.List;

public class MusicSystemFacade {
    private final MusicPlayer musicPlayer;
    private final UserService userService;
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final NetworkClient netClient;

    public MusicSystemFacade() {
        this.netClient = new NetworkClient();

        this.userService = new UserService(null) {
            @Override
            public User login(String email, String password) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("LOGIN", new String[]{email, password}));
                return resp.isSuccess() ? (User) resp.getData() : null;
            }

            @Override
            public User register(String name, String email, String password) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("REGISTER", new String[]{name, email, password}));
                return resp.isSuccess() ? (User) resp.getData() : null;
            }

            @Override
            public User getUserByID(int id) {
                
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("GET_USER", id));
                return resp.isSuccess() ? (User) resp.getData() : null;
            }
        };

        this.trackService = new TrackService(null) {
            @Override
            public List<Track> getAllTracks(int userId) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("GET_USER_TRACKS", userId));
                return resp.isSuccess() ? (List<Track>) resp.getData() : List.of();
            }

            @Override
            public void addTrack(Track track) {
                netClient.sendRequest(new NetworkRequest("ADD_TRACK", track));
            }

            @Override
            public void updateTrack(Track track) {
                netClient.sendRequest(new NetworkRequest("UPDATE_TRACK", track));
            }

            @Override
            public void deleteTrack(int id) {
                netClient.sendRequest(new NetworkRequest("DELETE_TRACK", id));
            }

            @Override
            public Track getTrackByID(int id) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("GET_TRACK", id));
                return resp.isSuccess() ? (Track) resp.getData() : null;
            }

            @Override
            public void linkTrackToUser(int userId, int trackId) {
                netClient.sendRequest(new NetworkRequest("LINK_TRACK_USER", new int[]{userId, trackId}));
            }
        };

        this.playlistService = new PlaylistService(null) {
            @Override
            public List<Playlist> getAllPlaylists(int userId) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("GET_USER_PLAYLISTS", userId));
                return resp.isSuccess() ? (List<Playlist>) resp.getData() : List.of();
            }

            @Override
            public Playlist createPlaylist(Playlist playlist) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("CREATE_PLAYLIST", playlist));
                return resp.isSuccess() ? (Playlist) resp.getData() : null;
            }

            @Override
            public void updatePlaylist(Playlist playlist) {
                netClient.sendRequest(new NetworkRequest("UPDATE_PLAYLIST", playlist));
            }

            @Override
            public void deletePlaylist(int id) {
                netClient.sendRequest(new NetworkRequest("DELETE_PLAYLIST", id));
            }

            @Override
            public void linkPlaylistToUser(int userId, int playlistId) {
                netClient.sendRequest(new NetworkRequest("LINK_PLAYLIST_USER", new int[]{userId, playlistId}));
            }

            @Override
            public Playlist getPlaylistByID(int id) {
                NetworkResponse resp = netClient.sendRequest(new NetworkRequest("GET_PLAYLIST", id));
                return resp.isSuccess() ? (Playlist) resp.getData() : null;
            }
        };
        
        this.musicPlayer = new MusicPlayer(userService, trackService, playlistService);
        
        User defaultUser = userService.getUserByID(1);
        if (defaultUser != null) this.musicPlayer.loginUser(1);
    }

    public MusicPlayer getPlayer() { return musicPlayer; }
    public UserService getUserService() { return userService; }
    public TrackService getTrackService() { return trackService; }
    public PlaylistService getPlaylistService() { return playlistService; }

    public User authenticate(String email, String password) {
        User user = userService.login(email, password);
        if (user != null) musicPlayer.loginUser(user.getUserID());
        return user;
    }

    public User register(String name, String email, String password) {
        User user = userService.register(name, email, password);
        if (user != null) musicPlayer.loginUser(user.getUserID());
        return user;
    }

    public void logout() {
        musicPlayer.stop(); 
        User defaultUser = userService.getUserByID(1);
        if (defaultUser != null) {
            musicPlayer.loginUser(defaultUser.getUserID());
        }
    }

    public List<Track> getCurrentUserTracks() {
        if (musicPlayer.getCurrentUser() == null) return List.of();
        return trackService.getAllTracks(musicPlayer.getCurrentUser().getUserID());
    }

    public List<Playlist> getCurrentUserPlaylists() {
        if (musicPlayer.getCurrentUser() == null) return List.of();
        return playlistService.getAllPlaylists(musicPlayer.getCurrentUser().getUserID());
    }

    public void addTrackToLibrary(Track track) {

        if (musicPlayer.getCurrentUser() != null) {
            int userId = musicPlayer.getCurrentUser().getUserID();
            netClient.sendRequest(new NetworkRequest("ADD_TRACK", new Object[]{userId, track}));
        }
    }

    public void saveState(int userId, PlayerMemento memento) {
        
        netClient.sendRequest(new NetworkRequest("SAVE_STATE", new Object[]{userId, memento}));
    }

    public PlayerMemento loadState(int userId) {
        NetworkResponse resp = netClient.sendRequest(new NetworkRequest("LOAD_STATE", userId));
        return resp.isSuccess() ? (PlayerMemento) resp.getData() : null;
    }
}