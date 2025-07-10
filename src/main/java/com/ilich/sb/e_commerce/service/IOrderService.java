package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.Order;
import com.ilich.sb.e_commerce.model.User;

import java.util.List;
import java.util.Optional;

public interface IOrderService {

    /**
     * Crea un nuevo pedido para un usuario a partir de los ítems de su carrito.
     * Reduce el stock de los productos y vacía el carrito.
     *
     * @param user El usuario autenticado que realiza el pedido.
     * @return El objeto Order recién creado.
     */
    Order createOrderFromCart(User user);

    /**
     * Obtiene el historial de pedidos de un usuario específico.
     *
     * @param user El usuario cuyos pedidos se quieren obtener.
     * @return Una lista de objetos Order.
     */
    List<Order> getOrdersByUser(User user);

    /**
     * Obtiene los detalles de un pedido específico por su ID para un usuario dado.
     *
     * @param orderId El ID del pedido.
     * @param user El usuario que intenta acceder al pedido (para seguridad).
     * @return Un Optional que contiene el objeto Order si se encuentra y pertenece al usuario.
     */
    Optional<Order> getOrderByIdAndUser(Long orderId, User user);

    // Puedes añadir más métodos según tus necesidades futuras, como:
    // Order updateOrderStatus(Long orderId, OrderStatus newStatus); // Para administradores
    // void cancelOrder(Long orderId, User user);
}
