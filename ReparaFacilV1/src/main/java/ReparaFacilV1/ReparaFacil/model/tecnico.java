package ReparaFacilV1.ReparaFacil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TECNICO")
public class Tecnico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable=false)
    private String nombre;

    @Column(length = 50, nullable=false)
    private String apellido;

    @Column(length = 100)
    private String especialidades;

    @Column(length = 100, nullable=false)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column
    private Boolean disponibilidad;

    @Column
    private Double valoracionPromedio;
}