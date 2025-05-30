package com.reparafacilspa.reparaciones.repository;

import com.reparafacilspa.reparaciones.model.ServicioReparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface ServicioReparacionRepository extends JpaRepository<ServicioReparacion, Long> {
    
    // Buscar servicios activos
    List<ServicioReparacion> findByActivoTrue();
    
    // Buscar por email del cliente
    List<ServicioReparacion> findByEmailAndActivoTrue(String email);
    
    // Buscar por estado
    List<ServicioReparacion> findByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion estado);
    
    // Buscar por tipo de dispositivo
    List<ServicioReparacion> findByTipoDispositivoAndActivoTrue(String tipoDispositivo);
    
    // Buscar por técnico asignado
    List<ServicioReparacion> findByTecnicoAsignadoAndActivoTrue(String tecnicoAsignado);
    
    // Buscar por prioridad
    List<ServicioReparacion> findByPrioridadAndActivoTrue(ServicioReparacion.PrioridadReparacion prioridad);
    
    // Buscar por rango de fechas
    List<ServicioReparacion> findByFechaAgendadaBetweenAndActivoTrue(Date fechaInicio, Date fechaFin);
    
    // Buscar por cliente (teléfono o email)
    List<ServicioReparacion> findByTelefonoOrEmailAndActivoTrue(String telefono, String email);
    
    // Contar por estado
    long countByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion estado);
    
    // Contar por tipo de dispositivo
    long countByTipoDispositivoAndActivoTrue(String tipoDispositivo);
}