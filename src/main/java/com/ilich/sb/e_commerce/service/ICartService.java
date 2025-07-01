package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.Cart;
import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.model.User;

import java.math.BigDecimal;
import java.util.Set;

public interface ICartService {

    /**
     * Obtiene el carrito de un usuario. Si el usuario no tiene un carrito, se crea uno nuevo.
     * @param user El usuario para el que se busca/crea el carrito.
     * @return El carrito de compras del usuario.
     */
    Cart getCartByUser(User user);

    /**
     * Añade un producto al carrito de un usuario. Si el producto ya existe en el carrito,
     * se actualiza la cantidad. Si no, se añade como un nuevo CartItem.
     * @param user El usuario cuyo carrito se va a modificar.
     * @param productId El ID del producto a añadir.
     * @param quantity La cantidad del producto a añadir.
     * @return El CartItem actualizado o recién creado.
     * @throws RuntimeException si el producto no se encuentra o la cantidad es inválida.
     */
    CartItem addProductToCart(User user, Long productId, int quantity);

    /**
     * Actualiza la cantidad de un producto existente en el carrito de un usuario.
     * Si la cantidad es 0 o menos, el producto se elimina del carrito.
     * @param user El usuario cuyo carrito se va a modificar.
     * @param productId El ID del producto cuya cantidad se va a actualizar.
     * @param newQuantity La nueva cantidad para el producto.
     * @return El CartItem actualizado, o null si fue eliminado.
     * @throws RuntimeException si el producto no se encuentra en el carrito o la cantidad es inválida.
     */
    CartItem updateProductQuantity(User user, Long productId, int newQuantity);

    /**
     * Elimina un producto específico del carrito de un usuario.
     * @param user El usuario cuyo carrito se va a modificar.
     * @param productId El ID del producto a eliminar.
     * @throws RuntimeException si el producto no se encuentra en el carrito.
     */
    void removeProductFromCart(User user, Long productId);

    /**
     * Vacía completamente el carrito de un usuario, eliminando todos los ítems.
     * @param user El usuario cuyo carrito se va a vaciar.
     */
    void clearCart(User user);

    /**
     * Obtiene todos los ítems del carrito de un usuario.
     * @param user El usuario cuyo carrito se consulta.
     * @return Un Set de CartItem que representa los productos en el carrito.
     */
    Set<CartItem> getCartItems(User user);

    /**
     * Calcula el precio total de todos los productos en el carrito de un usuario.
     * @param user El usuario cuyo carrito se va a calcular.
     * @return El BigDecimal que representa el precio total del carrito.
     */
    BigDecimal getCartTotal(User user);
}