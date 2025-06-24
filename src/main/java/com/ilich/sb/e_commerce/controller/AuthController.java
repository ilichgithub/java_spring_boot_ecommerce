package com.ilich.sb.e_commerce.controller;

import com.ilich.sb.e_commerce.dto.LoginRequestDTO;
import com.ilich.sb.e_commerce.dto.MessageResponseDTO;
import com.ilich.sb.e_commerce.dto.RegisterRequestDTO;
import com.ilich.sb.e_commerce.model.User;

import com.ilich.sb.e_commerce.service.IUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/api/auth") // Ruta base para los endpoints de autenticación
public class AuthController {

    private final IUserService iUserService;

    public AuthController(
                          IUserService iUserService
                          ) { 
        this.iUserService = iUserService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        
        User user = new User(loginRequest.getUsername(),
                             loginRequest.getPassword(), 
                             new HashSet<>()); 

        return ResponseEntity.ok(iUserService.authenticateUser(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        
        User user = new User(registerRequest.getUsername(),
                             registerRequest.getPassword(), 
                             new HashSet<>()); 

        return ResponseEntity.ok(
            new MessageResponseDTO(
                iUserService.registerUser(user)
            )
        );
    }

    /**
     * Endpoint para manejar el logout.
     * Requiere que el usuario esté autenticado para solicitar su propio logout.
     * El token se añade a una lista negra para prevenir su reutilización.
     * @param request La petición HTTP, usada para extraer el token JWT.
     * @return ResponseEntity con un mensaje de éxito.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String response = iUserService.logoutUser(request);
        return response.toUpperCase().contains("ERROR") ? 
            ResponseEntity.badRequest().body(new MessageResponseDTO(response)) : 
            ResponseEntity.ok(new MessageResponseDTO(response));
    }
}