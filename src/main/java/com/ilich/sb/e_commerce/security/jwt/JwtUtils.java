package com.ilich.sb.e_commerce.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ilich.sb.e_commerce.repository.IRevokedTokenRepository;
import com.ilich.sb.e_commerce.service.impl.UserDetailsImpl;

import java.security.Key;
import java.util.Date;

@Component // Marca como un componente de Spring
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${ecommerce.app.jwt.secret}") // Se leerá desde application.properties
    private String jwtSecret;

    @Value("${ecommerce.app.jwt.expiration.ms}") // Se leerá desde application.properties
    private int jwtExpirationMs;
    
    private IRevokedTokenRepository revokedTokenRepository; 
    
    public JwtUtils(IRevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    // Genera el token JWT
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }


    // Nuevo método para generar Access Token directamente desde un username (usado en refresh-token endpoint)
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date()) // Fecha de emisión
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Fecha de expiración
                .signWith(key(), SignatureAlgorithm.HS512) // Firma el token con la clave secreta y algoritmo
                .compact(); // Compacta el token en una cadena JWT
    }

    // Obtiene la clave de firma
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Valida el token JWT
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);

            // 2. Después de la validación estructural, verificar si está en la lista negra
            if (revokedTokenRepository.existsByToken(authToken)) {
                logger.warn("Intento de uso de token JWT revocado: {}", authToken);
                return false; // El token está en la lista negra
            }

            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La cadena de claims JWT está vacía: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene el nombre de usuario desde el token JWT.
     * @param authToken El token JWT.
     * @return El nombre de usuario.
     */
    public String getUserNameFromJwtToken(String authToken) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody().getSubject();
    }


    /**
     * Obtiene la fecha de expiración de un token JWT.
     * Necesario para almacenar el token revocado con su fecha de expiración original.
     * @param authToken El token JWT.
     * @return La fecha de expiración.
     */
    public Date getExpirationDateFromJwtToken(String authToken) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody().getExpiration();
    }
}
