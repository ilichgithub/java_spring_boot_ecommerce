package com.ilich.sb.e_commerce.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenRefreshRequestDTO {
    @NotBlank(message = "El refresh token es obligatorio.")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}