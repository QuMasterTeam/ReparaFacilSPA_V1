package ReparaFacilV1.ReparaFacil.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SERVICIO")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable=false, unique=true)
    private String nombreServicio;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable=false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(length = 50, nullable=false)
    @Enumerated(EnumType.STRING)
    private CategoriaServicio categoria;

    @Column
    private Integer duracionEstimada; // en minutos

    public Servicio(Integer id, String nombreServicio, String descripcion, BigDecimal precioBase,
            CategoriaServicio categoria, Integer duracionEstimada) {
        this.id = id;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.categoria = categoria;
        this.duracionEstimada = duracionEstimada;
    }

    // Enum para categorías de servicio
    public enum CategoriaServicio {
        ELECTRODOMESTICOS,
        PLOMERIA,
        ELECTRICIDAD,
        CARPINTERIA,
        PINTURA,
        LIMPIEZA,
        JARDINERIA,
        OTROS
    }
}