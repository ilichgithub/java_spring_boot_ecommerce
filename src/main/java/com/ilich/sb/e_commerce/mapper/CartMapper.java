package com.ilich.sb.e_commerce.mapper;

import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.payload.response.CartItemResponseDTO;
import com.ilich.sb.e_commerce.payload.response.CartResponseDTO;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public class CartMapper {

    private CartMapper() {
    }

    // Mapeo de CartItem a CartItemResponse
    public static CartItemResponseDTO mapToCartItemResponse(CartItem cartItem) {
        return new CartItemResponseDTO(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                cartItem.getSubtotal()
        );
    }

    // Mapeo de Cart a CartResponse
    public static CartResponseDTO mapToCartResponse(Set<CartItem> cartItems, Long cartId, BigDecimal total) {
        Set<CartItemResponseDTO> itemResponses = cartItems.stream()
                .map(CartMapper::mapToCartItemResponse)
                .collect(Collectors.toSet());
        return new CartResponseDTO(cartId, itemResponses, total);
    }
}
