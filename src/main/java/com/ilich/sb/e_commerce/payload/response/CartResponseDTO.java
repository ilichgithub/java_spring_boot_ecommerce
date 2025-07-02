package com.ilich.sb.e_commerce.payload.response;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class CartResponseDTO {
    private Long cartId;
    private Set<CartItemResponseDTO> items = new HashSet<>();
    private BigDecimal total;

    // Constructor vacío
    public CartResponseDTO() {
    }

    // Constructor completo
    public CartResponseDTO(Long cartId, Set<CartItemResponseDTO> items, BigDecimal total) {
        this.cartId = cartId;
        this.items = items;
        this.total = total;
    }

    // Getters y Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Set<CartItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(Set<CartItemResponseDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}