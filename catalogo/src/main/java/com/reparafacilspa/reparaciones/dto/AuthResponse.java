package com.reparafacilspa.reparaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de operaciones de autenticación")
public class AuthResponse {
    
    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;
    
    @Schema(description = "Mensaje descriptivo del resultado", example = "Login exitoso")
    private String message;
    
    @Schema(description = "Información del usuario autenticado (si es exitoso)")
    private UserInfo user;
    
    @Schema(description = "Token de sesión generado (si es exitoso)", example = "abc123-456789")
    private String sessionToken;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Información básica del usuario autenticado")
    public static class UserInfo {
        
        @Schema(description = "ID único del usuario", example = "1")
        private Long id;
        
        @Schema(description = "Nombre de usuario", example = "admin")
        private String username;
        
        @Schema(description = "Correo electrónico", example = "admin@reparafacil.com")
        private String email;
        
        @Schema(description = "Nombre real", example = "Administrador")
        private String nombre;
        
        @Schema(description = "Apellido", example = "Sistema")
        private String apellido;
        
        @Schema(description = "Nombre completo", example = "Administrador Sistema")
        public String getNombreCompleto() {
            return nombre + " " + apellido;
        }
        
        @Schema(description = "Teléfono", example = "+56912345678")
        private String telefono;
        
        @Schema(description = "Rol del usuario", example = "ADMIN", allowableValues = {"ADMIN", "EMPRENDEDOR", "CLIENTE"})
        private String rol;
        
        @Schema(description = "Estado activo del usuario", example = "true")
        private Boolean activo;
    }

    // Métodos estáticos para crear respuestas
    public static AuthResponse success(String message, UserInfo user, String sessionToken) {
        return new AuthResponse(true, message, user, sessionToken);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null, null);
    }
}