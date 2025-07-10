package com.ilich.sb.e_commerce.repository;

import com.ilich.sb.e_commerce.model.Order;
import com.ilich.sb.e_commerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Encuentra todos los ítems de un pedido específico.
    List<OrderItem> findByOrder(Order order);

    // Puedes añadir más métodos personalizados si lo necesitas.
    // Por ejemplo, para encontrar un OrderItem por un producto específico dentro de un pedido:
    // Optional<OrderItem> findByOrderAndProduct(Order order, Product product);
}