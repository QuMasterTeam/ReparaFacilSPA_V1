package com.reparafacilspa.reparaciones.repository;

import com.reparafacilspa.reparaciones.model.ServicioReparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface ServicioReparacionRepository extends JpaRepository<ServicioReparacion, Long> {
    
    // Métodos existentes
    List<ServicioReparacion> findByActivoTrue();
    List<ServicioReparacion> findByEmailAndActivoTrue(String email);
    List<ServicioReparacion> findByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion estado);
    List<ServicioReparacion> findByTipoDispositivoAndActivoTrue(String tipoDispositivo);
    List<ServicioReparacion> findByTecnicoAsignadoAndActivoTrue(String tecnicoAsignado);
    List<ServicioReparacion> findByPrioridadAndActivoTrue(ServicioReparacion.PrioridadReparacion prioridad);
    List<ServicioReparacion> findByFechaAgendadaBetweenAndActivoTrue(Date fechaInicio, Date fechaFin);
    List<ServicioReparacion> findByTelefonoOrEmailAndActivoTrue(String telefono, String email);
    long countByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion estado);
    long countByTipoDispositivoAndActivoTrue(String tipoDispositivo);
    
    // NUEVOS MÉTODOS PARA HATEOAS - Métodos personalizados basados en la guía
    
    // 1. Obtener todas las reservas en una fecha específica (por fecha agendada)
    @Query("SELECT s FROM ServicioReparacion s WHERE DATE(s.fechaAgendada) = DATE(:fecha) AND s.activo = true")
    List<ServicioReparacion> findByFechaAgendadaAndActivoTrue(@Param("fecha") Date fecha);
    
    // 2. Obtener el total de reservas realizadas por un estudiante (adaptado a cliente por email)
    long countByEmailAndActivoTrue(String email);
    
    // NUEVOS MÉTODOS PERSONALIZADOS ADICIONALES (los 5 faltantes de la guía)
    
    // 1. Obtener todas las reservas de un estudiante en una fecha específica (cliente por email en fecha específica)
    @Query("SELECT s FROM ServicioReparacion s WHERE s.email = :email AND DATE(s.fechaAgendada) = DATE(:fecha) AND s.activo = true")
    List<ServicioReparacion> findByEmailAndFechaAgendadaAndActivoTrue(@Param("email") String email, @Param("fecha") Date fecha);
    
    // 2. Obtener todas las reservas de una sala en un estado específico (técnico asignado en estado específico)
    List<ServicioReparacion> findByTecnicoAsignadoAndEstadoAndActivoTrue(String tecnicoAsignado, ServicioReparacion.EstadoReparacion estado);
    
    // 3. Obtener todas las reservas de un estudiante entre dos fechas (cliente por email entre fechas)
    @Query("SELECT s FROM ServicioReparacion s WHERE s.email = :email AND s.fechaAgendada BETWEEN :fechaInicio AND :fechaFin AND s.activo = true")
    List<ServicioReparacion> findByEmailAndFechaAgendadaBetweenAndActivoTrue(@Param("email") String email, @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    // 4. Obtener todas las reservas de una sala entre dos fechas (técnico asignado entre fechas)
    @Query("SELECT s FROM ServicioReparacion s WHERE s.tecnicoAsignado = :tecnicoAsignado AND s.fechaAgendada BETWEEN :fechaInicio AND :fechaFin AND s.activo = true")
    List<ServicioReparacion> findByTecnicoAsignadoAndFechaAgendadaBetweenAndActivoTrue(@Param("tecnicoAsignado") String tecnicoAsignado, @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    // 5. Obtener el total de reservas realizadas en una sala específica (total por técnico asignado)
    long countByTecnicoAsignadoAndActivoTrue(String tecnicoAsignado);
}