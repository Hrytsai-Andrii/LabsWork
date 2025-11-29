package com.example.kursova.client;

import com.example.kursova.common.NetworkRequest;
import com.example.kursova.common.NetworkResponse;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    public NetworkResponse sendRequest(NetworkRequest request) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            return (NetworkResponse) in.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return new NetworkResponse(false, "Помилка з'єднання: " + e.getMessage());
        }
    }
}