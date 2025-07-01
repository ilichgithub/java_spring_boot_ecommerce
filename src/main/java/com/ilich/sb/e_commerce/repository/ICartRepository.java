package com.ilich.sb.e_commerce.repository;

import com.ilich.sb.e_commerce.model.Cart;
import com.ilich.sb.e_commerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Long> {
    // Método para encontrar un carrito por el usuario al que pertenece
    Optional<Cart> findByUser(User user);
}
