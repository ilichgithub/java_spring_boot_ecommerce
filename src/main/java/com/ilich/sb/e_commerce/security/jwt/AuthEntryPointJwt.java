package com.ilich.sb.e_commerce.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper; // Para convertir objetos a JSON
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType; // Para establecer el tipo de contenido de la respuesta
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        // Registra el error para depuración en el servidor
        logger.error("Error de autenticación: {}", authException.getMessage());

        // Establece el código de estado HTTP a 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Establece el tipo de contenido de la respuesta a JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Crea un mapa para la respuesta JSON
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage()); // Mensaje de la excepción de autenticación
        body.put("path", request.getServletPath()); // Ruta donde ocurrió el error

        // Escribe la respuesta JSON al flujo de salida de la respuesta
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}