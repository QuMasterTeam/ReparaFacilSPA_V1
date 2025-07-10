package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.model.ServicioReparacion;
import com.reparafacilspa.reparaciones.repository.ServicioReparacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioReparacionServiceV3 {

    @Autowired
    private ServicioReparacionRepository servicioRepository;

    // ===== MÉTODOS EXISTENTES (HEREDADOS DE V2) =====

    public List<ServicioReparacionResponse> findAll() {
        return servicioRepository.findByActivoTrue()
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    public ServicioReparacionResponse save(ServicioReparacionRequest request) {
        ServicioReparacion servicio = new ServicioReparacion();
        servicio.setNombreCliente(request.getNombreCliente());
        servicio.setTelefono(request.getTelefono());
        servicio.setEmail(request.getEmail());
        servicio.setTipoDispositivo(request.getTipoDispositivo());
        servicio.setMarca(request.getMarca());
        servicio.setModelo(request.getModelo());
        servicio.setDescripcionProblema(request.getDescripcionProblema());
        servicio.setFechaAgendada(request.getFechaAgendada());
        servicio.setEstado(ServicioReparacion.EstadoReparacion.AGENDADO);
        servicio.setPrioridad(ServicioReparacion.PrioridadReparacion.NORMAL);
        servicio.setActivo(true);
        servicio.setFechaCreacion(new Date());

        ServicioReparacion savedServicio = servicioRepository.save(servicio);
        return new ServicioReparacionResponse(savedServicio);
    }

    public ServicioReparacionResponse findById(Long id) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        return new ServicioReparacionResponse(servicio);
    }

    // ===== NUEVO MÉTODO UPDATE MEJORADO PARA V3 =====

    public ServicioReparacionResponse update(Long id, ServicioReparacionRequest request) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        
        // Actualizar todos los campos básicos
        servicio.setNombreCliente(request.getNombreCliente());
        servicio.setTelefono(request.getTelefono());
        servicio.setEmail(request.getEmail());
        servicio.setTipoDispositivo(request.getTipoDispositivo());
        servicio.setMarca(request.getMarca());
        servicio.setModelo(request.getModelo());
        servicio.setDescripcionProblema(request.getDescripcionProblema());
        servicio.setFechaAgendada(request.getFechaAgendada());
        
        // Mantener campos que no vienen en el request básico
        // (estado, técnico, costos, etc. se mantienen o se actualizan por otros endpoints)
        
        ServicioReparacion updatedServicio = servicioRepository.save(servicio);
        return new ServicioReparacionResponse(updatedServicio);
    }

    // Método update completo (para uso interno o futuras extensiones)
    public ServicioReparacionResponse updateComplete(Long id, ServicioReparacion servicioDetails) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        
        // Actualizar todos los campos
        servicio.setNombreCliente(servicioDetails.getNombreCliente());
        servicio.setTelefono(servicioDetails.getTelefono());
        servicio.setEmail(servicioDetails.getEmail());
        servicio.setTipoDispositivo(servicioDetails.getTipoDispositivo());
        servicio.setMarca(servicioDetails.getMarca());
        servicio.setModelo(servicioDetails.getModelo());
        servicio.setDescripcionProblema(servicioDetails.getDescripcionProblema());
        servicio.setFechaAgendada(servicioDetails.getFechaAgendada());
        
        // Campos administrativos (solo si se proporcionan)
        if (servicioDetails.getEstado() != null) {
            servicio.setEstado(servicioDetails.getEstado());
        }
        if (servicioDetails.getTecnicoAsignado() != null) {
            servicio.setTecnicoAsignado(servicioDetails.getTecnicoAsignado());
        }
        if (servicioDetails.getCostoEstimado() != null) {
            servicio.setCostoEstimado(servicioDetails.getCostoEstimado());
        }
        if (servicioDetails.getCostoFinal() != null) {
            servicio.setCostoFinal(servicioDetails.getCostoFinal());
        }
        if (servicioDetails.getObservaciones() != null) {
            servicio.setObservaciones(servicioDetails.getObservaciones());
        }
        if (servicioDetails.getPrioridad() != null) {
            servicio.setPrioridad(servicioDetails.getPrioridad());
        }
        if (servicioDetails.getGarantiaDias() != null) {
            servicio.setGarantiaDias(servicioDetails.getGarantiaDias());
        }
        if (servicioDetails.getFechaInicioReparacion() != null) {
            servicio.setFechaInicioReparacion(servicioDetails.getFechaInicioReparacion());
        }
        if (servicioDetails.getFechaFinReparacion() != null) {
            servicio.setFechaFinReparacion(servicioDetails.getFechaFinReparacion());
        }
        
        ServicioReparacion updatedServicio = servicioRepository.save(servicio);
        return new ServicioReparacionResponse(updatedServicio);
    }

    public void delete(Long id) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    // ===== MÉTODOS DE BÚSQUEDA (HEREDADOS DE V2) =====

    public List<ServicioReparacionResponse> findByEmail(String email) {
        return servicioRepository.findByEmailAndActivoTrue(email)
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    public List<ServicioReparacionResponse> findByEstado(String estado) {
        try {
            ServicioReparacion.EstadoReparacion estadoEnum = 
                ServicioReparacion.EstadoReparacion.valueOf(estado.toUpperCase());
            return servicioRepository.findByEstadoAndActivoTrue(estadoEnum)
                    .stream()
                    .map(ServicioReparacionResponse::new)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    public List<ServicioReparacionResponse> findByTipoDispositivo(String tipoDispositivo) {
        return servicioRepository.findByTipoDispositivoAndActivoTrue(tipoDispositivo)
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    public List<ServicioReparacionResponse> search(String busqueda) {
        return servicioRepository.findByActivoTrue()
                .stream()
                .filter(s -> s.getNombreCliente().toLowerCase().contains(busqueda.toLowerCase()) ||
                           s.getDescripcionProblema().toLowerCase().contains(busqueda.toLowerCase()) ||
                           s.getMarca().toLowerCase().contains(busqueda.toLowerCase()) ||
                           s.getModelo().toLowerCase().contains(busqueda.toLowerCase()))
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    public ServicioReparacionResponse cambiarEstado(Long id, String nuevoEstado) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        
        try {
            ServicioReparacion.EstadoReparacion estadoEnum = 
                ServicioReparacion.EstadoReparacion.valueOf(nuevoEstado.toUpperCase());
            servicio.setEstado(estadoEnum);
            
            Date ahora = new Date();
            switch (estadoEnum) {
                case EN_REPARACION:
                    if (servicio.getFechaInicioReparacion() == null) {
                        servicio.setFechaInicioReparacion(ahora);
                    }
                    break;
                case COMPLETADO:
                    servicio.setFechaFinReparacion(ahora);
                    break;
                case ENTREGADO:
                    if (servicio.getFechaFinReparacion() == null) {
                        servicio.setFechaFinReparacion(ahora);
                    }
                    break;
                default:
                    break;
            }
            
            ServicioReparacion updatedServicio = servicioRepository.save(servicio);
            return new ServicioReparacionResponse(updatedServicio);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + nuevoEstado);
        }
    }

    public Map<String, Object> getEstadisticas() {
        List<ServicioReparacion> todosServicios = servicioRepository.findByActivoTrue();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalServicios", todosServicios.size());
        stats.put("serviciosAgendados", servicioRepository.countByEstadoAndActivoTrue(
            ServicioReparacion.EstadoReparacion.AGENDADO));
        stats.put("serviciosEnReparacion", servicioRepository.countByEstadoAndActivoTrue(
            ServicioReparacion.EstadoReparacion.EN_REPARACION));
        stats.put("serviciosCompletados", servicioRepository.countByEstadoAndActivoTrue(
            ServicioReparacion.EstadoReparacion.COMPLETADO));
        
        Map<String, Long> porTipoDispositivo = todosServicios.stream()
                .collect(Collectors.groupingBy(
                    ServicioReparacion::getTipoDispositivo,
                    Collectors.counting()
                ));
        stats.put("serviciosPorTipo", porTipoDispositivo);
        
        Set<String> tecnicos = todosServicios.stream()
                .map(ServicioReparacion::getTecnicoAsignado)
                .filter(tecnico -> tecnico != null && !tecnico.trim().isEmpty())
                .collect(Collectors.toSet());
        stats.put("totalTecnicos", tecnicos.size());
        
        return stats;
    }

    // ===== MÉTODOS PERSONALIZADOS (HEREDADOS DE V2) =====
    
    // 1. Obtener todas las reservas de un técnico específico
    public List<ServicioReparacionResponse> findByTecnicoAsignado(String tecnicoAsignado) {
        return servicioRepository.findByTecnicoAsignadoAndActivoTrue(tecnicoAsignado)
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }
    
    // 2. Obtener todas las reservas en una fecha específica
    public List<ServicioReparacionResponse> findByFechaAgendada(String fechaStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha = sdf.parse(fechaStr);
            return servicioRepository.findByFechaAgendadaAndActivoTrue(fecha)
                    .stream()
                    .map(ServicioReparacionResponse::new)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new RuntimeException("Formato de fecha inválido. Use yyyy-MM-dd");
        }
    }
    
    // 3. Obtener el total de reservas realizadas por un cliente (email)
    public long countByEmail(String email) {
        return servicioRepository.countByEmailAndActivoTrue(email);
    }
    
    // 4. Obtener todas las reservas de un cliente en una fecha específica
    public List<ServicioReparacionResponse> findByEmailAndFechaAgendada(String email, String fechaStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha = sdf.parse(fechaStr);
            return servicioRepository.findByEmailAndFechaAgendadaAndActivoTrue(email, fecha)
                    .stream()
                    .map(ServicioReparacionResponse::new)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new RuntimeException("Formato de fecha inválido. Use yyyy-MM-dd");
        }
    }
    
    // 5. Obtener todas las reservas de un técnico en un estado específico
    public List<ServicioReparacionResponse> findByTecnicoAsignadoAndEstado(String tecnicoAsignado, String estado) {
        try {
            ServicioReparacion.EstadoReparacion estadoEnum = 
                ServicioReparacion.EstadoReparacion.valueOf(estado.toUpperCase());
            return servicioRepository.findByTecnicoAsignadoAndEstadoAndActivoTrue(tecnicoAsignado, estadoEnum)
                    .stream()
                    .map(ServicioReparacionResponse::new)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }
    
    // 6. Obtener todas las reservas de un cliente entre dos fechas
    public List<ServicioReparacionResponse> findByEmailAndFechaAgendadaBetween(String email, String fechaInicioStr, String fechaFinStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaInicio = sdf.parse(fechaInicioStr);
            Date fechaFin = sdf.parse(fechaFinStr);
            return servicioRepository.findByEmailAndFechaAgendadaBetweenAndActivoTrue(email, fechaInicio, fechaFin)
                    .stream()
                    .map(ServicioReparacionResponse::new)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new RuntimeException("Formato de fecha inválido. Use yyyy-MM-dd");
        }
    }
    
    // 7. Obtener todas las reservas de un técnico entre dos fechas
    public List<ServicioReparacionResponse> findByTecnicoAsignadoAndFechaAgendadaBetween(String tecnicoAsignado, String fechaInicioStr, String fechaFinStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaInicio = sdf.parse(fechaInicioStr);
            Date fechaFin = sdf.parse(fechaFinStr);
            return servicioRepository.findByTecnicoAsignadoAndFechaAgendadaBetweenAndActivoTrue(tecnicoAsignado, fechaInicio, fechaFin)
                    .stream()
                    .map(ServicioReparacionResponse::new)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new RuntimeException("Formato de fecha inválido. Use yyyy-MM-dd");
        }
    }
    
    // 8. Obtener el total de reservas realizadas por un técnico específico
    public long countByTecnicoAsignado(String tecnicoAsignado) {
        return servicioRepository.countByTecnicoAsignadoAndActivoTrue(tecnicoAsignado);
    }

    // ===== MÉTODOS ADICIONALES V3 =====

    // Obtener servicios eliminados (soft deleted)
    public List<ServicioReparacionResponse> findDeletedServicios() {
        return servicioRepository.findAll()
                .stream()
                .filter(s -> !s.getActivo())
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    // Restaurar servicio eliminado
    public ServicioReparacionResponse restoreServicio(Long id) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(true);
        ServicioReparacion restoredServicio = servicioRepository.save(servicio);
        return new ServicioReparacionResponse(restoredServicio);
    }

    // Búsqueda avanzada con múltiples filtros
    public List<ServicioReparacionResponse> searchAdvanced(String nombreCliente, String email, 
                                                          String tipoDispositivo, String estado, 
                                                          String tecnicoAsignado) {
        return servicioRepository.findByActivoTrue()
                .stream()
                .filter(s -> {
                    boolean matches = true;
                    
                    if (nombreCliente != null && !nombreCliente.trim().isEmpty()) {
                        matches &= s.getNombreCliente().toLowerCase().contains(nombreCliente.toLowerCase());
                    }
                    if (email != null && !email.trim().isEmpty()) {
                        matches &= s.getEmail().toLowerCase().contains(email.toLowerCase());
                    }
                    if (tipoDispositivo != null && !tipoDispositivo.trim().isEmpty()) {
                        matches &= s.getTipoDispositivo().toLowerCase().contains(tipoDispositivo.toLowerCase());
                    }
                    if (estado != null && !estado.trim().isEmpty()) {
                        matches &= s.getEstado().name().equalsIgnoreCase(estado);
                    }
                    if (tecnicoAsignado != null && !tecnicoAsignado.trim().isEmpty()) {
                        matches &= s.getTecnicoAsignado() != null && 
                                  s.getTecnicoAsignado().toLowerCase().contains(tecnicoAsignado.toLowerCase());
                    }
                    
                    return matches;
                })
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    // Obtener estadísticas detalladas
    public Map<String, Object> getDetailedStatistics() {
        List<ServicioReparacion> todosServicios = servicioRepository.findAll(); // Incluye eliminados
        List<ServicioReparacion> serviciosActivos = servicioRepository.findByActivoTrue();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Estadísticas generales
        stats.put("totalServicios", todosServicios.size());
        stats.put("serviciosActivos", serviciosActivos.size());
        stats.put("serviciosEliminados", todosServicios.size() - serviciosActivos.size());
        
        // Por estado
        Map<String, Long> porEstado = serviciosActivos.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getEstado().name(),
                    Collectors.counting()
                ));
        stats.put("serviciosPorEstado", porEstado);
        
        // Por tipo de dispositivo
        Map<String, Long> porTipo = serviciosActivos.stream()
                .collect(Collectors.groupingBy(
                    ServicioReparacion::getTipoDispositivo,
                    Collectors.counting()
                ));
        stats.put("serviciosPorTipo", porTipo);
        
        // Por prioridad
        Map<String, Long> porPrioridad = serviciosActivos.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getPrioridad().name(),
                    Collectors.counting()
                ));
        stats.put("serviciosPorPrioridad", porPrioridad);
        
        // Por técnico
        Map<String, Long> porTecnico = serviciosActivos.stream()
                .filter(s -> s.getTecnicoAsignado() != null && !s.getTecnicoAsignado().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    ServicioReparacion::getTecnicoAsignado,
                    Collectors.counting()
                ));
        stats.put("serviciosPorTecnico", porTecnico);
        
        return stats;
    }
}