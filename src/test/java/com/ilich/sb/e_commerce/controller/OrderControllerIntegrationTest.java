package com.ilich.sb.e_commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilich.sb.e_commerce.model.*;
import com.ilich.sb.e_commerce.payload.request.PlaceOrderRequest;
import com.ilich.sb.e_commerce.repository.*;
import com.ilich.sb.e_commerce.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
        import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Asegura que se usa application-test.properties
@Transactional // Cada test se ejecuta en una transacción y se revierte al final
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para serializar/deserializar JSON

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository; // Asegúrate de tenerlo inyectado
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private ICartRepository cartRepository;
    @Autowired
    private ICartItemRepository cartItemRepository;
    @Autowired
    private IOrderRepository orderRepository;
    @Autowired
    private IOrderItemRepository orderItemRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private IRefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private String userToken;
    private Product testProduct1;
    private Product testProduct2;
    private Cart userCart;

    @BeforeEach
    void setUp() {
        // MUY IMPORTANTE: La orden de eliminación es crucial debido a las restricciones de clave foránea.
        // Elimina primero los "hijos" antes de los "padres".
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch(); // Asegúrate de eliminar también los roles si los gestionas en el test

        // 1. Asegurar que el rol USER exista
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        // 2. Crear y guardar un usuario de prueba
        testUser = new User("testuser", encoder.encode("password123"));
        testUser.setRoles(Collections.singleton(userRole));
        testUser = userRepository.save(testUser);

        // 3. Generar token JWT para el usuario de prueba
        //UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
        userToken = jwtUtils.generateTokenFromUsername(testUser.getUsername());

        // 4. Crear y guardar una categoría de prueba
        Category testCategory = new Category("Electronics");
        categoryRepository.save(testCategory);

        // 5. Crear y guardar productos de prueba (con stock)
        testProduct1 = new Product("Laptop Pro", "Powerful laptop", new BigDecimal("1200.00"), 10, testCategory);
        productRepository.save(testProduct1);

        testProduct2 = new Product("Mouse Wireless", "Ergonomic mouse", new BigDecimal("25.00"), 50, testCategory);
        productRepository.save(testProduct2);

        // 6. Crear un carrito para el usuario y añadir ítems para la prueba de "place order"
        userCart = new Cart(testUser);
        cartRepository.save(userCart); // Guardar el carrito primero

        CartItem cartItem1 = new CartItem(userCart, testProduct1, 2); // 2 Laptops
        CartItem cartItem2 = new CartItem(userCart, testProduct2, 3); // 3 Mice
        cartItemRepository.save(cartItem1);
        cartItemRepository.save(cartItem2);

        // Asegurarse de que el carrito en memoria refleja los ítems guardados
        userCart.getCartItems().add(cartItem1);
        userCart.getCartItems().add(cartItem2);
        cartRepository.save(userCart); // Guardar el carrito con los ítems asociados
    }

    @Test
    void placeOrder_ShouldCreateOrderAndUpdateStockAndClearCart() throws Exception {
        // GIVEN: Un carrito existente con ítems (configurado en @BeforeEach)
        // La cantidad inicial de stock: testProduct1 = 10, testProduct2 = 50

        // WHEN: Se realiza una petición POST para crear un pedido
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Body vacío
                .andExpect(status().isCreated()) // THEN: Se espera un estado 201 Created
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(2475.00)) // (2*1200) + (3*25) = 2475
                .andExpect(jsonPath("$.items", hasSize(2)));

        // THEN: Verificar la persistencia en la base de datos
        List<Order> orders = orderRepository.findByUser(testUser);
        assertEquals(1, orders.size(), "Debe haber un pedido para el usuario");
        Order createdOrder = orders.get(0);
        assertNotNull(createdOrder.getId());
        assertEquals(2, createdOrder.getOrderItems().size(), "El pedido debe tener 2 ítems");

        // Verificar stock actualizado
        Product updatedProduct1 = productRepository.findById(testProduct1.getId()).orElseThrow();
        Product updatedProduct2 = productRepository.findById(testProduct2.getId()).orElseThrow();
        assertEquals(8, updatedProduct1.getStockQuantity(), "El stock de Laptop Pro debe ser 8"); // 10 - 2
        assertEquals(47, updatedProduct2.getStockQuantity(), "El stock de Mouse Wireless debe ser 47"); // 50 - 3

        // Verificar que el carrito está vacío
        Cart updatedCart = cartRepository.findByUser(testUser).orElseThrow();
        assertTrue(updatedCart.getCartItems().isEmpty(), "El carrito del usuario debe estar vacío después del pedido");
        assertEquals(0, cartItemRepository.findByCart(updatedCart).size(), "No debe haber CartItems asociados al carrito");
    }

    @Test
    void placeOrder_ShouldReturnBadRequest_WhenCartIsEmpty() throws Exception {
        // GIVEN: Un carrito vacío
        // Limpiamos los ítems del carrito que se crearon en @BeforeEach
        cartItemRepository.deleteAllInBatch();
        userCart.getCartItems().clear(); // Asegúrate de que el objeto en memoria también esté vacío
        cartRepository.save(userCart); // Guardar el carrito vacío en la DB

        // WHEN: Se intenta crear un pedido
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Body vacío
                .andExpect(status().isBadRequest()) // THEN: Se espera un estado 400 Bad Request
                .andExpect(jsonPath("$.message").value("Cannot create an order from an empty cart."));

        // THEN: No se debe haber creado ningún pedido
        assertEquals(0, orderRepository.findByUser(testUser).size());
    }

    @Test
    void placeOrder_ShouldReturnBadRequest_WhenInsufficientStock() throws Exception {
        // GIVEN: Stock insuficiente para un producto
        testProduct1.setStockQuantity(1); // Reducir stock para que sea insuficiente
        productRepository.save(testProduct1); // Guardar el cambio de stock

        // WHEN: Se intenta crear un pedido
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Body vacío
                .andExpect(status().isBadRequest()) // THEN: Se espera un estado 400 Bad Request
                .andExpect(jsonPath("$.message", containsString("Not enough stock for product: Laptop Pro")));

        // THEN: No se debe haber creado ningún pedido y el stock no debe haber cambiado
        assertEquals(0, orderRepository.findByUser(testUser).size());
        Product originalProduct1 = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertEquals(1, originalProduct1.getStockQuantity(), "El stock no debe haber cambiado");
    }


    @Test
    void getUserOrders_ShouldReturnUsersOrders() throws Exception {
        // GIVEN: Se crea un pedido de prueba directamente en la BD para el usuario
        Order existingOrder = new Order(testUser, new BigDecimal("100.00"), OrderStatus.DELIVERED);
        orderRepository.save(existingOrder);
        OrderItem existingOrderItem = new OrderItem(existingOrder, testProduct1, 1, new BigDecimal("100.00"));
        orderItemRepository.save(existingOrderItem);
        existingOrder.addOrderItem(existingOrderItem); // Asegurarse de que la relación se establezca en memoria también
        orderRepository.save(existingOrder); // Volver a guardar para asegurar la relación en la DB si es bidireccional

        // WHEN: Se solicita el historial de pedidos
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()) // THEN: Se espera un estado 200 OK
                .andExpect(jsonPath("$", hasSize(1))) // Se espera 1 pedido
                .andExpect(jsonPath("$[0].orderId").value(existingOrder.getId()))
                .andExpect(jsonPath("$[0].totalAmount").value(100.00))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].productId").value(testProduct1.getId()));
    }

    @Test
    void getOrderDetails_ShouldReturnOrderDetails_WhenOrderExistsAndBelongsToUser() throws Exception {
        // GIVEN: Se crea un pedido de prueba directamente en la BD
        Order existingOrder = new Order(testUser, new BigDecimal("100.00"), OrderStatus.DELIVERED);
        orderRepository.save(existingOrder);
        OrderItem existingOrderItem = new OrderItem(existingOrder, testProduct1, 1, new BigDecimal("100.00"));
        orderItemRepository.save(existingOrderItem);
        existingOrder.addOrderItem(existingOrderItem);
        orderRepository.save(existingOrder);


        // WHEN: Se solicita un pedido específico por ID
        mockMvc.perform(get("/api/orders/{orderId}", existingOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()) // THEN: Se espera un estado 200 OK
                .andExpect(jsonPath("$.orderId").value(existingOrder.getId()))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void getOrderDetails_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        // WHEN: Se solicita un pedido que no existe
        mockMvc.perform(get("/api/orders/{orderId}", 9999L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound()) // THEN: Se espera un estado 404 Not Found
                .andExpect(jsonPath("$.message").value("Order not found or not accessible."));
    }

    @Test
    void getOrderDetails_ShouldReturnNotFound_WhenOrderDoesNotBelongToUser() throws Exception {
        // GIVEN: Un segundo usuario y un pedido que le pertenece a él
        User anotherUser = new User("anotheruser", encoder.encode("pass"));
        roleRepository.findByName("ROLE_USER").ifPresent(role -> anotherUser.setRoles(Collections.singleton(role)));
        userRepository.save(anotherUser);

        Order anotherUserOrder = new Order(anotherUser, new BigDecimal("50.00"), OrderStatus.DELIVERED);
        orderRepository.save(anotherUserOrder);

        // WHEN: El testUser intenta acceder al pedido del otro usuario
        mockMvc.perform(get("/api/orders/{orderId}", anotherUserOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound()) // THEN: Se espera un estado 404 Not Found
                .andExpect(jsonPath("$.message").value("Order not found or not accessible."));
    }
}