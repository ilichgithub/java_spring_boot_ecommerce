package com.ilich.sb.e_commerce.model;
/*
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
@Table(name = "order_table") // 'order' es una palabra reservada en SQL, se usa 'order_table' o 'app_order'
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Many-to-One con User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK en la tabla 'order'
    private User user;

    @Column(name = "order_date", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // Ej: PENDING, PROCESSED, SHIPPED, DELIVERED, CANCELED

    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // Ej: PENDING, PAID, FAILED, REFUNDED

    // Relación One-to-Many con OrderItem
    // CascadeType.ALL y orphanRemoval = true aseguran que si se borra un Order, sus OrderItems también lo hagan.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();


    // Callbacks para manejar fechas automáticamente
    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
    }

    // Métodos de conveniencia para añadir/remover ítems
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    public void removeOrderItem(OrderItem orderItem) {
        this.orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }
}

 */