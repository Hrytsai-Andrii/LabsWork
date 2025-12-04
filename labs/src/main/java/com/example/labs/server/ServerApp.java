package com.example.labs.server;

import com.example.labs.server.repository.*;
import com.example.labs.server.service.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {

    private static final int PORT = 8888;

    public static void main(String[] args) {
        try {
            DatabaseConnection.initSchema(); 
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        UserRepository userRepo = new UserRepository(); 
        TrackRepository trackRepo = new TrackRepository(); 
        PlaylistRepository playlistRepo = new PlaylistRepository(); 
        StateRepository stateRepo = new StateRepository(); 

        UserService userService = new UserService(userRepo); 
        TrackService trackService = new TrackService(trackRepo); 
        PlaylistService playlistService = new PlaylistService(playlistRepo); 

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(
                        clientSocket,
                        userService,
                        trackService,
                        playlistService,
                        stateRepo
                );
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}