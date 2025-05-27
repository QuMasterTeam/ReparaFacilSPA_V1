package main.java.ReparaFacilV1.ReparaFacil.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SERVICIO")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable=false)
    private String nombreServicio;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable=false)
    private Double precioBase;

    @Column(length = 50)
    private String categoria;

    @Column
    private Integer duracionEstimada; // en minutos
}