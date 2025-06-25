package com.ilich.sb.e_commerce.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ilich.sb.e_commerce.repository.IRevokedTokenRepository;
import com.ilich.sb.e_commerce.service.IRefreshTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Component
public class TokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    @Autowired
    private IRefreshTokenService refreshTokenService;

    @Autowired
    private IRevokedTokenRepository revokedTokenRepository;

    /**
     * Limpia los Refresh Tokens expirados de la base de datos.
     * Se ejecuta cada 24 horas (ajusta el cron según necesites).
     */
    @Scheduled(cron = "0 0 0 * * ?") // Ejecuta a medianoche todos los días
    //@Scheduled(cron = "0 */5 * * * ?") // ¡CAMBIO AQUÍ!
    //@Scheduled(fixedRate = 86400000) // O cada 24 horas desde el inicio de la app
    @Transactional // ¡AÑADE ESTA ANOTACIÓN!
    public void cleanupExpiredRefreshTokens() {
        logger.info("Iniciando limpieza de Refresh Tokens expirados...");
        refreshTokenService.cleanExpiredRefreshTokens();
        logger.info("Limpieza de Refresh Tokens expirados completada.");
    }

    /**
     * Opcional: Limpia los Access Tokens revocados que ya expiraron naturalmente.
     * Se ejecuta cada 24 horas.
     */
    @Scheduled(cron = "0 30 0 * * ?") // Ejecuta 30 minutos después de medianoche
    //@Scheduled(cron = "30 */5 * * * ?") // Ejecuta 30 segundos después de cada 5 minutos
    @Transactional // ¡AÑADE ESTA ANOTACIÓN!
    public void cleanupExpiredRevokedAccessTokens() {
        logger.info("Iniciando limpieza de Access Tokens revocados y expirados...");
        // Implementar un método en RevokedTokenRepository para esto si no lo tienes
        // Ejemplo: revokedTokenRepository.deleteByExpiryDateBefore(new Date());
        // Necesitarás añadir `void deleteByExpiryDateBefore(Date date);` en RevokedTokenRepository
        // y llamarlo aquí.
        revokedTokenRepository.deleteByExpiryDateBefore(new Date()); // Asegúrate de que el método exista en tu repo

        logger.info("Limpieza de Access Tokens revocados y expirados completada.");
    }
}