package com.ilich.sb.e_commerce.repository;

import com.ilich.sb.e_commerce.model.Cart;
import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    // Método para encontrar un CartItem específico en un carrito para un producto dado
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // Método para encontrar todos los ítems de un carrito específico
    Set<CartItem> findByCart(Cart cart);
}
