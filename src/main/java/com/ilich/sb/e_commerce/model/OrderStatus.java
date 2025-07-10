package com.ilich.sb.e_commerce.model;

public enum OrderStatus {
    PENDING,        // Pedido creado, esperando confirmación/pago
    PROCESSING,     // Pago recibido, preparando el envío
    SHIPPED,        // Enviado
    DELIVERED,      // Entregado al cliente
    CANCELLED,      // Cancelado por el usuario o administrador
    REFUNDED        // Reembolsado


}