package com.ilich.sb.e_commerce.service.impl;

import com.ilich.sb.e_commerce.model.Cart;
import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.ICartItemRepository;
import com.ilich.sb.e_commerce.repository.ICartRepository;
import com.ilich.sb.e_commerce.repository.IProductRepository;
import com.ilich.sb.e_commerce.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final IProductRepository productRepository; // Necesitamos acceso a productos para añadirlos al carrito

    @Autowired
    public CartServiceImpl(ICartRepository cartRepository, ICartItemRepository cartItemRepository, IProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional // Asegura que todas las operaciones dentro de este método se realicen como una sola transacción
    public Cart getCartByUser(User user) {
        // Busca el carrito por el usuario. Si no existe, crea uno nuevo y lo guarda.
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    @Transactional
    public CartItem addProductToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive.");
        }

        Cart cart = getCartByUser(user); // Obtiene o crea el carrito del usuario

        // Busca el producto por su ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Intenta encontrar si el producto ya existe en el carrito
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // Si el producto ya está en el carrito, actualiza la cantidad
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // Si el producto no está en el carrito, crea un nuevo CartItem
            cartItem = new CartItem(cart, product, quantity);
            cart.addCartItem(cartItem); // Añade el CartItem a la colección del Cart
        }
        return cartItemRepository.save(cartItem); // Guarda o actualiza el CartItem
    }

    @Override
    @Transactional
    public CartItem updateProductQuantity(User user, Long productId, int newQuantity) {
        Cart cart = getCartByUser(user); // Obtiene el carrito del usuario

        // Busca el producto en el carrito
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart,
                        productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId)))
                .orElseThrow(() -> new RuntimeException("Product with id: " + productId + " not found in cart."));

        if (newQuantity <= 0) {
            // Si la nueva cantidad es 0 o menos, elimina el ítem del carrito
            cart.removeCartItem(cartItem); // Elimina de la colección del Cart
            cartItemRepository.delete(cartItem); // Elimina de la base de datos
            return null; // Indica que el ítem fue eliminado
        } else {
            // Si la nueva cantidad es positiva, actualiza
            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem); // Guarda la actualización
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(User user, Long productId) {
        Cart cart = getCartByUser(user); // Obtiene el carrito del usuario

        // Busca el producto en el carrito
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart,
                        productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId)))
                .orElseThrow(() -> new RuntimeException("Product with id: " + productId + " not found in cart."));

        cart.removeCartItem(cartItem); // Elimina de la colección del Cart
        cartItemRepository.delete(cartItem); // Elimina de la base de datos
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user); // Obtiene el carrito del usuario
        cartItemRepository.deleteAll(cart.getCartItems()); // Elimina todos los ítems asociados al carrito
        cart.getCartItems().clear(); // Limpia la colección en memoria
        cartRepository.save(cart); // Guarda el carrito con la colección vacía
    }

    @Override
    @Transactional(readOnly = true) // Solo lectura, no se modifican datos
    public Set<CartItem> getCartItems(User user) {
        Cart cart = getCartByUser(user); // Obtiene el carrito del usuario
        // Asegúrate de que los ítems se carguen si son LAZY
        // Esto podría causar un LazyInitializationException si no se maneja correctamente
        // o si no se llama dentro de una transacción.
        // Como getCartByUser ya está dentro de una transacción, los ítems deberían estar accesibles.
        return cart.getCartItems();
    }

    @Override
    @Transactional(readOnly = true) // Solo lectura
    public BigDecimal getCartTotal(User user) {
        Cart cart = getCartByUser(user); // Obtiene el carrito del usuario
        return cart.getCartItems().stream()
                .map(CartItem::getSubtotal) // Usa el método getSubtotal de CartItem
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Suma todos los subtotales
    }
}
