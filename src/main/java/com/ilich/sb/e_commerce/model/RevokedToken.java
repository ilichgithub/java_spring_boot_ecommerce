package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500) // JWTs pueden ser largos
    private String token;

    @Column(nullable = false)
    private Date expiryDate; // Fecha de expiración original del token

    public RevokedToken() {
    }

    public RevokedToken(String token, Date expiryDate) {
        this.token = token;
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

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "RevokedToken{" +
               "id=" + id +
               ", token='" + token.substring(0, Math.min(token.length(), 50)) + "...'" + // Truncar para logs
               ", expiryDate=" + expiryDate +
               '}';
    }
}
