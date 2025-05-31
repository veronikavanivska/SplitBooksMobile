package com.example.splitbooks.DTO;

public class LoginResponse {
    private Long userId;
    private String token;

    // Constructor
    public LoginResponse(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}