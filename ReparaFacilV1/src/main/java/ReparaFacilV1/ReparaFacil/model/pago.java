package ReparaFacilV1.ReparaFacil.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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