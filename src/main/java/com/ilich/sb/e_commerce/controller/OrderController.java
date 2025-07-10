// src/main/java/com/ilich/sb/e_commerce.controller/OrderController.java
package com.ilich.sb.e_commerce.controller;

import com.ilich.sb.e_commerce.model.Order;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.PlaceOrderRequest; // Aunque esté vacío, lo usamos para el @RequestBody
import com.ilich.sb.e_commerce.payload.response.OrderResponse;
import com.ilich.sb.e_commerce.service.IOrderService; // Usa la interfaz
import com.ilich.sb.e_commerce.mapper.OrderMapper; // Importa tu nuevo mapper

import com.ilich.sb.e_commerce.util.UserUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;
    private final UserUtil userUtil;

    @Autowired
    public OrderController(IOrderService orderService, UserUtil userUtil) {
        this.orderService = orderService;
        this.userUtil = userUtil;
    }

    /**
     * Crea un nuevo pedido a partir del carrito del usuario autenticado.
     * Requiere rol de USER o ADMIN.
     *
     * @param request El objeto PlaceOrderRequest (puede estar vacío si solo es una confirmación).
     * @return ResponseEntity con el OrderResponse del pedido creado y estado HTTP 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        Order newOrder = orderService.createOrderFromCart(currentUser);
        return new ResponseEntity<>(OrderMapper.toOrderResponse(newOrder), HttpStatus.CREATED);
    }

    /**
     * Obtiene el historial de pedidos del usuario autenticado.
     * Requiere rol de USER o ADMIN.
     *
     * @return ResponseEntity con una lista de OrderResponse y estado HTTP 200.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getUserOrders() {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        List<OrderResponse> orderResponses = orders.stream()
                .map(OrderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    /**
     * Obtiene los detalles de un pedido específico por su ID para el usuario autenticado.
     * Requiere rol de USER o ADMIN.
     *
     * @param orderId El ID del pedido a buscar.
     * @return ResponseEntity con el OrderResponse del pedido y estado HTTP 200, o 404 si no se encuentra.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
        User currentUser = userUtil.getCurrentAuthenticatedUser();
        Order order = orderService.getOrderByIdAndUser(orderId, currentUser)
                .orElseThrow(() -> new RuntimeException("Order not found or not accessible.")); // Manejar con GlobalExceptionHandler
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }
}
