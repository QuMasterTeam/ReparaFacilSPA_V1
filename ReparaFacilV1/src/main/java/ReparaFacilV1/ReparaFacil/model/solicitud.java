package ReparaFacilV1.ReparaFacil.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SOLICITUD")
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable=false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "servicio_id", nullable=false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @Column(nullable=false)
    @CreationTimestamp
    private LocalDateTime fechaSolicitud;

    @Column
    private LocalDateTime fechaProgramada;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    @Column(length = 255)
    private String comentarioCliente;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoFinal;

    // Enum para estados de solicitud
    public enum EstadoSolicitud {
        PENDIENTE,
        EN_PROGRESO,
        COMPLETADO,
        CANCELADO
    }
}