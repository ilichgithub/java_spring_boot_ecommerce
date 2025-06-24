package com.ilich.sb.e_commerce.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.config.jwt.AuthTokenFilter;
import com.ilich.sb.e_commerce.config.jwt.JwtUtils;
import com.ilich.sb.e_commerce.config.service.UserDetailsImpl;
import com.ilich.sb.e_commerce.controller.AuthController;
import com.ilich.sb.e_commerce.dto.JwtResponseDTO;
import com.ilich.sb.e_commerce.model.RevokedToken;
import com.ilich.sb.e_commerce.model.Role;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.IRevokedTokenRepository;
import com.ilich.sb.e_commerce.repository.IRoleRepository;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import com.ilich.sb.e_commerce.service.IUserService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class); // Agrega un logger


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
            PasswordEncoder encoder,
            JwtUtils jwtUtils
        ) {
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
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
        // Extrae los roles del usuario
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponseDTO (jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            roles);
    }

    @Override
    public String logoutUser(HttpServletRequest request) {
        // 1. Obtener el token JWT de la cabecera de la petición
        String jwt = AuthTokenFilter.parseJwt(request); // Reutilizamos el método de parseo de AuthTokenFilter

        if (jwt == null) {
            return "Error: No se proporcionó token JWT.";
        }

        try {
            // 2. Obtener la fecha de expiración del token
            Date expiryDate = jwtUtils.getExpirationDateFromJwtToken(jwt);

            // 3. Crear una entrada en la tabla de tokens revocados
            RevokedToken revokedToken = new RevokedToken(jwt, expiryDate);
            revokedTokenRepository.save(revokedToken);

            // 4. Limpiar el contexto de seguridad actual (opcional pero buena práctica)
            SecurityContextHolder.clearContext();

            logger.info("Token JWT revocado exitosamente: {}", jwt.substring(0, Math.min(jwt.length(), 50)) + "...");

            return "¡Logout exitoso! Tu token ha sido invalidado.";

        } catch (ExpiredJwtException e) {
            logger.warn("Intento de logout con un token ya expirado: {}", e.getMessage());
            // A pesar de que el token ya está expirado, podemos registrarlo para el caso de logout
            // pero no es estrictamente necesario, ya que el sistema lo rechazaría de todas formas.
            return "Error: El token ya ha expirado.";
        } catch (Exception e) {
            logger.error("Error al revocar el token JWT: {}", e.getMessage());
            return "Error interno al procesar el logout.";
        }
    }

}
