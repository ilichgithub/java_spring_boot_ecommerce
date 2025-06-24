package com.ilich.sb.e_commerce.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.exception.TokenRefreshException;
import com.ilich.sb.e_commerce.model.RefreshToken;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.IRefreshTokenRepository;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import com.ilich.sb.e_commerce.service.IRefreshTokenService;

import jakarta.transaction.Transactional; // Para operaciones transaccionales

import java.time.Instant;
import java.util.Optional;
import java.util.UUID; // Para generar el token aleatorio

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    @Value("${ecommerce.app.jwt.refresh.expiration.ms}") // Duración del Refresh Token
    private Long refreshTokenDurationMs;

    @Autowired
    private IRefreshTokenRepository refreshTokenRepository;

    @Autowired
    private IUserRepository userRepository;

    /**
     * Busca un RefreshToken por su valor.
     * @param token El valor del Refresh Token.
     * @return Un Optional que contiene el RefreshToken si se encuentra.
     */
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Crea y guarda un nuevo RefreshToken para un usuario dado.
     * Si el usuario ya tiene un refresh token, se podría optar por revocar el antiguo
     * o generar uno nuevo y que el antiguo expire. Para este ejemplo, generamos uno nuevo.
     *
     * @param user El usuario para el que se crea el RefreshToken.
     * @return El RefreshToken creado y guardado.
     */
    @Override
    public RefreshToken createRefreshToken(User user) {
        // Opcional: Si quieres que un usuario solo tenga un RefreshToken activo a la vez:
        // refreshTokenRepository.deleteByUser(user); // Revoca el anterior si existe

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString()); // Genera una cadena aleatoria y única

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    /**
     * Verifica si un RefreshToken ha expirado.
     * @param token El RefreshToken a verificar.
     * @return El RefreshToken si no ha expirado.
     * @throws TokenRefreshException Si el token ha expirado.
     */
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token); // Elimina el token expirado de la BD
            throw new TokenRefreshException(token.getToken(),
                "Refresh token ha expirado. Por favor, inicia sesión de nuevo.");
        }
        return token;
    }

    /**
     * Elimina un RefreshToken de la base de datos por su usuario.
     * Usado en el logout para revocar todos los refresh tokens de un usuario.
     * @param user El usuario cuyos tokens se eliminarán.
     * @return Número de tokens eliminados.
     */
    @Override
    @Transactional // Asegura que la operación de eliminación se complete correctamente
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }

    /**
     * Elimina todos los RefreshTokens expirados de la base de datos.
     * Debería ser llamado por un trabajo programado (scheduler).
     */
    @Transactional
    @Override
    public void cleanExpiredRefreshTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}