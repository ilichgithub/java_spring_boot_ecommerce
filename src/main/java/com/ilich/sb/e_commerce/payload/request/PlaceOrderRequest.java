package com.ilich.sb.e_commerce.payload.request;

// No se necesitan anotaciones de validación aquí si solo es una confirmación
// y la lógica de validación se maneja en el servicio desde el carrito.

import java.io.Serializable;

public class PlaceOrderRequest {
    // Por ahora, este DTO puede estar vacío si la lógica es simplemente
    // "confirmar pedido del carrito actual del usuario autenticado".
    // Si necesitas más datos (ej. dirección de envío, método de pago),
    // los añadirías aquí con sus validaciones.

    // Ejemplo:
    // @NotBlank(message = "Shipping address is required")
    // private String shippingAddress;

    // Puedes añadir un constructor vacío si lo necesitas
    public PlaceOrderRequest() {
    }

    // Getters y Setters si añades campos
    // public String getShippingAddress() { return shippingAddress; }
    // public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}