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
@Table(name = "VALORACION")
public class Valoracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable=false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id", nullable=false)
    private Tecnico tecnico;

    @Column(nullable=false)
    private Integer puntuacion;

    @Column(length = 255)
    private String comentario;

    @Column(nullable=false)
    private LocalDateTime fechaValoracion;
}