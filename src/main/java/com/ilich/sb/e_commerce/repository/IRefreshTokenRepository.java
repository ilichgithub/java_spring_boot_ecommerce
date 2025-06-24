package com.ilich.sb.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ilich.sb.e_commerce.model.RefreshToken;
import com.ilich.sb.e_commerce.model.User;

import java.util.Optional;
import java.time.Instant; // Para limpiar por fecha

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Buscar un RefreshToken por su valor de token
    Optional<RefreshToken> findByToken(String token);

    // Eliminar RefreshTokens asociados a un usuario específico (útil para logout global del usuario)
    int deleteByUser(User user);

    // Opcional: Eliminar RefreshTokens expirados (para limpieza periódica)
    void deleteByExpiryDateBefore(Instant now);
}