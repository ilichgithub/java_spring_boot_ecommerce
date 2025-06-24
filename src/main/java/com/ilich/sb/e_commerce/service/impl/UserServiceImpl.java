package com.ilich.sb.e_commerce.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.config.jwt.AuthTokenFilter;
import com.ilich.sb.e_commerce.config.jwt.JwtUtils;
import com.ilich.sb.e_commerce.config.service.UserDetailsImpl;
import com.ilich.sb.e_commerce.dto.JwtResponseDTO;
import com.ilich.sb.e_commerce.exception.TokenRefreshException;
import com.ilich.sb.e_commerce.model.RefreshToken;
import com.ilich.sb.e_commerce.model.RevokedToken;
import com.ilich.sb.e_commerce.model.Role;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.IRevokedTokenRepository;
import com.ilich.sb.e_commerce.repository.IRoleRepository;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import com.ilich.sb.e_commerce.service.IRefreshTokenService;
import com.ilich.sb.e_commerce.service.IUserService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class); // Agrega un logger

    private final IRefreshTokenService refreshTokenService; // Inyecta el nuevo servicio
    private final AuthenticationManager authenticationManager;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IRevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(
            AuthenticationManager authenticationManager,
            IUserRepository userRepository,
            IRoleRepository roleRepository,
            IRevokedTokenRepository revokedTokenRepository,
            IRefreshTokenService refreshTokenService,
            PasswordEncoder encoder,
            JwtUtils jwtUtils
        ) {
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.revokedTokenRepository = revokedTokenRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public String registerUser(User user) {
        // Verifica si el nombre de usuario ya existe
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Error: ¡El nombre de usuario ya está en uso!";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        // Asigna el rol por defecto (ej. "ROLE_USER")
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        if (!userRole.isPresent()) {
            return "Error: Rol 'ROLE_USER' no encontrado.";
        }
        roles.add(userRole.get());
        user.setRoles(roles);
        userRepository.save(user);
        return "Usuario registrado exitosamente!";
    }

    @Override
    public JwtResponseDTO authenticateUser(User user) {
        // Autentica al usuario usando el AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        // Si la autenticación es exitosa, establece el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Genera el token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);
        // Obtiene los detalles del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Generar Refresh Token (vida larga) y guardarlo en la BD
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userRepository.findById(userDetails.getId()).get());
        // Extrae los roles del usuario
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponseDTO (
            jwt,
            refreshToken.getToken(),
            userDetails.getId(),
            userDetails.getUsername(),
            roles);
    }

    @Override
    public String logoutUser(HttpServletRequest request) {
        // 1. Obtener el token JWT de la cabecera de la petición
        String jwt = AuthTokenFilter.parseJwt(request); // Reutilizamos el método de parseo de AuthTokenFilter

        if (jwt != null) { // El access token actual puede ser nulo si solo se envía el refresh token
             // Opcional: Añadir el access token a la blacklist si es de corta duración y aún no expira
            try {
                // 2. Obtener la fecha de expiración del token
                Date expiryDate = jwtUtils.getExpirationDateFromJwtToken(jwt);
                // 3. Crear una entrada en la tabla de tokens revocados
                RevokedToken revokedToken = new RevokedToken(jwt, expiryDate);
                revokedTokenRepository.save(revokedToken);
                logger.info("Access Token revocado exitosamente: {}", jwt.substring(0, Math.min(jwt.length(), 50)) + "...");
            } catch (Exception e) {
                logger.warn("No se pudo revocar el Access Token (ya expirado o inválido): {}", e.getMessage());
            }
        }

        // Recuperar el refresh token de la cookie (si se envía así) o del body (si se envía así)
        String refreshTokenFromCookie = getRefreshTokenFromCookies(request); // Necesitas este método
        if (refreshTokenFromCookie != null) {
            // Eliminar el refresh token de la base de datos
            refreshTokenService.findByToken(refreshTokenFromCookie)
                .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
            logger.info("Refresh Token de usuario revocado exitosamente.");
        } else {
             logger.warn("No se encontró Refresh Token en la solicitud de logout.");
        }

        // 4. Limpiar el contexto de seguridad actual (opcional pero buena práctica)
        SecurityContextHolder.clearContext();

        return "¡Logout exitoso! Tu token ha sido invalidado.";
    }

    @Override
    public JwtResponseDTO refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration) // Verifica si ha expirado
                .map(RefreshToken::getUser) // Obtiene el usuario asociado
                .map(user -> {
                    // Genera un nuevo Access Token
                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUsername());
                    refreshTokenService.deleteByUserId(user.getId()); // Elimina el viejo (o solo el que se usó)

                    // Opcional: Generar un nuevo Refresh Token y revocar el anterior
                    // Esto implementa rotación de refresh tokens.
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

                    return new JwtResponseDTO (
                        newAccessToken,
                        newRefreshToken.getToken(),
                        0L,
                        "",
                        null);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token no encontrado en la base de datos!"));
    }

    // Método auxiliar para obtener el Refresh Token de las cookies
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshtoken")) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
