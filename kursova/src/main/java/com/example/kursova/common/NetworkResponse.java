package com.example.kursova.common;

import java.io.Serializable;

public class NetworkResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;      // Чи успішна операція?
    private Object data;          // Дані, які повертає сервер (наприклад, User або List<Track>)
    private String errorMessage;  // Повідомлення, якщо success = false

    public NetworkResponse() {
    }

    // Конструктор для успішної відповіді
    public NetworkResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;
        this.errorMessage = null;
    }

    // Конструктор для помилки (зручно викликати)
    public NetworkResponse(boolean success, String errorMessage) {
        this.success = success;
        this.data = null;
        this.errorMessage = errorMessage;
    }

    // Повний конструктор
    public NetworkResponse(boolean success, Object data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "NetworkResponse{" +
                "success=" + success +
                ", data=" + data +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}