package com.reparafacilspa.reparaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.reparafacilspa.reparaciones.model.ServicioReparacion;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioReparacionResponse {
    private Long id;
    private String nombreCliente;
    private String telefono;
    private String email;
    private String tipoDispositivo;
    private String marca;
    private String modelo;
    private String descripcionProblema;
    private Date fechaAgendada;
    private Date fechaCreacion;
    private String estado;
    private String estadoDescripcion;
    private String tecnicoAsignado;
    private BigDecimal costoEstimado;
    private BigDecimal costoFinal;
    private String observaciones;
    private Date fechaInicioReparacion;
    private Date fechaFinReparacion;
    private String prioridad;
    private Integer garantiaDias;
    private Boolean activo;
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