package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // Enum para el estado del pedido (PENDING, COMPLETED, CANCELLED, etc.)

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    // Constructor vacío (necesario para JPA)
    public Order() {
        this.orderDate = LocalDateTime.now(); // Establecer la fecha actual al crear
        this.status = OrderStatus.PENDING; // Estado inicial por defecto
        this.totalAmount = BigDecimal.ZERO; // Inicializar total
    }

    // Constructor para facilidad en la creación de nuevos pedidos
    public Order(User user, BigDecimal totalAmount, OrderStatus status) {
        this(); // Llama al constructor vacío para inicializar fecha y estado por defecto
        this.user = user;
        this.totalAmount = totalAmount; // Este total podría calcularse en el servicio
        this.status = status;
    }

    // Constructor para facilidad en la creación de nuevos pedidos
    public Order(Long id, User user, BigDecimal totalAmount, OrderStatus status) {
        this(); // Llama al constructor vacío para inicializar fecha y estado por defecto
        this.id = id;
        this.user = user;
        this.totalAmount = totalAmount; // Este total podría calcularse en el servicio
        this.status = status;
    }

    // --- Getters y Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // --- Métodos de Conveniencia ---
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        this.orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    // Métodos para calcular el total desde los items (útil para consistencia)
    public BigDecimal calculateTotalAmount() {
        return this.orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}