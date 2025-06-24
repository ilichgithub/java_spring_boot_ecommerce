package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.response.JwtResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface IUserService {

    JwtResponseDTO authenticateUser(User user);

    String registerUser(User user);

    String logoutUser(HttpServletRequest request);

    JwtResponseDTO refreshToken(String requestRefreshToken);

}
