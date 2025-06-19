package com.reparafacilspa.reparaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "USUARIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa un usuario del sistema ReparaFacilSPA")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    @Schema(description = "Nombre de usuario único en el sistema", example = "admin", required = true)
    private String username;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    @Schema(description = "Dirección de correo electrónico del usuario", example = "admin@reparafacil.com", required = true)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    @Schema(description = "Contraseña encriptada del usuario", hidden = true)
    private String password;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    @Schema(description = "Nombre real del usuario", example = "Juan", required = true)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 100)
    @Schema(description = "Apellido del usuario", example = "Pérez", required = true)
    private String apellido;

    @Column(name = "TELEFONO", length = 20)
    @Schema(description = "Número de teléfono del usuario", example = "+56912345678")
    private String telefono;

    @Column(name = "ROL", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Rol del usuario en el sistema", example = "CLIENTE", allowableValues = {"ADMIN", "EMPRENDEDOR", "CLIENTE"})
    private UserRole rol = UserRole.CLIENTE;

    @Column(name = "ACTIVO")
    @Schema(description = "Indica si el usuario está activo en el sistema", example = "true")
    private Boolean activo = true;

    @Column(name = "FECHA_CREACION")
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Fecha y hora de creación del usuario", example = "2024-01-15T10:30:00")
    private Date fechaCreacion;

    @Column(name = "ULTIMO_LOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Fecha y hora del último inicio de sesión", example = "2024-01-15T10:30:00")
    private Date ultimoLogin;

    @Column(name = "INTENTOS_LOGIN")
    @Schema(description = "Número de intentos de login fallidos", example = "0")
    private Integer intentosLogin = 0;

    @Column(name = "CUENTA_BLOQUEADA")
    @Schema(description = "Indica si la cuenta está bloqueada por múltiples intentos fallidos", example = "false")
    private Boolean cuentaBloqueada = false;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @Schema(description = "Roles disponibles para los usuarios")
    public enum UserRole {
        @Schema(description = "Administrador del sistema con acceso completo")
        ADMIN, 
        @Schema(description = "Técnico o emprendedor que realiza reparaciones")
        EMPRENDEDOR, 
        @Schema(description = "Cliente que solicita servicios de reparación")
        CLIENTE
    }

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
