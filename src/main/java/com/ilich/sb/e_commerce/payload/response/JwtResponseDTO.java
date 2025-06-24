package com.ilich.sb.e_commerce.payload.response;

import java.util.List;

public class JwtResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer"; // Tipo de token, por convención "Bearer"
    private Long id;
    private String username;
    private List<String> roles;

    public JwtResponseDTO(String accessToken, String refreshToken, Long id, String username, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    // --- Getters y Setters ---
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // --- Getters y Setters ---
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}