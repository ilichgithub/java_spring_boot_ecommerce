package com.ilich.sb.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ilich.sb.e_commerce.model.RevokedToken;

@Repository
public interface IRevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    // Para verificar si un token ya está en la lista negra
    boolean existsByToken(String token);

    // Opcional: para limpiar tokens expirados de la lista negra periódicamente
    // List<RevokedToken> findByExpiryDateBefore(Date date);
}