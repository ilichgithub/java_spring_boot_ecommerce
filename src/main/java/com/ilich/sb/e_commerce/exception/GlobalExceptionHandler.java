package com.ilich.sb.e_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Indica que esta clase manejará excepciones globales
public class GlobalExceptionHandler {

    // --- NUEVO MÉTODO PARA BadCredentialsException ---
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Invalid username or password."); // Mensaje genérico por seguridad
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // --- NUEVO MÉTODO PARA BadCredentialsException ---
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<Object> handleTokenRefreshException(
            TokenRefreshException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Full authentication is required to access this resource"); // Mensaje genérico por seguridad
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // --- NUEVO MÉTODO PARA UsernameNotFoundException ---
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value()); // o HttpStatus.NOT_FOUND si prefieres
        body.put("error", "Unauthorized"); // o "Not Found"
        body.put("message", ex.getMessage().concat(" UsernameNotFoundException")); // Usa el mensaje de la excepción: "User Not Found with username: ..."
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED); // Retorna 401
    }

    // Maneja excepciones de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Puedes crear una clase de respuesta de error genérica si lo prefieres
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Validation failed".concat(" MethodArgumentNotValidException")); // Mensaje general
        body.put("details", errors); // Detalles específicos de los errores de campo
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    // Maneja tus RuntimeException personalizadas (ej. Product not found)
    // Asumiendo que tus servicios lanzan RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // O HttpStatus.BAD_REQUEST, según la naturaleza
        body.put("error", "Not Found"); // O "Bad Request"
        body.put("message", ex.getMessage().concat("RuntimeException ")); // Usa el mensaje de tu RuntimeException
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // Decide el HttpStatus basado en el mensaje o si creas excepciones personalizadas
        if (ex.getMessage().contains("not found")) {
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        } else if (ex.getMessage().contains("Quantity must be positive.")) { // Captura esta específica del CartService
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Bad Request");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // Si no coincide con ninguna condición específica, devolver 500 o un 400 genérico
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}