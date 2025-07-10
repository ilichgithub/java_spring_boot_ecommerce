package com.ilich.sb.e_commerce.payload.response;

import com.ilich.sb.e_commerce.model.OrderStatus; // Importar el enum OrderStatus
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class OrderResponse {
    private Long orderId;
    private Long userId; // Opcional: si quieres exponer el ID del usuario
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status; // Usar el enum OrderStatus
    private Set<OrderItemResponse> items; // Lista de ítems del pedido

    public OrderResponse(Long orderId, Long userId, LocalDateTime orderDate,
                         BigDecimal totalAmount, OrderStatus status, Set<OrderItemResponse> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = items;
    }

    // --- Getters y Setters ---
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Set<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(Set<OrderItemResponse> items) {
        this.items = items;
    }
}