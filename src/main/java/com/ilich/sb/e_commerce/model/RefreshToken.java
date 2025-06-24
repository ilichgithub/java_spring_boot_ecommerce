package com.ilich.sb.e_commerce.model;


import jakarta.persistence.*;
import java.time.Instant; // Usaremos Instant para fechas para mayor precisión y compatibilidad con JPA

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El token en sí, debe ser una cadena aleatoria y única
    @Column(nullable = false, unique = true, length = 255) // Tamaño suficiente para UUID u otra cadena
    private String token;

    // Relación con el usuario: Un usuario puede tener varios refresh tokens (si se permite iniciar sesión en múltiples dispositivos)
    // O OneToOne si solo permites un refresh token activo por usuario a la vez.
    // Para simplificar, empezaremos con ManyToOne, lo que permite múltiples sesiones.
    @ManyToOne(optional = false) // Un RefreshToken siempre debe estar asociado a un User
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Fecha de expiración del Refresh Token (más larga que la del Access Token)
    @Column(nullable = false)
    private Instant expiryDate;

    // --- Constructor vacío (necesario para JPA) ---
    public RefreshToken() {
    }

    // --- Constructor con argumentos ---
    public RefreshToken(String token, User user, Instant expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    // --- Getters y Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
               "id=" + id +
               ", token='" + token.substring(0, Math.min(token.length(), 20)) + "...'" + // Truncar para logs
               ", userId=" + (user != null ? user.getId() : "null") +
               ", expiryDate=" + expiryDate +
               '}';
    }
}