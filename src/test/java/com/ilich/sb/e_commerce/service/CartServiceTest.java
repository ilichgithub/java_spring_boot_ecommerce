package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.Cart;
import com.ilich.sb.e_commerce.model.CartItem;
import com.ilich.sb.e_commerce.model.Category; // Necesario si Product tiene Category
import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.ICartItemRepository;
import com.ilich.sb.e_commerce.repository.ICartRepository;
import com.ilich.sb.e_commerce.repository.IProductRepository;
import com.ilich.sb.e_commerce.service.impl.CartServiceImpl; // Asegúrate de importar tu implementación concreta

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Importa los métodos estáticos de Mockito

@ExtendWith(MockitoExtension.class) // Habilita la integración de Mockito con JUnit 5
public class CartServiceTest {

    @Mock // Mock del repositorio de carritos
    private ICartRepository cartRepository;

    @Mock // Mock del repositorio de ítems de carrito
    private ICartItemRepository cartItemRepository;

    @Mock // Mock del repositorio de productos (ya que CartService lo usa)
    private IProductRepository productRepository;

    @InjectMocks // Inyecta los mocks en una instancia real de CartServiceImpl
    private CartServiceImpl cartService; // Asegúrate de que coincida con tu implementación

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Cart userCart;
    private CartItem cartItem1;

    @BeforeEach
    void setUp() {
        // Inicializar un usuario de prueba
        testUser = new User("testuser", "password");
        testUser.setId(1L); // Asignar un ID para la simulación

        // Inicializar productos de prueba (necesitan tener precio y stock)
        Category dummyCategory = new Category(100L, "Electronics"); // Categoría dummy
        testProduct1 = new Product("Laptop X", "Powerful laptop", new BigDecimal("1200.00"), 10, dummyCategory);
        testProduct1.setId(101L);
        testProduct2 = new Product("Mouse Z", "Gaming mouse", new BigDecimal("50.00"), 50, dummyCategory);
        testProduct2.setId(102L);

        // Inicializar un carrito para el usuario
        userCart = new Cart(testUser);
        userCart.setId(201L);
        userCart.setCartItems(new HashSet<>()); // Asegurarse de que esté vacío al inicio del test

        // Inicializar un CartItem para posibles pruebas de actualización/eliminación
        cartItem1 = new CartItem(userCart, testProduct1, 2);
        cartItem1.setId(301L);
        userCart.addCartItem(cartItem1); // Añadirlo al carrito en setUp
    }

    @Test
    void testGetCartByUser_ExistingCart() {
        // Simular que el repositorio encuentra el carrito existente
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));

        Cart foundCart = cartService.getCartByUser(testUser);

        assertNotNull(foundCart);
        assertEquals(userCart.getId(), foundCart.getId());
        assertEquals(testUser, foundCart.getUser());
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartRepository, never()).save(any(Cart.class)); // No debería guardar si ya existe
    }

    @Test
    void testGetCartByUser_NewCartCreated() {
        // Simular que el repositorio NO encuentra el carrito
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        // Simular que cuando se guarda un nuevo carrito, devuelve ese carrito
        when(cartRepository.save(any(Cart.class))).thenReturn(userCart); // Simula el guardado del nuevo carrito

        Cart newCart = cartService.getCartByUser(testUser);

        assertNotNull(newCart);
        assertEquals(testUser, newCart.getUser());
        // El ID puede ser el que asignamos en setUp para la simulación, o null si no se asigna al mock
        assertEquals(userCart.getId(), newCart.getId());
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartRepository, times(1)).save(any(Cart.class)); // Debería guardar uno nuevo
    }

    @Test
    void testAddProductToCart_NewItem() {
        // Limpiar cartItems en userCart para este test
        userCart.getCartItems().clear();

        // Simular el carrito del usuario
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        // Simular que el producto existe
        when(productRepository.findById(testProduct2.getId())).thenReturn(Optional.of(testProduct2));
        // Simular que no hay un CartItem existente para este producto en el carrito
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct2)).thenReturn(Optional.empty());
        // Simular el guardado de un nuevo CartItem
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem savedItem = invocation.getArgument(0);
            savedItem.setId(302L); // Asignar un ID para el nuevo item guardado
            return savedItem;
        });

        CartItem resultItem = cartService.addProductToCart(testUser, testProduct2.getId(), 3);

        assertNotNull(resultItem);
        assertEquals(testProduct2.getId(), resultItem.getProduct().getId());
        assertEquals(3, resultItem.getQuantity());
        assertEquals(userCart.getId(), resultItem.getCart().getId());

        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct2.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct2);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));

        // Verificar que el item se añadió a la colección del carrito
        assertTrue(userCart.getCartItems().contains(resultItem));
    }

    @Test
    void testAddProductToCart_ExistingItem_QuantityUpdated() {
        // `userCart` ya contiene `cartItem1` (testProduct1, quantity 2) del `setUp`

        // Simular el carrito del usuario
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        // Simular que el producto existe
        when(productRepository.findById(testProduct1.getId())).thenReturn(Optional.of(testProduct1));
        // Simular que el CartItem ya existe para testProduct1
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct1)).thenReturn(Optional.of(cartItem1));
        // Simular el guardado (actualización) del CartItem
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem1);

        CartItem resultItem = cartService.addProductToCart(testUser, testProduct1.getId(), 5);

        assertNotNull(resultItem);
        assertEquals(testProduct1.getId(), resultItem.getProduct().getId());
        assertEquals(7, resultItem.getQuantity()); // 2 (original) + 5 (añadido)
        assertEquals(userCart.getId(), resultItem.getCart().getId());

        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct1.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct1);
        verify(cartItemRepository, times(1)).save(cartItem1); // Se guarda la misma instancia de cartItem1
    }

    @Test
    void testAddProductToCart_ProductNotFound_ThrowsException() {
        // Simular el carrito del usuario
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        // Simular que el producto NO existe
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cartService.addProductToCart(testUser, 999L, 1);
        });

        assertEquals("Product not found with id: 999", thrown.getMessage());
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(999L);
        verify(cartItemRepository, never()).findByCartAndProduct(any(Cart.class), any(Product.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void testAddProductToCart_NegativeQuantity_ThrowsException() {
        // No se necesita mockear repositorios para esta prueba, ya que la validación es temprana
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cartService.addProductToCart(testUser, testProduct1.getId(), -1);
        });

        assertEquals("Quantity must be positive.", thrown.getMessage());
        verifyNoInteractions(cartRepository, productRepository, cartItemRepository); // No debería haber ninguna interacción con los mocks
    }

    @Test
    void testUpdateProductQuantity_ExistingItem_NewQuantity() {
        // `userCart` ya contiene `cartItem1` (testProduct1, quantity 2)
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        when(productRepository.findById(testProduct1.getId())).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct1)).thenReturn(Optional.of(cartItem1));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem1);

        CartItem resultItem = cartService.updateProductQuantity(testUser, testProduct1.getId(), 5);

        assertNotNull(resultItem);
        assertEquals(testProduct1.getId(), resultItem.getProduct().getId());
        assertEquals(5, resultItem.getQuantity()); // Cantidad actualizada
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct1.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct1);
        verify(cartItemRepository, times(1)).save(cartItem1);
    }

    @Test
    void testUpdateProductQuantity_ZeroQuantity_RemovesItem() {
        // `userCart` ya contiene `cartItem1` (testProduct1, quantity 2)
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        when(productRepository.findById(testProduct1.getId())).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct1)).thenReturn(Optional.of(cartItem1));
        // Simula que la eliminación no devuelve nada (método void)
        doNothing().when(cartItemRepository).delete(cartItem1);

        CartItem resultItem = cartService.updateProductQuantity(testUser, testProduct1.getId(), 0);

        assertNull(resultItem); // Debe ser nulo si el ítem fue eliminado
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct1.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct1);
        verify(cartItemRepository, times(1)).delete(cartItem1); // Verifica que se llamó al método delete
        verify(cartItemRepository, never()).save(any(CartItem.class)); // No debe guardar
        assertFalse(userCart.getCartItems().contains(cartItem1)); // Verifica que se eliminó de la colección
    }

    @Test
    void testUpdateProductQuantity_ProductNotInCart_ThrowsException() {
        // `userCart` contiene cartItem1 (testProduct1), pero estamos buscando testProduct2
        userCart.getCartItems().remove(cartItem1); // Asegurarse de que el carrito esté sin testProduct1

        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        when(productRepository.findById(testProduct2.getId())).thenReturn(Optional.of(testProduct2));
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct2)).thenReturn(Optional.empty()); // No encontrado

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cartService.updateProductQuantity(testUser, testProduct2.getId(), 5);
        });

        assertEquals("Product with id: " + testProduct2.getId() + " not found in cart.", thrown.getMessage());
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct2.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct2);
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void testRemoveProductFromCart_Success() {
        // `userCart` ya contiene `cartItem1` (testProduct1)
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        when(productRepository.findById(testProduct1.getId())).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct1)).thenReturn(Optional.of(cartItem1));
        doNothing().when(cartItemRepository).delete(cartItem1);

        cartService.removeProductFromCart(testUser, testProduct1.getId());

        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct1.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct1);
        verify(cartItemRepository, times(1)).delete(cartItem1);
        assertFalse(userCart.getCartItems().contains(cartItem1)); // Verifica que se eliminó de la colección
    }

    @Test
    void testRemoveProductFromCart_ProductNotInCart_ThrowsException() {
        // `userCart` contiene cartItem1 (testProduct1), pero estamos buscando testProduct2
        userCart.getCartItems().remove(cartItem1); // Asegurarse de que el carrito esté sin testProduct1

        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        when(productRepository.findById(testProduct2.getId())).thenReturn(Optional.of(testProduct2));
        when(cartItemRepository.findByCartAndProduct(userCart, testProduct2)).thenReturn(Optional.empty()); // No encontrado

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cartService.removeProductFromCart(testUser, testProduct2.getId());
        });

        assertEquals("Product with id: " + testProduct2.getId() + " not found in cart.", thrown.getMessage());
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(1)).findById(testProduct2.getId());
        verify(cartItemRepository, times(1)).findByCartAndProduct(userCart, testProduct2);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void testClearCart_Success() {
        // `userCart` ya contiene `cartItem1` del `setUp`
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        doNothing().when(cartItemRepository).deleteAll(anySet()); // Simula la eliminación de todos los ítems
        when(cartRepository.save(any(Cart.class))).thenReturn(userCart); // Simula que el carrito vacío se guarda

        cartService.clearCart(testUser);

        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartItemRepository, times(1)).deleteAll(userCart.getCartItems()); // Verifica que se llamo con la colección correcta
        verify(cartRepository, times(1)).save(userCart);
        assertTrue(userCart.getCartItems().isEmpty()); // Verifica que la colección en memoria está vacía
    }

    @Test
    void testGetCartItems_Success() {
        // `userCart` ya contiene `cartItem1` del `setUp`
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));

        Set<CartItem> items = cartService.getCartItems(testUser);

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        assertTrue(items.contains(cartItem1));
        verify(cartRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetCartItems_EmptyCart() {
        // Asegurarse de que el carrito esté vacío para este test
        userCart.getCartItems().clear();
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));

        Set<CartItem> items = cartService.getCartItems(testUser);

        assertNotNull(items);
        assertTrue(items.isEmpty());
        verify(cartRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetCartTotal_Success() {
        // `userCart` ya contiene `cartItem1` (Product1: $1200, Cantidad: 2) -> Subtotal: $2400.00
        // Añadir otro item para un cálculo más complejo
        CartItem cartItem2 = new CartItem(userCart, testProduct2, 3); // Product2: $50.00, Cantidad: 3 -> Subtotal: $150.00
        cartItem2.setId(302L);
        userCart.addCartItem(cartItem2); // Añadir a la colección en el carrito mock

        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));

        BigDecimal total = cartService.getCartTotal(testUser);

        assertNotNull(total);
        // Esperamos 2400.00 (de testProduct1) + 150.00 (de testProduct2) = 2550.00
        assertEquals(new BigDecimal("2550.00"), total);
        verify(cartRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetCartTotal_EmptyCart() {
        // Asegurarse de que el carrito esté vacío para este test
        userCart.getCartItems().clear();
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));

        BigDecimal total = cartService.getCartTotal(testUser);

        assertNotNull(total);
        assertEquals(BigDecimal.ZERO, total);
        verify(cartRepository, times(1)).findByUser(testUser);
    }
}
