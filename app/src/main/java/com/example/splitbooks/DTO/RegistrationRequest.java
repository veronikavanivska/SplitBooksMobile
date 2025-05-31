package com.example.splitbooks.DTO;


public class RegistrationRequest {
    private String email;
    private String password;

    private String username;

    public RegistrationRequest(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
