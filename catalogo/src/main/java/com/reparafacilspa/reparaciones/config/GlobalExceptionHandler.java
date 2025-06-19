package com.reparafacilspa.reparaciones.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.reparafacilspa.reparaciones.dto.AuthResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// TEMPORALMENTE DESHABILITADO PARA QUE SWAGGER FUNCIONE
// Cuando necesites volver a habilitarlo, descomenta la línea de abajo:
// @RestControllerAdvice
public class GlobalExceptionHandler {

    // Manejar errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(AuthResponse.error("Errores de validación: " + errors));
    }

    // Manejar errores generales de runtime
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Error interno del servidor: " + ex.getMessage()));
    }

    // Manejar errores generales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Error interno del servidor");
        errorResponse.put("timestamp", new java.util.Date());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    // Manejar errores de acceso ilegal
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(AuthResponse.error("Datos inválidos: " + ex.getMessage()));
    }
}