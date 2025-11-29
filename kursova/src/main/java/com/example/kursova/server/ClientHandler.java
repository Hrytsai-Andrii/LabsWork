package com.example.kursova.server;

import com.example.kursova.common.NetworkRequest;
import com.example.kursova.common.NetworkResponse;
import com.example.kursova.common.model.*;
import com.example.kursova.common.patterns.memento.PlayerMemento;
import com.example.kursova.server.repository.StateRepository;
import com.example.kursova.server.service.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final UserService userService;
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final StateRepository stateRepository;

    public ClientHandler(Socket socket,
                         UserService userService,
                         TrackService trackService,
                         PlaylistService playlistService,
                         StateRepository stateRepository) {
        this.socket = socket;
        this.userService = userService;
        this.trackService = trackService;
        this.playlistService = playlistService;
        this.stateRepository = stateRepository;
    }

    @Override
    public void run() {
        try (
                // Потоки введення/виведення об'єктів
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // Читаємо запит від клієнта
            Object inputObject = in.readObject();
            if (inputObject instanceof NetworkRequest) {
                NetworkRequest request = (NetworkRequest) inputObject;

                // Обробляємо запит і отримуємо відповідь
                NetworkResponse response = handleRequest(request);

                // Відправляємо відповідь клієнту
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Помилка обробки клієнта: " + e.getMessage());
            // e.printStackTrace(); // Розкоментувати для дебагу
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private NetworkResponse handleRequest(NetworkRequest req) {
        String command = req.getCommand();
        Object payload = req.getPayload();

        System.out.println("Отримано команду: " + command);

        try {
            switch (command) {
                // --- АВТОРИЗАЦІЯ ---
                case "LOGIN":
                    String[] loginData = (String[]) payload; // [email, password]
                    User user = userService.login(loginData[0], loginData[1]);// [cite: 106]
                    return new NetworkResponse(user != null, user);

                case "REGISTER":
                    String[] regData = (String[]) payload; // [name, email, password]
                    User newUser = userService.register(regData[0], regData[1], regData[2]);// [cite: 109]
                    return new NetworkResponse(newUser != null, newUser);

                // --- ТРЕКИ ---
                case "GET_USER_TRACKS":
                    int userId = (int) payload;
                    List<Track> tracks = trackService.getAllTracks(userId);// [cite: 114]
                    return new NetworkResponse(true, tracks);

                case "ADD_TRACK":
                    Object[] addData = (Object[]) payload;
                    int trackOwnerId = (int) addData[0];
                    Track track = (Track) addData[1];
                    trackService.addTrack(track);
                    if (track.getTrackID() != 0) {
                        trackService.linkTrackToUser(trackOwnerId, track.getTrackID());
                    }

                    return new NetworkResponse(true, track);

                case "GET_USER_PLAYLISTS":
                    int pUserId = (int) payload;
                    List<Playlist> playlists = playlistService.getAllPlaylists(pUserId);// [cite: 115]
                    return new NetworkResponse(true, playlists);

                case "CREATE_PLAYLIST":
                    Playlist pl = (Playlist) payload;
                    playlistService.createPlaylist(pl);// [cite: 78]
                    // Повертаємо оновлений плейлист (з ID)
                    return new NetworkResponse(true, pl);

                case "LINK_PLAYLIST_USER":
                    // Очікуємо int[]{userId, playlistId}
                    int[] linkData = (int[]) payload;
                    playlistService.linkPlaylistToUser(linkData[0], linkData[1]);// [cite: 78]
                    return new NetworkResponse(true, null);

                // --- СТАН (MEMENTO) ---
                case "SAVE_STATE":
                    // Очікуємо об'єкт, що містить userId і memento
                    // Наприклад, Object[]{userId, memento}
                    Object[] stateData = (Object[]) payload;
                    stateRepository.saveState((int) stateData[0], (PlayerMemento) stateData[1]);// [cite: 95]
                    return new NetworkResponse(true, null);

                case "LOAD_STATE":
                    int sUserId = (int) payload;
                    PlayerMemento memento = stateRepository.loadState(sUserId);// [cite: 92]
                    return new NetworkResponse(true, memento);
                // У метод handleRequest додайте:

                case "GET_TRACK":
                    int tId = (int) payload;
                    Track t = trackService.getTrackByID(tId);
                    return new NetworkResponse(t != null, t);

                case "DELETE_TRACK":
                    trackService.deleteTrack((int) payload);
                    return new NetworkResponse(true, null);

                case "UPDATE_TRACK":
                    trackService.updateTrack((Track) payload);
                    return new NetworkResponse(true, null);

                case "LINK_TRACK_USER":
                    int[] linkTrackData = (int[]) payload;
                    trackService.linkTrackToUser(linkTrackData[0], linkTrackData[1]);
                    return new NetworkResponse(true, null);

                case "GET_PLAYLIST":
                    int pId = (int) payload;
                    Playlist playlist = playlistService.getPlaylistByID(pId);
                    return new NetworkResponse(playlist != null, playlist);

                case "DELETE_PLAYLIST":
                    playlistService.deletePlaylist((int) payload);
                    return new NetworkResponse(true, null);

                case "UPDATE_PLAYLIST":
                    playlistService.updatePlaylist((Playlist) payload);
                    return new NetworkResponse(true, null);

                case "GET_USER":
                    int userIdForSearch = (int) payload;
                    User foundUser = userService.getUserByID(userIdForSearch);
                    return new NetworkResponse(foundUser != null, foundUser);

                default:
                    return new NetworkResponse(false, "Невідома команда: " + command);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NetworkResponse(false, "Помилка сервера: " + e.getMessage());
        }
    }
}