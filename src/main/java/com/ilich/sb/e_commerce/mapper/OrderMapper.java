package com.ilich.sb.e_commerce.mapper;

import com.ilich.sb.e_commerce.model.Order;
import com.ilich.sb.e_commerce.model.OrderItem;
import com.ilich.sb.e_commerce.payload.response.OrderItemResponse;
import com.ilich.sb.e_commerce.payload.response.OrderResponse;

import java.util.Set;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    /**
     * Mapea una entidad OrderItem a su DTO de respuesta OrderItemResponse.
     *
     * @param orderItem La entidad OrderItem a mapear.
     * @return Un objeto OrderItemResponse.
     */
    public static OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getPriceAtPurchase(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }

    /**
     * Mapea una entidad Order a su DTO de respuesta OrderResponse.
     *
     * @param order La entidad Order a mapear.
     * @return Un objeto OrderResponse.
     */
    public static OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        Set<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(OrderMapper::toOrderItemResponse)
                .collect(Collectors.toSet());

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                itemResponses
        );
    }
}
