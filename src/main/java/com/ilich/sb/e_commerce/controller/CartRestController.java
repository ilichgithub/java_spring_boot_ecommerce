package com.ilich.sb.e_commerce.controller;

import com.ilich.sb.e_commerce.mapper.CartMapper;
import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.AddToCartRequestDTO;

import com.ilich.sb.e_commerce.payload.request.UpdateCartItemRequestDTO;
import com.ilich.sb.e_commerce.payload.response.CartItemResponseDTO;
import com.ilich.sb.e_commerce.payload.response.CartResponseDTO;
import com.ilich.sb.e_commerce.service.ICartService;
import com.ilich.sb.e_commerce.util.UserUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final ICartService cartService;
    private final UserUtil userUtil;

    @Autowired
    public CartRestController(ICartService cartService, UserUtil userUtil) {
        this.cartService = cartService;
        this.userUtil = userUtil;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Solo usuarios autenticados pueden ver su carrito
    public ResponseEntity<CartResponseDTO> getCart() {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        Set<CartItem> cartItems = cartService.getCartItems(currentUser);
        BigDecimal total = cartService.getCartTotal(currentUser);
        Long cartId = cartService.getCartByUser(currentUser).getId(); // Obtener el ID del carrito

        return ResponseEntity.ok(CartMapper.mapToCartResponse(cartItems, cartId, total));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CartItemResponseDTO> addProductToCart(@Valid @RequestBody AddToCartRequestDTO request) {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        CartItem cartItem = cartService.addProductToCart(currentUser, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(CartMapper.mapToCartItemResponse(cartItem), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CartItemResponseDTO> updateProductQuantity(@Valid @RequestBody UpdateCartItemRequestDTO request) {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        // Si la cantidad es 0, el servicio eliminará el ítem y devolverá null.
        CartItem updatedCartItem = cartService.updateProductQuantity(currentUser, request.getProductId(), request.getQuantity());

        if (updatedCartItem == null) {
            // Si el ítem fue eliminado (cantidad = 0), puedes devolver 204 No Content o un mensaje
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(CartMapper.
                mapToCartItemResponse(updatedCartItem));
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable Long productId) {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        cartService.removeProductFromCart(currentUser, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> clearCart() {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        cartService.clearCart(currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}