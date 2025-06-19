package com.reparafacilspa.reparaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVICIOS_REPARACION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa un servicio de reparación de dispositivos")
public class ServicioReparacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @Schema(description = "Identificador único del servicio", example = "1")
    private Long id;

    @Column(name = "NOMBRE_CLIENTE", nullable = false, length = 100)
    @Schema(description = "Nombre completo del cliente", example = "Juan Pérez", required = true)
    private String nombreCliente;

    @Column(name = "TELEFONO", nullable = false, length = 20)
    @Schema(description = "Número de teléfono del cliente", example = "+56912345678", required = true)
    private String telefono;

    @Column(name = "EMAIL", nullable = false, length = 100)
    @Schema(description = "Correo electrónico del cliente", example = "juan@email.com", required = true)
    private String email;

    @Column(name = "TIPO_DISPOSITIVO", nullable = false, length = 50)
    @Schema(description = "Tipo de dispositivo a reparar", example = "Smartphone", required = true)
    private String tipoDispositivo;

    @Column(name = "MARCA", nullable = false, length = 50)
    @Schema(description = "Marca del dispositivo", example = "Samsung", required = true)
    private String marca;

    @Column(name = "MODELO", nullable = false, length = 100)
    @Schema(description = "Modelo específico del dispositivo", example = "Galaxy S21", required = true)
    private String modelo;

    @Column(name = "DESCRIPCION_PROBLEMA", nullable = false, length = 1000)
    @Schema(description = "Descripción detallada del problema del dispositivo", 
            example = "La pantalla no enciende después de una caída", required = true)
    private String descripcionProblema;

    @Column(name = "FECHA_AGENDADA", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Fecha y hora agendada para la revisión", example = "2024-01-20T10:00:00", required = true)
    private Date fechaAgendada;

    @Column(name = "FECHA_CREACION")
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Fecha y hora de creación del servicio", example = "2024-01-15T10:30:00")
    private Date fechaCreacion;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del servicio", example = "AGENDADO")
    private EstadoReparacion estado = EstadoReparacion.AGENDADO;

    @Column(name = "TECNICO_ASIGNADO", length = 100)
    @Schema(description = "Nombre del técnico asignado al servicio", example = "Carlos González")
    private String tecnicoAsignado;

    @Column(name = "COSTO_ESTIMADO", precision = 10, scale = 2)
    @Schema(description = "Costo estimado de la reparación en pesos chilenos", example = "45000.00")
    private BigDecimal costoEstimado;

    @Column(name = "COSTO_FINAL", precision = 10, scale = 2)
    @Schema(description = "Costo final de la reparación en pesos chilenos", example = "42000.00")
    private BigDecimal costoFinal;

    @Column(name = "OBSERVACIONES", length = 500)
    @Schema(description = "Observaciones adicionales del técnico", 
            example = "Se requiere cambio de pantalla completa")
    private String observaciones;

    @Column(name = "FECHA_INICIO_REPARACION")
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Fecha y hora de inicio de la reparación", example = "2024-01-20T11:00:00")
    private Date fechaInicioReparacion;

    @Column(name = "FECHA_FIN_REPARACION")
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Fecha y hora de finalización de la reparación", example = "2024-01-22T15:30:00")
    private Date fechaFinReparacion;

    @Column(name = "PRIORIDAD", length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Prioridad del servicio", example = "NORMAL")
    private PrioridadReparacion prioridad = PrioridadReparacion.NORMAL;

    @Column(name = "GARANTIA_DIAS")
    @Schema(description = "Días de garantía ofrecidos", example = "30")
    private Integer garantiaDias = 30;

    @Column(name = "ACTIVO")
    @Schema(description = "Indica si el servicio está activo", example = "true")
    private Boolean activo = true;

    @Schema(description = "Estados posibles para un servicio de reparación")
    public enum EstadoReparacion {
        @Schema(description = "Servicio agendado, esperando revisión")
        AGENDADO, 
        @Schema(description = "En proceso de revisión técnica")
        EN_REVISION, 
        @Schema(description = "En proceso de reparación")
        EN_REPARACION, 
        @Schema(description = "Esperando llegada de repuestos")
        ESPERANDO_REPUESTOS, 
        @Schema(description = "Reparación completada")
        COMPLETADO, 
        @Schema(description = "Dispositivo entregado al cliente")
        ENTREGADO, 
        @Schema(description = "Servicio cancelado")
        CANCELADO, 
        @Schema(description = "Servicio en garantía")
        EN_GARANTIA
    }

    @Schema(description = "Niveles de prioridad para los servicios")
    public enum PrioridadReparacion {
        @Schema(description = "Prioridad baja")
        BAJA, 
        @Schema(description = "Prioridad normal")
        NORMAL, 
        @Schema(description = "Prioridad alta")
        ALTA, 
        @Schema(description = "Prioridad urgente")
        URGENTE
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @Schema(description = "Descripción legible del estado actual", example = "Agendado - Esperando revisión")
    public String getEstadoDescripcion() {
        switch (estado) {
            case AGENDADO: return "Agendado - Esperando revisión";
            case EN_REVISION: return "En revisión técnica";
            case EN_REPARACION: return "En proceso de reparación";
            case ESPERANDO_REPUESTOS: return "Esperando repuestos";
            case COMPLETADO: return "Reparación completada";
            case ENTREGADO: return "Entregado al cliente";
            case CANCELADO: return "Servicio cancelado";
            case EN_GARANTIA: return "En servicio de garantía";
            default: return estado.name();
        }
    }

    @Schema(description = "Número de días transcurridos desde la creación", example = "5")
    public long getDiasTranscurridos() {
        if (fechaCreacion == null) return 0;
        long diff = new Date().getTime() - fechaCreacion.getTime();
        return diff / (24 * 60 * 60 * 1000);
    }
}