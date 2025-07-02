package com.ilich.sb.e_commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilich.sb.e_commerce.model.RefreshToken;
import com.ilich.sb.e_commerce.model.Role;
import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.payload.request.LoginRequestDTO;
import com.ilich.sb.e_commerce.payload.request.RegisterRequestDTO;
import com.ilich.sb.e_commerce.payload.request.TokenRefreshRequestDTO;
import com.ilich.sb.e_commerce.payload.response.TokenRefreshResponseDTO;
import com.ilich.sb.e_commerce.repository.IRefreshTokenRepository;
import com.ilich.sb.e_commerce.repository.IRoleRepository;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import com.ilich.sb.e_commerce.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc // Configura MockMvc para simular peticiones HTTP
@ActiveProfiles("test") // Usa el perfil "test" (application-test.properties)
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON

    @Autowired
    private IUserRepository userRepository; // Para interactuar con la BD de usuarios

    @Autowired
    private IRoleRepository roleRepository; // Para interactuar con la BD de roles

    @Autowired
    private IRefreshTokenRepository refreshTokenRepository; // Para interactuar con la BD de roles

    @Autowired
    private PasswordEncoder encoder; // Para codificar contraseñas

    @Autowired
    private RefreshTokenServiceImpl refreshTokenService;

    // @Autowired
    // private AuthService authService; // Si tienes un AuthService, lo puedes inyectar aquí

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setUp() {
        // Limpiar la BD antes de cada test para asegurar un estado limpio
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Crear roles si no existen (Spring Security necesita los roles persistidos)
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        // Crear un usuario de prueba
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
        User user = new User("testuser", encoder.encode("password123"));
        user.getRoles().add(userRole);
        userRepository.save(user);

        // Crear un admin de prueba
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        User admin = new User("adminuser", encoder.encode("adminpassword"));
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
    }

    @Test
    void testAdminLoginSuccess() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("adminuser", "adminpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.username", is("adminuser")))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_ADMIN")));
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistentuser", "anypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // Espera 401 Unauthorized
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void testUserLoginSuccess() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue())) // Verifica que el token no sea nulo
                .andExpect(jsonPath("$.refreshToken", notNullValue())) // Verifica que el refresh token no sea nulo
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_USER"))); // Verifica el rol
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrongpassword"); // Contraseña incorrecta

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // Espera 401 Unauthorized
                .andExpect(jsonPath("$.message", notNullValue())); // O un mensaje de error específico
    }

    @Test
    void testRegisterUserRoleSuccess() throws Exception {
        RegisterRequestDTO signupRequest = new RegisterRequestDTO();
        signupRequest.setUsername("newadmin");
        signupRequest.setPassword("newadminpass");
        //signupRequest.setEmail("newadmin@example.com");
        //signupRequest.setRole(Collections.singleton("admin")); // Asigna el rol "admin"

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Usuario registrado exitosamente!")));

        assertTrue(userRepository.findByUsername("newadmin").isPresent());
        User registeredAdmin = userRepository.findByUsername("newadmin").get();
        assertEquals(1, registeredAdmin.getRoles().size());
        assertTrue(registeredAdmin.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_USER")));
    }


    @Test
    void testRegisterUserUsernameAlreadyExists() throws Exception {
        // Ya tenemos "testuser" creado en setUp()
        RegisterRequestDTO signupRequest = new RegisterRequestDTO();
        signupRequest.setUsername("testuser"); // Username que ya existe
        signupRequest.setPassword("somepassword");
        //signupRequest.setEmail("existing@example.com");
        //signupRequest.setRole(Collections.singleton("user"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest()) // Espera 400 Bad Request
                .andExpect(jsonPath("$.message", is("Error: ¡El nombre de usuario ya está en uso!")));
    }


    @Test
    void testRefreshTokenSuccess() throws Exception {
        // 1. Iniciar sesión para obtener un Refresh Token inicial
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn(); // Guarda el resultado de la petición

        // Extraer el refresh token de la respuesta de login
        String responseContent = loginResult.getResponse().getContentAsString();
        TokenRefreshResponseDTO loginResponse = objectMapper.readValue(responseContent, TokenRefreshResponseDTO.class);
        String initialRefreshToken = loginResponse.getRefreshToken();
        String initialAccessToken = loginResponse.getAccessToken(); // Opcional: si quieres verificar que es diferente

        // 2. Usar el Refresh Token para obtener un nuevo Access Token
        TokenRefreshResponseDTO refreshRequest = new TokenRefreshResponseDTO();
        refreshRequest.setRefreshToken(initialRefreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue())) // Espera un nuevo access token
                .andExpect(jsonPath("$.refreshToken", notNullValue())) // Espera un nuevo refresh token (no nulo)
                .andReturn(); // Guarda el resultado de la segunda petición

        // 3. Extraer el NUEVO refresh token de la respuesta de refresh
        String refreshResponseContent = refreshResult.getResponse().getContentAsString();
        TokenRefreshResponseDTO refreshResponse = objectMapper.readValue(refreshResponseContent, TokenRefreshResponseDTO.class);
        String newRefreshToken = refreshResponse.getRefreshToken();
        String newAccessToken = refreshResponse.getAccessToken();

        // 4. Hacer las aserciones sobre los tokens
        // Verifica que el nuevo refresh token NO sea el mismo que el inicial (si tu lógica es de rotación)
        assertNotEquals(initialRefreshToken, newRefreshToken, "El Refresh Token debería ser diferente después de la rotación.");

        // Opcional: Verifica que el nuevo access token NO sea el mismo que el inicial
        assertNotEquals(initialAccessToken, newAccessToken, "El Access Token debería ser diferente después de la rotación.");

        // Opcional: Puedes verificar que el refresh token inicial haya sido invalidado si tu servicio lo hace
        assertTrue(refreshTokenService.findByToken(initialRefreshToken).isEmpty(), "El refresh token inicial debería haber sido invalidado.");
        // O: assertFalse(refreshTokenRepository.existsByToken(initialRefreshToken)); // Si no tienes el servicio
    }
    @Test
    void testRefreshTokenNotFound() throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO();
        refreshRequest.setRefreshToken("nonexistentrefreshtoken"); // Token que no existe

        //MvcResult mvcResult =
                mockMvc.perform(post("/api/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isForbidden()) // O 401 Unauthorized, depende de cómo manejes TokenRefreshException
                .andExpect(jsonPath("$.message", is("Full authentication is required to access this resource")));
                //.andReturn(); // Verifica el mensaje de error

        //assertNotEquals(refreshRequest, mvcResult, "El Refresh Token debería ser diferente después de la rotación.");
    }


    @Test
    void testRefreshTokenExpired() throws Exception {
        // 1. Crear un usuario
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
        User expiredUser = new User("expireduser", encoder.encode("pass"));
        expiredUser.getRoles().add(userRole);
        userRepository.save(expiredUser);

        // 2. Crear un Refresh Token expirado manualmente para este usuario
        RefreshToken expiredToken = refreshTokenService.createRefreshToken(expiredUser);
        // Simula la expiración (ej. restando mucho tiempo a la fecha actual, o seteando una fecha pasada)
        expiredToken.setExpiryDate(java.time.Instant.now().minusSeconds(10000));
        refreshTokenRepository.save(expiredToken); // Guarda el token expirado

        // 3. Intentar refrescar con el token expirado
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO();
        refreshRequest.setRefreshToken(expiredToken.getToken());

        mockMvc.perform(post("/api/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isForbidden()) // O 401 Unauthorized, dependiendo de cómo manejes TokenRefreshException
                .andExpect(jsonPath("$.message", notNullValue())); // Verifica que haya un mensaje de error
    }
}