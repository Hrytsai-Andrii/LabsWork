package com.example.kursova.common;

import java.io.Serializable;

public class NetworkRequest implements Serializable {
    // Унікальний ID для серіалізації (гарантує сумісність версій)
    private static final long serialVersionUID = 1L;

    private String command; // Назва команди, наприклад "LOGIN", "ADD_TRACK"
    private Object payload; // Будь-які дані (User, Track, String[], Integer тощо)

    // Конструктор порожній (іноді потрібен для бібліотек серіалізації)
    public NetworkRequest() {
    }

    // Основний конструктор
    public NetworkRequest(String command, Object payload) {
        this.command = command;
        this.payload = payload;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "NetworkRequest{" +
                "command='" + command + '\'' +
                ", payload=" + payload +
                '}';
    }
}