package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.*;
import com.ilich.sb.e_commerce.repository.*;
import com.ilich.sb.e_commerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito para JUnit 5
public class OrderServiceTest {

    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IOrderItemRepository orderItemRepository;
    @Mock
    private ICartRepository cartRepository;
    @Mock
    private ICartItemRepository cartItemRepository;
    @Mock
    private IProductRepository productRepository;

    @InjectMocks // Inyecta los mocks anteriores en esta instancia de OrderService
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Cart userCart;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "encodedpassword");
        // Asignar roles si es necesario para OrderService, aunque generalmente no se usan en este nivel para la lógica de negocio
        testUser.setRoles(Collections.singleton(new Role("ROLE_USER")));


        Category testCategory = new Category(1L, "Electronics");

        testProduct1 = new Product(101L, "Laptop Pro", "Powerful laptop", new BigDecimal("1200.00"), 10, testCategory);
        testProduct2 = new Product(102L, "Mouse Wireless", "Ergonomic mouse", new BigDecimal("25.00"), 50, testCategory);

        userCart = new Cart(1L, testUser, new HashSet<>());

        cartItem1 = new CartItem(1L, userCart, testProduct1, 2); // 2 Laptops
        cartItem2 = new CartItem(2L, userCart, testProduct2, 3); // 3 Mice

        // Configurar las relaciones bidireccionales
        userCart.getCartItems().add(cartItem1);
        userCart.getCartItems().add(cartItem2);
    }

    @Test
    void createOrderFromCart_ShouldCreateOrderAndUpdateStockAndClearCart() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Simula guardar producto
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(100L); // Asignar un ID para el Order guardado
            return savedOrder;
        });

        // Act
        Order createdOrder = orderService.createOrderFromCart(testUser);

        // Assert
        assertNotNull(createdOrder);
        assertEquals(testUser.getId(), createdOrder.getUser().getId());
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        assertEquals(new BigDecimal("2475.00"), createdOrder.getTotalAmount()); // (2*1200) + (3*25) = 2400 + 75 = 2475

        assertEquals(2, createdOrder.getOrderItems().size()); // Debe tener 2 ítems

        // Verificar que el stock de los productos se actualizó
        assertEquals(8, testProduct1.getStockQuantity()); // 10 - 2 = 8
        assertEquals(47, testProduct2.getStockQuantity()); // 50 - 3 = 47

        // Verificar que los repositorios fueron llamados correctamente
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(productRepository, times(2)).save(any(Product.class)); // Dos productos actualizados
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartItemRepository, times(1)).deleteAll(userCart.getCartItems()); // Carrito vaciado
        verify(cartRepository, times(1)).save(userCart); // El carrito se guarda sin items
    }

    @Test
    void createOrderFromCart_ShouldThrowException_WhenCartIsEmpty() {
        // Arrange
        userCart.setCartItems(new HashSet<>()); // Vaciar el carrito para esta prueba
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(testUser)
        );
        assertEquals("Cannot create an order from an empty cart.", exception.getMessage());
        // Verificar que no se realizaron llamadas de guardado o actualización
        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
        verify(cartItemRepository, never()).deleteAll(anySet());
    }

    @Test
    void createOrderFromCart_ShouldThrowException_WhenInsufficientStock() {
        // Arrange
        testProduct1.setStockQuantity(1); // Stock insuficiente para cartItem1 (cantidad 2)
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(userCart));
        //when(productRepository.findById(testProduct1.getId())).thenReturn(Optional.of(testProduct1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(testUser)
        );
        assertTrue(exception.getMessage().contains("Not enough stock for product: Laptop Pro"));
        // Verificar que no se realizaron llamadas de guardado o actualización
        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
        verify(cartItemRepository, never()).deleteAll(anySet());
    }

    @Test
    void getOrdersByUser_ShouldReturnUsersOrders() {
        // Arrange
        Order order1 = new Order(10L, testUser, new BigDecimal("100.00"), OrderStatus.DELIVERED);
        Order order2 = new Order(11L, testUser, new BigDecimal("200.00"), OrderStatus.PENDING);
        List<Order> userOrders = Arrays.asList(order1, order2);

        when(orderRepository.findByUser(testUser)).thenReturn(userOrders);

        // Act
        List<Order> result = orderService.getOrdersByUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(order1));
        assertTrue(result.contains(order2));
        verify(orderRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getOrderByIdAndUser_ShouldReturnOrder_WhenFoundAndBelongsToUser() {
        // Arrange
        Order order = new Order(10L, testUser, new BigDecimal("100.00"), OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndUser(order.getId(), testUser)).thenReturn(Optional.of(order));

        // Act
        Optional<Order> result = orderService.getOrderByIdAndUser(order.getId(), testUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(order.getId(), result.get().getId());
        verify(orderRepository, times(1)).findByIdAndUser(order.getId(), testUser);
    }

    @Test
    void getOrderByIdAndUser_ShouldReturnEmptyOptional_WhenNotFound() {
        // Arrange
        when(orderRepository.findByIdAndUser(anyLong(), any(User.class))).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderByIdAndUser(99L, testUser);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findByIdAndUser(99L, testUser);
    }

    @Test
    void getOrderByIdAndUser_ShouldReturnEmptyOptional_WhenNotBelongsToUser() {
        // Arrange
        User anotherUser = new User(2L, "anotheruser", "pass");
        Order order = new Order(10L, anotherUser, new BigDecimal("100.00"), OrderStatus.DELIVERED);
        // El mock debería retornar Optional.empty() si se busca con testUser, incluso si existe para anotherUser
        when(orderRepository.findByIdAndUser(order.getId(), testUser)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderByIdAndUser(order.getId(), testUser);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findByIdAndUser(order.getId(), testUser);
    }
}
