package com.example.kursova.server;

import com.example.kursova.server.repository.*;
import com.example.kursova.server.service.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {

    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println(">>> Запуск сервера...");

        // 1. Ініціалізація бази даних (створення таблиць)
        try {
            // Використовуємо логіку з старого DatabaseConnection
            DatabaseConnection.initSchema(); //// [cite: 99, 355]
            System.out.println(">>> База даних ініціалізована.");
        } catch (Exception e) {
            System.err.println("!!! Помилка ініціалізації БД:");
            e.printStackTrace();
            return;
        }

        // 2. Ініціалізація шарів (Repository -> Service)
        // Створюємо об'єкти, які будуть обробляти логіку
        UserRepository userRepo = new UserRepository(); // [cite: 101]
        TrackRepository trackRepo = new TrackRepository(); // [cite: 102]
        PlaylistRepository playlistRepo = new PlaylistRepository(); // [cite: 102]
        StateRepository stateRepo = new StateRepository(); // [cite: 6]

        UserService userService = new UserService(userRepo); // [cite: 102]
        TrackService trackService = new TrackService(trackRepo); // [cite: 102]
        PlaylistService playlistService = new PlaylistService(playlistRepo); // [cite: 103]

        // 3. Запуск мережевого сервера
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(">>> Сервер слухає порт " + PORT);
            System.out.println(">>> Очікування клієнтів...");

            while (true) {
                // Блокується, поки не під'єднається клієнт
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> Нове підключення: " + clientSocket.getInetAddress());

                // Створюємо новий потік для клієнта, передаючи йому сервіси
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