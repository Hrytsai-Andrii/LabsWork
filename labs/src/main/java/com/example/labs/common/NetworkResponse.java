package com.example.labs.common;

import java.io.Serializable;

public class NetworkResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;      
    private Object data;          
    private String errorMessage;  

    public NetworkResponse() {
    }

    public NetworkResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;
        this.errorMessage = null;
    }

    public NetworkResponse(boolean success, String errorMessage) {
        this.success = success;
        this.data = null;
        this.errorMessage = errorMessage;
    }

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