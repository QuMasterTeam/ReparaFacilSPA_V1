package com.reparafacilspa.reparaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para crear un nuevo servicio de reparación")
public class ServicioReparacionRequest {
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Schema(description = "Nombre completo del cliente", example = "Juan Pérez", required = true)
    private String nombreCliente;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    @Schema(description = "Número de teléfono del cliente", example = "+56912345678", required = true)
    private String telefono;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 100, message = "El email no puede tener más de 100 caracteres")
    @Schema(description = "Correo electrónico del cliente", example = "juan@email.com", required = true)
    private String email;
    
    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Size(max = 50, message = "El tipo de dispositivo no puede tener más de 50 caracteres")
    @Schema(description = "Tipo de dispositivo a reparar", example = "Smartphone", required = true,
            allowableValues = {"Smartphone", "Laptop", "Tablet", "Computador", "Smartwatch", "Auriculares", "Consola", "Otro"})
    private String tipoDispositivo;
    
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede tener más de 50 caracteres")
    @Schema(description = "Marca del dispositivo", example = "Samsung", required = true)
    private String marca;
    
    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 100, message = "El modelo no puede tener más de 100 caracteres")
    @Schema(description = "Modelo específico del dispositivo", example = "Galaxy S21", required = true)
    private String modelo;
    
    @NotBlank(message = "La descripción del problema es obligatoria")
    @Size(max = 1000, message = "La descripción no puede tener más de 1000 caracteres")
    @Schema(description = "Descripción detallada del problema", 
            example = "La pantalla no enciende después de una caída", required = true)
    private String descripcionProblema;
    
    @NotNull(message = "La fecha agendada es obligatoria")
    @Schema(description = "Fecha y hora preferida para la revisión", 
            example = "2024-01-20T10:00:00", required = true)
    private Date fechaAgendada;
}