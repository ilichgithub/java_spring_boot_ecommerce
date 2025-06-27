package com.ilich.sb.e_commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.model.Role;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.LoginRequestDTO;
import com.ilich.sb.e_commerce.payload.response.TokenRefreshResponseDTO;
import com.ilich.sb.e_commerce.repository.ICategoryRepository;
import com.ilich.sb.e_commerce.repository.IRefreshTokenRepository;
import com.ilich.sb.e_commerce.repository.IRoleRepository;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc // Configura MockMvc para simular peticiones HTTP
@ActiveProfiles("test") // Usa el perfil "test" (application-test.properties)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Permite @BeforeAll en métodos no estáticos
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Inyecta MockMvc para realizar llamadas HTTP simuladas

    @Autowired
    private ObjectMapper objectMapper; // Para serializar objetos Java a JSON y viceversa

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ICategoryRepository categoryRepository; // Para limpiar la BD entre pruebas y verificar datos
    // Campo estático para almacenar el token de acceso obtenido una vez
    @Autowired
    private IRefreshTokenRepository refreshTokenRepository; // Para interactuar con la BD de roles
    private static String accessTokenUser;
    private static String accessTokenAdmin;

    @BeforeAll // Este método se ejecuta una vez antes de todos los tests de la clase
    void setupGlobal() throws Exception {
        // Asegúrate de que los roles y un usuario existan para el login
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll(); // Limpiar usuarios
        roleRepository.deleteAll(); // Limpiar roles primero

        Role userRoleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User testUser = new User("testuser", encoder.encode("password123"));
        testUser.setRoles(new HashSet<>(Arrays.asList(userRoleUser)));
        userRepository.save(testUser);

        Role userRoleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        User testAdmin = new User("testAdmin", encoder.encode("password124"));
        testAdmin.setRoles(new HashSet<>(Arrays.asList(userRoleAdmin)));
        userRepository.save(testAdmin);

        // Realizar login para obtener el accessToken
        LoginRequestDTO loginUserRequest = new LoginRequestDTO("testuser", "password123");
        MvcResult loginUserResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseUserContent = loginUserResult.getResponse().getContentAsString();
        TokenRefreshResponseDTO loginUserResponse = objectMapper.readValue(responseUserContent, TokenRefreshResponseDTO.class);
        accessTokenUser = loginUserResponse.getAccessToken(); // Almacenar el token para todos los tests

        // Realizar login para obtener el accessToken
        LoginRequestDTO loginAdminRequest = new LoginRequestDTO("testAdmin", "password124");
        MvcResult loginAdminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAdminRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseAdminContent = loginAdminResult.getResponse().getContentAsString();
        TokenRefreshResponseDTO loginAdminResponse = objectMapper.readValue(responseAdminContent, TokenRefreshResponseDTO.class);
        accessTokenAdmin = loginAdminResponse.getAccessToken(); // Almacenar el token para todos los tests

        // Opcional: Imprimir el token para depuración (eliminar en producción)
        System.out.println("Access Token obtained for CategoryControllerIntegrationTest: " + accessTokenUser);
        System.out.println("Access Token obtained for CategoryControllerIntegrationTest: " + accessTokenAdmin);
    }


    @BeforeEach // Este método se ejecuta antes de CADA test
    void setUpPerTest() {
        // Limpiar la BD de categorías antes de cada test para un estado limpio
        categoryRepository.deleteAll();
    }

    @Test
    void testGetAllCategories_Authenticated() throws Exception {
        // Pre-carga algunas categorías para el test
        Category cat1 = new Category("Electronics");
        Category cat2 = new Category("Books");
        categoryRepository.saveAll(Arrays.asList(cat1, cat2));

        // Realizar la petición GET a /api/categories usando el token obtenido en @BeforeAll
        mockMvc.perform(get("/api/category/getAll")
                        .header("Authorization", "Bearer " + accessTokenUser) // Usar el token almacenado
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Electronics")))
                .andExpect(jsonPath("$[1].name", is("Books")));
    }

    @Test
    void testGetCategoryById_Authenticated() throws Exception {
        Category savedCategory = categoryRepository.save(new Category("Electronics"));

        mockMvc.perform(get("/api/category/getById/{id}", savedCategory.getId())
                        .header("Authorization", "Bearer " + accessTokenUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    void testGetCategoryById_NotFound() throws Exception {
        // ID que sabemos que no existe
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/category/getById/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + accessTokenUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera 404 Not Found
    }
    // Necesitarás un usuario con rol ADMIN para este test, o ajustar la seguridad
    // para que ROLE_USER pueda crear categorías si esa es tu lógica de negocio.
    // Para simplificar, asumiremos que un 'testuser' con ROLE_USER tiene permisos.
    // Si solo ADMIN puede, deberías modificar setupGlobal para loguear un admin o mockear el usuario.
    @Test
    void testCreateCategory_AuthenticatedUser() throws Exception {
        String newCategoryName = "Home Decor";
        Category newCategory = new Category(newCategoryName);

        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + accessTokenAdmin) // Usar el token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated()) // Espera 201 Created
                .andExpect(jsonPath("$.name", is(newCategoryName))); // Verifica el nombre en la respuesta

        // Opcional: Verifica que la categoría realmente se guardó en la BD

        Optional<Category> savedCategory = categoryRepository.findByName(newCategoryName);
        assertTrue(savedCategory.isPresent());
        assertEquals(newCategoryName, savedCategory.get().getName());

    }

    @Test
    void testUpdateCategory_Authenticated() throws Exception {
        Category existingCategory = categoryRepository.save(new Category("Original Name"));
        Category updatedCategory = new Category("Updated Name");
        updatedCategory.setId(existingCategory.getId());

        mockMvc.perform(put("/api/category/{id}", existingCategory.getId())
                        .header("Authorization", "Bearer " + accessTokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));

        Optional<Category> categoryAfterUpdate = categoryRepository.findById(existingCategory.getId());
        assertTrue(categoryAfterUpdate.isPresent());
        assertEquals("Updated Name", categoryAfterUpdate.get().getName());
    }

    @Test
    void testUpdateCategory_NotFound() throws Exception {
        Long nonExistentId = 999L;
        Category updatedCategory = new Category("Updated Name");
        updatedCategory.setId(nonExistentId);

        mockMvc.perform(put("/api/category/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + accessTokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCategory_Authenticated() throws Exception {
        Category categoryToDelete = categoryRepository.save(new Category("ToDelete"));

        mockMvc.perform(delete("/api/category/{id}", categoryToDelete.getId())
                        .header("Authorization", "Bearer " + accessTokenAdmin))
                .andExpect(status().isNoContent()); // Espera 204 No Content para eliminación exitosa

        assertFalse(categoryRepository.findById(categoryToDelete.getId()).isPresent());
    }

    @Test
    void testDeleteCategory_NotFound() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(delete("/api/category/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + accessTokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCategories_Unauthorized() throws Exception {
        // Sin token
        mockMvc.perform(get("/api/category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCategory_Unauthorized() throws Exception {
        String newCategoryName = "Unauthorized Attempt";
        Category newCategory = new Category(newCategoryName);

        // Sin token
        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCategoryById_Unauthorized() throws Exception {
        Category savedCategory = categoryRepository.save(new Category( "Unauthorized Test"));
        // Sin token
        mockMvc.perform(get("/api/category/getById/{id}", savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCategory_Unauthorized() throws Exception {
        Category existingCategory = categoryRepository.save(new Category("Unauthorized Update Test"));
        Category updatedCategory = new Category("Updated Unauth Name");

        // Sin token
        mockMvc.perform(put("/api/category/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteCategory_Unauthorized() throws Exception {
        Category categoryToDelete = categoryRepository.save(new Category("Unauthorized Delete Test"));
        // Sin token
        mockMvc.perform(delete("/api/category/{id}", categoryToDelete.getId()))
                .andExpect(status().isUnauthorized());
    }

}