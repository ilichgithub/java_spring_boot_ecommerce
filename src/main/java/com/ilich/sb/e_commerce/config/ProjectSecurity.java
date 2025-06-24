package com.ilich.sb.e_commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ilich.sb.e_commerce.security.jwt.AuthTokenFilter;
import com.ilich.sb.e_commerce.service.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ProjectSecurity {

    private final UserDetailsServiceImpl userDetailsService; // Inyecta tu UserDetailsService

    // Inyectar UserDetailsService a través del constructor
    public ProjectSecurity(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Define AuthTokenFilter como un Bean
    
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }
    
    // Este bean define cómo se codificarán las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean para el AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Bean para el proveedor de autenticación
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Configura tu UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());    // Configura tu PasswordEncoder
        return authProvider;
    }

    // Este bean configura las reglas de autorización HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST sin estado (ya que usaremos JWT)
            .authorizeHttpRequests(authorize -> authorize
                // Permite el acceso sin autenticación a los endpoints de autenticación y registro
                .requestMatchers("/api/auth/**").permitAll()
                // Toda otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                // Hace que la aplicación sea sin estado (cada petición incluye el token JWT)
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

            http.authenticationProvider(authenticationProvider());
            // ¡Añade tu filtro JWT antes del filtro de autenticación de usuario/contraseña de Spring Security!
            http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
