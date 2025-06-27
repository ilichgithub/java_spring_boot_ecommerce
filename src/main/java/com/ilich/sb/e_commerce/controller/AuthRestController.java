package com.ilich.sb.e_commerce.controller;

import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.LoginRequestDTO;
import com.ilich.sb.e_commerce.payload.request.RegisterRequestDTO;
import com.ilich.sb.e_commerce.payload.request.TokenRefreshRequestDTO;
import com.ilich.sb.e_commerce.payload.response.JwtResponseDTO;
import com.ilich.sb.e_commerce.payload.response.MessageResponseDTO;
import com.ilich.sb.e_commerce.payload.response.TokenRefreshResponseDTO;
import com.ilich.sb.e_commerce.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/api/auth") // Ruta base para los endpoints de autenticación
@Tag(name = "Auth", description = "Operaciones relacionadas a la autenticacion")
public class AuthRestController {

    @Value("${ecommerce.app.jwt.refresh.expiration.ms}") // Duración del Refresh Token
    private Long refreshTokenDurationMs;

    private final IUserService iUserService;

    public AuthRestController(IUserService iUserService) { 
        this.iUserService = iUserService;
    }

    @Operation(summary = "Iniciar sesión de usuario", description = "Autentica a un usuario y devuelve un Access Token y Refresh Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        
        User user = new User(loginRequest.getUsername(),
                             loginRequest.getPassword(), 
                             new HashSet<>()); 

        JwtResponseDTO obj = iUserService.authenticateUser(user);

        // Puedes enviar el refresh token en una HttpOnly cookie para mayor seguridad.
        // Esto es una buena práctica para prevenir ataques XSS.
        ResponseCookie jwtRefreshCookie = ResponseCookie.from("refreshtoken", obj.getRefreshToken())
            .httpOnly(true)
            .secure(true) // Solo enviar en HTTPS
            .path("/api/auth/refreshtoken") // Solo accesible en el endpoint de refresh
            .maxAge(refreshTokenDurationMs / 1000) // Duración en segundos
            .build();

        // Envía el Access Token en el cuerpo y el Refresh Token en la cookie
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
            .body(new JwtResponseDTO(obj.getAccessToken(),obj.getRefreshToken(), obj.getId(), obj.getUsername(), obj.getRoles()));
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Registra una nueva cuenta de usuario en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Nombre de usuario ya en uso o solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        
        User user = new User(registerRequest.getUsername(),
                             registerRequest.getPassword(), 
                             new HashSet<>());

        String response = iUserService.registerUser(user);
        return response.toUpperCase().contains("ERROR") ?
                ResponseEntity.badRequest().body(new MessageResponseDTO(response)) :
                ResponseEntity.ok(new MessageResponseDTO(response));
    }

    /**
     * Nuevo endpoint para refrescar el Access Token usando el Refresh Token.
     * @param request La solicitud HTTP para obtener el Refresh Token de la cookie o body.
     * @param refreshRequest El DTO que contiene el refresh token.
     * @return Nuevo Access Token y Refresh Token.
     */

    @Operation(summary = "Refrescar Access Token", description = "Usa un Refresh Token válido para obtener un nuevo Access Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refrescados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o refresh token ausente"),
            @ApiResponse(responseCode = "403", description = "Refresh token expirado o no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {

        JwtResponseDTO obj = iUserService.refreshToken(refreshRequest.getRefreshToken());

        ResponseCookie jwtRefreshCookie = ResponseCookie.from("refreshtoken", obj.getRefreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/api/auth/refreshtoken")
            .maxAge(refreshTokenDurationMs / 1000)
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
            .body(new TokenRefreshResponseDTO(obj.getAccessToken(), obj.getRefreshToken()));
                        
    }

    /**
     * Endpoint para manejar el logout.
     * Requiere que el usuario esté autenticado para solicitar su propio logout.
     * El token se añade a una lista negra para prevenir su reutilización.
     * @param request La petición HTTP, usada para extraer el token JWT.
     * @return ResponseEntity con un mensaje de éxito.
     */

    @Operation(summary = "Cerrar sesión de usuario", description = "Invalida el Access Token actual y el Refresh Token asociado, cerrando la sesión del usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout exitoso"),
            @ApiResponse(responseCode = "400", description = "Token no proporcionado o ya expirado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String response = iUserService.logoutUser(request);
        return response.toUpperCase().contains("ERROR") ? 
            ResponseEntity.badRequest().body(new MessageResponseDTO(response)) : 
            ResponseEntity.ok(new MessageResponseDTO(response));
    }
}
