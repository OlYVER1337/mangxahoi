package com.example.messengerapp.models;

public class ApiResponse {
    private String message;

    // Constructor
    public ApiResponse(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter nếu cần
    public void setMessage(String message) {
        this.message = message;
    }
}
