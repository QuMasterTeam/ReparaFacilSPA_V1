package com.reparafacilspa.reparaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.reparafacilspa.reparaciones.model.ServicioReparacion;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información completa de un servicio de reparación")
public class ServicioReparacionResponse {
    
    @Schema(description = "ID único del servicio", example = "1")
    private Long id;
    
    @Schema(description = "Nombre completo del cliente", example = "Juan Pérez")
    private String nombreCliente;
    
    @Schema(description = "Teléfono del cliente", example = "+56912345678")
    private String telefono;
    
    @Schema(description = "Email del cliente", example = "juan@email.com")
    private String email;
    
    @Schema(description = "Tipo de dispositivo", example = "Smartphone")
    private String tipoDispositivo;
    
    @Schema(description = "Marca del dispositivo", example = "Samsung")
    private String marca;
    
    @Schema(description = "Modelo del dispositivo", example = "Galaxy S21")
    private String modelo;
    
    @Schema(description = "Descripción del problema reportado", example = "La pantalla no enciende después de una caída")
    private String descripcionProblema;
    
    @Schema(description = "Fecha y hora agendada para la revisión", example = "2024-01-20T10:00:00")
    private Date fechaAgendada;
    
    @Schema(description = "Fecha y hora de creación del servicio", example = "2024-01-15T10:30:00")
    private Date fechaCreacion;
    
    @Schema(description = "Estado actual del servicio", example = "AGENDADO", 
            allowableValues = {"AGENDADO", "EN_REVISION", "EN_REPARACION", "ESPERANDO_REPUESTOS", 
                             "COMPLETADO", "ENTREGADO", "CANCELADO", "EN_GARANTIA"})
    private String estado;
    
    @Schema(description = "Descripción legible del estado", example = "Agendado - Esperando revisión")
    private String estadoDescripcion;
    
    @Schema(description = "Nombre del técnico asignado", example = "Carlos González")
    private String tecnicoAsignado;
    
    @Schema(description = "Costo estimado de la reparación en CLP", example = "45000.00")
    private BigDecimal costoEstimado;
    
    @Schema(description = "Costo final de la reparación en CLP", example = "42000.00")
    private BigDecimal costoFinal;
    
    @Schema(description = "Observaciones adicionales del técnico", example = "Se requiere cambio de pantalla completa")
    private String observaciones;
    
    @Schema(description = "Fecha y hora de inicio de la reparación", example = "2024-01-20T11:00:00")
    private Date fechaInicioReparacion;
    
    @Schema(description = "Fecha y hora de finalización de la reparación", example = "2024-01-22T15:30:00")
    private Date fechaFinReparacion;
    
    @Schema(description = "Prioridad del servicio", example = "NORMAL", 
            allowableValues = {"BAJA", "NORMAL", "ALTA", "URGENTE"})
    private String prioridad;
    
    @Schema(description = "Días de garantía ofrecidos", example = "30")
    private Integer garantiaDias;
    
    @Schema(description = "Indica si el servicio está activo", example = "true")
    private Boolean activo;
    
    @Schema(description = "Días transcurridos desde la creación", example = "5")
    private Long diasTranscurridos;

    public ServicioReparacionResponse(ServicioReparacion servicio) {
        this.id = servicio.getId();
        this.nombreCliente = servicio.getNombreCliente();
        this.telefono = servicio.getTelefono();
        this.email = servicio.getEmail();
        this.tipoDispositivo = servicio.getTipoDispositivo();
        this.marca = servicio.getMarca();
        this.modelo = servicio.getModelo();
        this.descripcionProblema = servicio.getDescripcionProblema();
        this.fechaAgendada = servicio.getFechaAgendada();
        this.fechaCreacion = servicio.getFechaCreacion();
        this.estado = servicio.getEstado().name();
        this.estadoDescripcion = servicio.getEstadoDescripcion();
        this.tecnicoAsignado = servicio.getTecnicoAsignado();
        this.costoEstimado = servicio.getCostoEstimado();
        this.costoFinal = servicio.getCostoFinal();
        this.observaciones = servicio.getObservaciones();
        this.fechaInicioReparacion = servicio.getFechaInicioReparacion();
        this.fechaFinReparacion = servicio.getFechaFinReparacion();
        this.prioridad = servicio.getPrioridad().name();
        this.garantiaDias = servicio.getGarantiaDias();
        this.activo = servicio.getActivo();
        this.diasTranscurridos = servicio.getDiasTranscurridos();
    }
}