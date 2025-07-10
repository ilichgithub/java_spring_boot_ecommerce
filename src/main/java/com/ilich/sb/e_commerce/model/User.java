package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.stream.Collectors;


@Entity
@Table(name = "users",uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
}) // Renombra si ya tienes una tabla 'user'
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Generalmente usado como el email o nombre de usuario

    @Column(nullable = false)
    private String password;

    // Relación ManyToMany con Role (un usuario puede tener muchos roles, un rol puede aplicarse a muchos usuarios)
    @ManyToMany(fetch = FetchType.EAGER) // FetchType.EAGER carga los roles inmediatamente con el usuario
    @JoinTable(
        name = "user_roles", // Nombre de la tabla de unión
        joinColumns = @JoinColumn(name = "user_id"), // Columna para la entidad actual (User)
        inverseJoinColumns = @JoinColumn(name = "role_id") // Columna para la entidad asociada (Role)
    )
    private Set<Role> roles = new HashSet<>();

    // --- Constructor sin argumentos (requerido por JPA) ---
    public User() {}

    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // --- Constructor con argumentos ---
    public User(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    // --- Métodos de UserDetails (implementación de Spring Security) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte los roles del usuario en una colección de GrantedAuthority
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() { return true; } // Siempre true para este ejemplo
    @Override
    public boolean isAccountNonLocked() { return true; } // Siempre true para este ejemplo
    @Override
    public boolean isCredentialsNonExpired() { return true; } // Siempre true para este ejemplo
    @Override
    public boolean isEnabled() { return true; } // Siempre true para este ejemplo

    // --- toString, equals, hashCode (buenas prácticas, basados en ID) ---
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
