package com.reparafacilspa.reparaciones.dto;

import com.reparafacilspa.reparaciones.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private UserInfo user;
    private String sessionToken;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nombre;
        private String apellido;
        private String nombreCompleto;
        private String telefono;
        private String rol;
        private Boolean activo;

        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.nombre = user.getNombre();
            this.apellido = user.getApellido();
            this.nombreCompleto = user.getNombreCompleto();
            this.telefono = user.getTelefono();
            this.rol = user.getRol().name();
            this.activo = user.getActivo();
        }
    }

    // Constructor para respuesta exitosa
    public static AuthResponse success(User user, String sessionToken) {
        return new AuthResponse(true, "Login exitoso", new UserInfo(user), sessionToken);
    }

    // Constructor para respuesta de error
    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null, null);
    }
}