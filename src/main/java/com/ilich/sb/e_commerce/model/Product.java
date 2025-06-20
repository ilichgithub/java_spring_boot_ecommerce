package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2) 
    private BigDecimal price; 

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    // Relación Many-to-One con Category
    @ManyToOne(fetch = FetchType.LAZY) // Carga perezosa, solo se carga Category cuando se necesita
    @JoinColumn(name = "category_id", nullable = false) // FK en la tabla 'product'
    private Category category;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    /*
    // Relación One-to-Many con OrderItem (bidireccional)
    // No es necesario CascadeType.ALL ni orphanRemoval aquí, ya que OrderItem
    // existe como parte de un Order y un Product. Si el producto se elimina,
    // los OrderItems que lo referencian deberían eliminarse con el Order.
    @OneToMany(mappedBy = "product")
    private Set<OrderItem> orderItems = new HashSet<>();
    */


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


}
