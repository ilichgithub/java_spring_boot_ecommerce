package com.ilich.sb.e_commerce.controller;

import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.AddToCartRequestDTO;

import com.ilich.sb.e_commerce.payload.request.UpdateCartItemRequestDTO;
import com.ilich.sb.e_commerce.payload.response.CartItemResponseDTO;
import com.ilich.sb.e_commerce.payload.response.CartResponseDTO;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import com.ilich.sb.e_commerce.service.ICartService;
import com.ilich.sb.e_commerce.service.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Para proteger los endpoints
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

        import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final ICartService cartService;
    private final IUserRepository userRepository; // Necesitamos el userRepository para obtener la entidad User

    @Autowired
    public CartRestController(ICartService cartService, IUserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // Helper para obtener el usuario autenticado
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Asegúrate de que el usuario esté autenticado y sea de tipo UserDetailsImpl
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new RuntimeException("User not authenticated."); // Esto no debería ocurrir con @PreAuthorize
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
    }

    // Mapeo de CartItem a CartItemResponse
    private CartItemResponseDTO mapToCartItemResponse(CartItem cartItem) {
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
    private CartResponseDTO mapToCartResponse(Set<CartItem> cartItems, Long cartId, BigDecimal total) {
        Set<CartItemResponseDTO> itemResponses = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toSet());
        return new CartResponseDTO(cartId, itemResponses, total);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Solo usuarios autenticados pueden ver su carrito
    public ResponseEntity<CartResponseDTO> getCart() {
        User currentUser = getCurrentAuthenticatedUser();
        Set<CartItem> cartItems = cartService.getCartItems(currentUser);
        BigDecimal total = cartService.getCartTotal(currentUser);
        Long cartId = cartService.getCartByUser(currentUser).getId(); // Obtener el ID del carrito

        return ResponseEntity.ok(mapToCartResponse(cartItems, cartId, total));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CartItemResponseDTO> addProductToCart(@Valid @RequestBody AddToCartRequestDTO request) {
        User currentUser = getCurrentAuthenticatedUser();
        CartItem cartItem = cartService.addProductToCart(currentUser, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(mapToCartItemResponse(cartItem), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CartItemResponseDTO> updateProductQuantity(@Valid @RequestBody UpdateCartItemRequestDTO request) {
        User currentUser = getCurrentAuthenticatedUser();
        // Si la cantidad es 0, el servicio eliminará el ítem y devolverá null.
        CartItem updatedCartItem = cartService.updateProductQuantity(currentUser, request.getProductId(), request.getQuantity());

        if (updatedCartItem == null) {
            // Si el ítem fue eliminado (cantidad = 0), puedes devolver 204 No Content o un mensaje
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(mapToCartItemResponse(updatedCartItem));
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable Long productId) {
        User currentUser = getCurrentAuthenticatedUser();
        cartService.removeProductFromCart(currentUser, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> clearCart() {
        User currentUser = getCurrentAuthenticatedUser();
        cartService.clearCart(currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}