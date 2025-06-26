package com.ilich.sb.e_commerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de E-commerce",
                version = "1.0",
                description = "Documentación de la API de E-commerce con Spring Boot y JWT para la gestión de productos, categorías y autenticación de usuarios.",
                termsOfService = "http://swagger.io/terms/",
                contact = @Contact(
                        name = "Ilich Rondon",
                        email = "ilichrondon@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor Local de Desarrollo")
                // Puedes añadir más servidores aquí (ej. producción, staging)
        }
)
@SecurityScheme(
        name = "bearerAuth", // Nombre que usarás para referenciar este esquema de seguridad
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT", // Indica que el formato del token es JWT
        description = "Se requiere un token JWT para acceder a los endpoints protegidos. Incluye 'Bearer ' antes del token."
)
public class OpenApiConfig {
    // Esta clase no necesita ningún método, las anotaciones hacen todo el trabajo.
}