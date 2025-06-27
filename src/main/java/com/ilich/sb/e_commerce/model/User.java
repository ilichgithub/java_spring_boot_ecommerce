package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

// Implementar UserDetails es una práctica común para integrar con Spring Security
// aunque no es estrictamente obligatorio si usas un servicio de usuario personalizado
// pero lo haremos aquí para simplificar.
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

/*

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user") // Nombre de la tabla en minúsculas y singular
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // Ej: "ROLE_USER", "ROLE_ADMIN"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    /* 
    // Relación One-to-Many con Order
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders = new HashSet<>();
    
    // Constructor vacío (requerido por JPA)
    public User() {}

    // Constructor para facilidad (sin ID, created_at, updated_at)
    public User(String username, String passwordHash, String email, String firstName, String lastName, String address, String phoneNumber, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    // Callbacks para manejar fechas automáticamente
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    // No setter para createdAt, se maneja por @PrePersist
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // No setter para updatedAt, se maneja por @PreUpdate
    /*
    public Set<Order> getOrders() { return orders; }
    public void setOrders(Set<Order> orders) { this.orders = orders; }

    // Métodos de conveniencia para añadir/remover pedidos
    public void addOrder(Order order) {
        this.orders.add(order);
        order.setUser(this);
    }
    public void removeOrder(Order order) {
        this.orders.remove(order);
        order.setUser(null);
    }
        
}
*/