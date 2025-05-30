package com.reparafacilspa.reparaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVICIOS_REPARACION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioReparacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE_CLIENTE", nullable = false, length = 100)
    private String nombreCliente;

    @Column(name = "TELEFONO", nullable = false, length = 20)
    private String telefono;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "TIPO_DISPOSITIVO", nullable = false, length = 50)
    private String tipoDispositivo;

    @Column(name = "MARCA", nullable = false, length = 50)
    private String marca;

    @Column(name = "MODELO", nullable = false, length = 100)
    private String modelo;

    @Column(name = "DESCRIPCION_PROBLEMA", nullable = false, length = 1000)
    private String descripcionProblema;

    @Column(name = "FECHA_AGENDADA", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAgendada;

    @Column(name = "FECHA_CREACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "ESTADO", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EstadoReparacion estado = EstadoReparacion.AGENDADO;

    @Column(name = "TECNICO_ASIGNADO", length = 100)
    private String tecnicoAsignado;

    @Column(name = "COSTO_ESTIMADO", precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "COSTO_FINAL", precision = 10, scale = 2)
    private BigDecimal costoFinal;

    @Column(name = "OBSERVACIONES", length = 500)
    private String observaciones;

    @Column(name = "FECHA_INICIO_REPARACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicioReparacion;

    @Column(name = "FECHA_FIN_REPARACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFinReparacion;

    @Column(name = "PRIORIDAD", length = 20)
    @Enumerated(EnumType.STRING)
    private PrioridadReparacion prioridad = PrioridadReparacion.NORMAL;

    @Column(name = "GARANTIA_DIAS")
    private Integer garantiaDias = 30;

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    // Enums para estados y prioridades
    public enum EstadoReparacion {
        AGENDADO, EN_REVISION, EN_REPARACION, ESPERANDO_REPUESTOS, 
        COMPLETADO, ENTREGADO, CANCELADO, EN_GARANTIA
    }

    public enum PrioridadReparacion {
        BAJA, NORMAL, ALTA, URGENTE
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    // Método auxiliar para obtener descripción del estado
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

    // Método auxiliar para calcular días transcurridos
    public long getDiasTranscurridos() {
        if (fechaCreacion == null) return 0;
        long diff = new Date().getTime() - fechaCreacion.getTime();
        return diff / (24 * 60 * 60 * 1000);
    }
}