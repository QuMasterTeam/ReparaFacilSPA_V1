package main.java.ReparaFacilV1.ReparaFacil.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
//lombok
@Data

@NoArgsConstructor
@AllArgsConstructor
//Data
@Table(name = "CLIENTE")
public class cliente {
    @Id
    @GenerateValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable=false)
    private String firstname;

    @Column(length = 50, nullable=false)
    private String lastname;
    
    @Column(nullable=false)
    private Integer rut;
    
      @Column(nullable=false)
    private Integer dv;