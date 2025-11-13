package com.example.todo.auth.dto;

// TodoRequest.java

import jakarta.validation.constraints.NotBlank;

public class TodoRequest {
    @NotBlank
    private String title;

    private String description;

    // Constructors, getters, setters
    public TodoRequest() {}

    public TodoRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
