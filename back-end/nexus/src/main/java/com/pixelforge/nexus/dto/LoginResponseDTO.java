package com.pixelforge.nexus.dto;

import com.pixelforge.nexus.entity.Role;

/**
 * DTO for login response data
 */
public class LoginResponseDTO {

    private String token;
    private String type = "Bearer";
    private UserResponseDTO user;

    public LoginResponseDTO(String token, UserResponseDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }
}