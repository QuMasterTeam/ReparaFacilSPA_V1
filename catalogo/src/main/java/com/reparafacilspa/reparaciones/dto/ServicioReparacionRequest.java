package com.reparafacilspa.reparaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioReparacionRequest {
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombreCliente;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    private String telefono;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 100, message = "El email no puede tener más de 100 caracteres")
    private String email;
    
    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Size(max = 50, message = "El tipo de dispositivo no puede tener más de 50 caracteres")
    private String tipoDispositivo;
    
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede tener más de 50 caracteres")
    private String marca;
    
    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 100, message = "El modelo no puede tener más de 100 caracteres")
    private String modelo;
    
    @NotBlank(message = "La descripción del problema es obligatoria")
    @Size(max = 1000, message = "La descripción no puede tener más de 1000 caracteres")
    private String descripcionProblema;
    
    @NotNull(message = "La fecha agendada es obligatoria")
    private Date fechaAgendada;
}