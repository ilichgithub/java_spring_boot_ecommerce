package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.dto.JwtResponseDTO;
import com.ilich.sb.e_commerce.model.User;

import jakarta.servlet.http.HttpServletRequest;

public interface IUserService {

    JwtResponseDTO authenticateUser(User user);

    String registerUser(User user);

    String logoutUser(HttpServletRequest request);

}
