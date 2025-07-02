package com.ilich.sb.e_commerce.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCartItemRequestDTO {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    // La cantidad puede ser 0 para indicar eliminación en la actualización
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    // Constructor vacío
    public UpdateCartItemRequestDTO() {
    }

    public UpdateCartItemRequestDTO(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters y Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
