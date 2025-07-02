package com.ilich.sb.e_commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.model.Role;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.AddToCartRequestDTO;
import com.ilich.sb.e_commerce.payload.request.UpdateCartItemRequestDTO;
import com.ilich.sb.e_commerce.repository.*;
import com.ilich.sb.e_commerce.security.jwt.JwtUtils;
import com.ilich.sb.e_commerce.service.impl.CartServiceImpl;
import com.ilich.sb.e_commerce.service.impl.UserDetailsImpl;
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
import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest // Inicia un contexto completo de Spring Boot para la prueba
@AutoConfigureMockMvc // Configura y auto-inyecta MockMvc
@ActiveProfiles("test") // Usa un perfil de test para configuración de BD específica
@Transactional // Cada test se ejecuta en una transacción y se revierte al final
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    @Autowired
    private JwtUtils jwtUtils; // Para generar tokens JWT

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ICartRepository cartRepository; // Para limpiar y verificar la BD

    @Autowired
    private ICartItemRepository cartItemRepository; // Para limpiar y verificar la BD

    @Autowired
    private IRefreshTokenRepository refreshTokenRepository; // Para interactuar con la BD de roles

    @Autowired // Inyecta los mocks en una instancia real de CartServiceImpl
    private CartServiceImpl cartService; // Asegúrate de que coincida con tu implementación

    private User testUser;
    private User adminUser; // Opcional, si tienes roles de admin
    private String userToken;
    private String adminToken; // Opcional
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos de prueba antes de cada test para asegurar un estado limpio
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 1. Crear y guardar un usuario de prueba (ROLE_USER)
        testUser = new User( "testuser", encoder.encode("password123"));

        Role userRoleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        testUser.setRoles(new HashSet<>(Arrays.asList(userRoleUser)));

        testUser = userRepository.save(testUser);

        // 2. Generar token JWT para el usuario de prueba
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
        userToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // 3. Crear y guardar una categoría de prueba
        Category testCategory = new Category("Electronics");
        categoryRepository.save(testCategory);

        // 4. Crear y guardar productos de prueba
        testProduct1 = new Product("Laptop Pro", "Powerful laptop", new BigDecimal("1200.01"), 10, testCategory);
        productRepository.save(testProduct1);

        testProduct2 = new Product("Mouse Wireless", "Ergonomic mouse", new BigDecimal("25.00"), 50, testCategory);
        productRepository.save(testProduct2);
    }

    // --- Helper para obtener el token de autorización ---
    private String obtainAuthHeader(String token) {
        return "Bearer " + token;
    }

    // --- Tests para el acceso no autorizado ---

    @Test
    void shouldReturnUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddToCartRequestDTO(testProduct1.getId(), 1))))
                .andExpect(status().isUnauthorized());
    }

    // --- Tests para GET /api/cart ---

    @Test
    void getCart_ShouldReturnEmptyCartForNewUser() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", obtainAuthHeader(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").isNumber()) // Verifica que el ID del carrito exista
                .andExpect(jsonPath("$.items").isEmpty()) // El carrito debería estar vacío
                .andExpect(jsonPath("$.total").value(0.0)); // El total debe ser 0
    }

    @Test
    void getCart_ShouldReturnCartWithItems() throws Exception {
        // Añadir un producto directamente a la BD para este test (simulando una acción previa)
        // Esto evita depender del endpoint POST /add para este test
        cartService.addProductToCart(testUser, testProduct1.getId(), 2);

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", obtainAuthHeader(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").isNumber())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.total").value(testProduct1.getPrice().multiply(BigDecimal.valueOf(2))));
    }

    // --- Tests para POST /api/cart/add ---

    @Test
    void addProductToCart_ShouldAddItem_NewProduct() throws Exception {
        AddToCartRequestDTO request = new AddToCartRequestDTO(testProduct1.getId(), 1);

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Espera un 201 Created
                .andExpect(jsonPath("$.productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.productName").value(testProduct1.getName()))
                .andExpect(jsonPath("$.subtotal").value(testProduct1.getPrice()));

        // Verificar que el ítem realmente se añadió a la BD
        org.junit.jupiter.api.Assertions.assertEquals(1, cartItemRepository.findByCartAndProduct(
                cartRepository.findByUser(testUser).orElseThrow(), testProduct1).get().getQuantity());
    }

    @Test
    void addProductToCart_ShouldUpdateItem_ExistingProduct() throws Exception {
        // Primero añadir un ítem para que ya exista
        cartService.addProductToCart(testUser, testProduct1.getId(), 1);

        AddToCartRequestDTO request = new AddToCartRequestDTO(testProduct1.getId(), 2); // Añadir 2 más

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Todavía devuelve 201 porque es una operación de "añadir"
                .andExpect(jsonPath("$.productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$.quantity").value(3)) // 1 original + 2 nuevos
                .andExpect(jsonPath("$.subtotal").value(testProduct1.getPrice().multiply(BigDecimal.valueOf(3))));

        // Verificar en la BD
        org.junit.jupiter.api.Assertions.assertEquals(3, cartItemRepository.findByCartAndProduct(
                cartRepository.findByUser(testUser).orElseThrow(), testProduct1).get().getQuantity());
    }

    @Test
    void addProductToCart_ShouldReturnBadRequest_InvalidQuantity() throws Exception {
        AddToCartRequestDTO request = new AddToCartRequestDTO(testProduct1.getId(), 0); // Cantidad inválida

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Espera un 400 Bad Request
                .andExpect(jsonPath("$.details.quantity").value("Quantity must be at least 1"));
    }

    @Test
    void addProductToCart_ShouldReturnNotFound_ProductDoesNotExist() throws Exception {
        AddToCartRequestDTO request = new AddToCartRequestDTO(999L, 1); // Producto que no existe

        mockMvc.perform(post("/api/cart/add")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // Espera un 404 Not Found
                .andExpect(jsonPath("$.message").value(containsString("Product not found with id: 999")));
    }

    // --- Tests para PUT /api/cart/update ---

    @Test
    void updateProductQuantity_ShouldUpdateItemQuantity() throws Exception {
        // Añadir un ítem primero
        cartService.addProductToCart(testUser, testProduct1.getId(), 5);

        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO(testProduct1.getId(), 3); // Cambiar a 3

        mockMvc.perform(put("/api/cart/update")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$.quantity").value(3));

        // Verificar en la BD
        org.junit.jupiter.api.Assertions.assertEquals(3, cartItemRepository.findByCartAndProduct(
                cartRepository.findByUser(testUser).orElseThrow(), testProduct1).get().getQuantity());
    }

    @Test
    void updateProductQuantity_ShouldRemoveItem_WhenQuantityIsZero() throws Exception {
        // Añadir un ítem primero
        cartService.addProductToCart(testUser, testProduct1.getId(), 5);

        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO(testProduct1.getId(), 0); // Establecer cantidad a 0

        mockMvc.perform(put("/api/cart/update")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent()); // Espera un 204 No Content

        // Verificar en la BD que el ítem fue eliminado
        org.junit.jupiter.api.Assertions.assertFalse(cartItemRepository.findByCartAndProduct(
                cartRepository.findByUser(testUser).orElseThrow(), testProduct1).isPresent());
    }

    @Test
    void updateProductQuantity_ShouldReturnNotFound_ProductNotInCart() throws Exception {
        // No añadimos el producto a propósito para este test
        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO(testProduct1.getId(), 1);

        mockMvc.perform(put("/api/cart/update")
                        .header("Authorization", obtainAuthHeader(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // Espera un 404 Not Found
                .andExpect(jsonPath("$.message").value(containsString("Product with id: " + testProduct1.getId() + " not found in cart.")));
    }

    // --- Tests para DELETE /api/cart/remove/{productId} ---

    @Test
    void removeProductFromCart_ShouldRemoveItem_Success() throws Exception {
        // Añadir un ítem primero
        cartService.addProductToCart(testUser, testProduct1.getId(), 1);

        mockMvc.perform(delete("/api/cart/remove/{productId}", testProduct1.getId())
                        .header("Authorization", obtainAuthHeader(userToken)))
                .andExpect(status().isNoContent()); // Espera un 204 No Content

        // Verificar en la BD
        org.junit.jupiter.api.Assertions.assertFalse(cartItemRepository.findByCartAndProduct(
                cartRepository.findByUser(testUser).orElseThrow(), testProduct1).isPresent());
    }

    @Test
    void removeProductFromCart_ShouldReturnNotFound_ProductNotInCart() throws Exception {
        // No añadimos el producto a propósito
        mockMvc.perform(delete("/api/cart/remove/{productId}", testProduct1.getId())
                        .header("Authorization", obtainAuthHeader(userToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Product with id: " + testProduct1.getId() + " not found in cart.")));
    }

    // --- Tests para DELETE /api/cart/clear ---

    @Test
    void clearCart_ShouldRemoveAllItems() throws Exception {
        // Añadir varios ítems
        cartService.addProductToCart(testUser, testProduct1.getId(), 1);
        cartService.addProductToCart(testUser, testProduct2.getId(), 2);

        mockMvc.perform(delete("/api/cart/clear")
                        .header("Authorization", obtainAuthHeader(userToken)))
                .andExpect(status().isNoContent());

        // Verificar en la BD
        org.junit.jupiter.api.Assertions.assertTrue(cartItemRepository.findByCart(
                cartRepository.findByUser(testUser).orElseThrow()).isEmpty());
    }
}