package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ilich.sb.e_commerce.dto.ProductDTO;

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
    @ManyToOne() // Carga perezosa, solo se carga Category cuando se necesita fetch = FetchType.LAZY
    @JoinColumn(name = "category_id", nullable = false) // FK en la tabla 'product'
    @JsonManagedReference
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

    public Product(ProductDTO productDTO){
        this.id = productDTO.getId();
        this.name = productDTO.getName();
        this.description = productDTO.getDescription();
        this.price = productDTO.getPrice();
        this.stockQuantity = productDTO.getStockQuantity();
        this.imageUrl = productDTO.getImageUrl();
        this.category = new Category(productDTO.getCategoryDTO());
    }
    


}
