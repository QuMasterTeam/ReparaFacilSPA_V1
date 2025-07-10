package com.reparafacilspa.reparaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para actualizar un usuario existente")
public class UpdateUserRequest {
    
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Schema(description = "Nombre de usuario único", example = "usuarioactualizado", required = true)
    private String username;
    
    @Email(message = "Debe ser un email válido")
    @Schema(description = "Dirección de correo electrónico", example = "actualizado@email.com", required = true)
    private String email;
    
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Nueva contraseña (opcional - dejar vacío para mantener actual)", example = "nuevapassword")
    private String password;
    
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Schema(description = "Nombre real del usuario", example = "Juan Actualizado", required = true)
    private String nombre;
    
    @Size(max = 100, message = "El apellido no puede tener más de 100 caracteres")
    @Schema(description = "Apellido del usuario", example = "Pérez Actualizado", required = true)
    private String apellido;
    
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    @Schema(description = "Número de teléfono", example = "+56987654321")
    private String telefono;
    
    @Schema(description = "Rol del usuario en el sistema", example = "CLIENTE", 
            allowableValues = {"ADMIN", "EMPRENDEDOR", "CLIENTE"})
    private String rol;
    
    @Schema(description = "Estado activo del usuario", example = "true")
    private Boolean activo;
}