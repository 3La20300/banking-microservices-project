package com.banking.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON
public class UserResponseDto {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String message;

    // Default constructor
    public UserResponseDto() {}

    // Register Success Response (201)
    public UserResponseDto(UUID userId, String username, String message) {
        this.userId = userId;
        this.username = username;
        this.message = message;
    }

    // Login Success Response (200)
    public UserResponseDto(UUID userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // Profile Success Response (200)
    public UserResponseDto(UUID userId, String username, String email, String firstName, String lastName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
