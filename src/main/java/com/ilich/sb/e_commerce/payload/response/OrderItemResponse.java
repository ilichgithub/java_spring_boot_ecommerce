package com.ilich.sb.e_commerce.payload.response;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Long orderItemId; // ID del OrderItem
    private Long productId;
    private String productName;
    private BigDecimal priceAtPurchase; // Precio al momento de la compra
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItemResponse(Long orderItemId, Long productId, String productName,
                             BigDecimal priceAtPurchase, Integer quantity, BigDecimal subtotal) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.priceAtPurchase = priceAtPurchase;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    // --- Getters y Setters ---
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}