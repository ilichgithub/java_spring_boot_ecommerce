package com.ilich.sb.e_commerce.service.impl;

import com.ilich.sb.e_commerce.model.*; // Importa todas las entidades necesarias
import com.ilich.sb.e_commerce.repository.*; // Importa todos los repositorios
import com.ilich.sb.e_commerce.service.IOrderService;
import jakarta.transaction.Transactional; // Importa desde Jakarta

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final IProductRepository productRepository; // Necesario para actualizar stock

    @Autowired
    public OrderServiceImpl(IOrderRepository orderRepository,
                        IOrderItemRepository orderItemRepository,
                        ICartRepository cartRepository,
                        ICartItemRepository cartItemRepository,
                        IProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional // Asegura que toda la operación (crear pedido, actualizar stock, limpiar carrito) sea atómica
    public Order createOrderFromCart(User user) {
        // 1. Obtener el carrito del usuario
        Cart userCart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + user.getUsername()));

        Set<CartItem> cartItems = userCart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot create an order from an empty cart.");
        }

        // 2. Crear una nueva instancia de Order
        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setOrderDate(LocalDateTime.now()); // Confirmar fecha de orden
        newOrder.setStatus(OrderStatus.PENDING); // Estado inicial

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();

        // 3. Convertir CartItems a OrderItems y actualizar el stock de productos
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer requestedQuantity = cartItem.getQuantity();

            // Verificar stock disponible
            if (product.getStockQuantity() < requestedQuantity) {
                throw new RuntimeException("Not enough stock for product: " + product.getName() + ". Available: " + product.getStockQuantity() + ", Requested: " + requestedQuantity);
            }

            // Crear OrderItem
            OrderItem orderItem = new OrderItem(
                    newOrder, // Asignar el Order recién creado
                    product,
                    requestedQuantity,
                    product.getPrice() // Capturar el precio actual del producto en el momento de la compra
            );
            orderItems.add(orderItem);
            totalOrderAmount = totalOrderAmount.add(orderItem.getSubtotal());

            // Actualizar stock del producto
            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            productRepository.save(product); // Guardar el producto con el stock actualizado
        }

        newOrder.setOrderItems(orderItems); // Establecer los ítems en el pedido
        newOrder.setTotalAmount(totalOrderAmount); // Establecer el total calculado

        // 4. Guardar el nuevo pedido y sus ítems
        Order savedOrder = orderRepository.save(newOrder);
        // orderItemRepository.saveAll(orderItems); // No es necesario si CascadeType.ALL está en Order

        // 5. Limpiar el carrito del usuario
        cartItemRepository.deleteAll(cartItems); // Eliminar todos los ítems del carrito
        userCart.getCartItems().clear(); // Limpiar la colección en memoria
        cartRepository.save(userCart); // Guardar el carrito vacío (o se manejará por cascade si está configurado)

        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public Optional<Order> getOrderByIdAndUser(Long orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user);
    }
}