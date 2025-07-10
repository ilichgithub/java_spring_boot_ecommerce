package com.ilich.sb.e_commerce.repository;

import com.ilich.sb.e_commerce.model.Order;
import com.ilich.sb.e_commerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {

    // Encuentra todos los pedidos de un usuario específico.
    // Útil para mostrar el historial de pedidos de un cliente.
    List<Order> findByUser(User user);

    // Encuentra un pedido específico por su ID y el usuario al que pertenece.
    // Esto añade una capa de seguridad para asegurar que un usuario solo pueda acceder a sus propios pedidos.
    Optional<Order> findByIdAndUser(Long id, User user);

    // Puedes añadir métodos de búsqueda personalizados si los necesitas, por ejemplo:
    // List<Order> findByStatus(OrderStatus status);
    // List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}