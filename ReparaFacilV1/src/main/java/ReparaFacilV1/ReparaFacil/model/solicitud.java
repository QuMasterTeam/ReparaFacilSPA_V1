package main.java.ReparaFacilV1.ReparaFacil.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private LocalDateTime fechaSolicitud;

    @Column
    private LocalDateTime fechaProgramada;

    @Column(length = 20)
    private String estado; // pendiente, en progreso, completado...

    @Column(length = 255)
    private String comentarioCliente;

    @Column
    private Double costoEstimado;

    @Column
    private Double costoFinal;
}