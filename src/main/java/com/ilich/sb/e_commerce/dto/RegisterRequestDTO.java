package com.ilich.sb.e_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres.")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 40, message = "La contraseña debe tener entre 6 y 40 caracteres.")
    private String password;

    // Puedes añadir otros campos como email, si tu username es solo un nombre de usuario
    // @NotBlank(message = "El email es obligatorio.")
    // @Email(message = "El formato del email no es válido.")
    // private String email;

    // Para un registro simple, los roles se asignan por defecto (ej. ROLE_USER).
    // Si quieres permitir que el cliente envíe roles, puedes incluirlos aquí:
    // private Set<String> roles; // ej. ["admin", "user"]

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Si añades email o roles, incluye sus getters y setters
}
