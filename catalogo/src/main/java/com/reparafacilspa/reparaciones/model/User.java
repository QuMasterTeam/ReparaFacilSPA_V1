package com.reparafacilspa.reparaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "USUARIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "ROL", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole rol = UserRole.CLIENTE;

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @Column(name = "FECHA_CREACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "ULTIMO_LOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimoLogin;

    @Column(name = "INTENTOS_LOGIN")
    private Integer intentosLogin = 0;

    @Column(name = "CUENTA_BLOQUEADA")
    private Boolean cuentaBloqueada = false;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    // Enum para roles
    public enum UserRole {
        ADMIN, EMPRENDEDOR, CLIENTE
    }

    // Método auxiliar para obtener nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
