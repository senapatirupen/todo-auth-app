package com.example.todo.auth.dto;

// AuthRequest.java

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // Constructors, getters, setters
    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
