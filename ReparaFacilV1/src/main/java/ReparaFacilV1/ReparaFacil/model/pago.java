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
@Table(name = "PAGO")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "solicitud_id", nullable=false)
    private Solicitud solicitud;

    @Column(nullable=false)
    private Double monto;

    @Column(nullable=false)
    private LocalDateTime fechaPago;

    @Column(length = 20)
    private String metodoPago; // tarjeta, efectivo, etc.

    @Column(length = 20)
    private String estadoPago;
}