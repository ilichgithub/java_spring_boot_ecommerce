package com.ilich.sb.e_commerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        // Registra el error para depuración en el servidor
        logger.error("Error de acceso denegado: {}", accessDeniedException.getMessage());

        // Establece el código de estado HTTP a 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // Establece el tipo de contenido de la respuesta a JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Crea un mapa para la respuesta JSON
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "Forbidden");
        body.put("message", accessDeniedException.getMessage()); // Mensaje de la excepción de acceso denegado
        body.put("path", request.getServletPath()); // Ruta donde ocurrió el error

        // Escribe la respuesta JSON al flujo de salida de la respuesta
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}