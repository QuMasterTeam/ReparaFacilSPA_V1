package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.model.ServicioReparacion;
import com.reparafacilspa.reparaciones.repository.ServicioReparacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioReparacionService {

    @Autowired
    private ServicioReparacionRepository servicioRepository;

    // Obtener todos los servicios activos
    public List<ServicioReparacionResponse> findAll() {
        return servicioRepository.findByActivoTrue()
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    // Crear nuevo servicio de reparación
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

    // Buscar servicio por ID
    public ServicioReparacionResponse findById(Long id) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        return new ServicioReparacionResponse(servicio);
    }

    // Actualizar servicio
    public ServicioReparacionResponse update(Long id, ServicioReparacion servicioDetails) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        
        servicio.setNombreCliente(servicioDetails.getNombreCliente());
        servicio.setTelefono(servicioDetails.getTelefono());
        servicio.setEmail(servicioDetails.getEmail());
        servicio.setTipoDispositivo(servicioDetails.getTipoDispositivo());
        servicio.setMarca(servicioDetails.getMarca());
        servicio.setModelo(servicioDetails.getModelo());
        servicio.setDescripcionProblema(servicioDetails.getDescripcionProblema());
        servicio.setFechaAgendada(servicioDetails.getFechaAgendada());
        servicio.setEstado(servicioDetails.getEstado());
        servicio.setTecnicoAsignado(servicioDetails.getTecnicoAsignado());
        servicio.setCostoEstimado(servicioDetails.getCostoEstimado());
        servicio.setCostoFinal(servicioDetails.getCostoFinal());
        servicio.setObservaciones(servicioDetails.getObservaciones());
        servicio.setPrioridad(servicioDetails.getPrioridad());
        servicio.setGarantiaDias(servicioDetails.getGarantiaDias());
        
        ServicioReparacion updatedServicio = servicioRepository.save(servicio);
        return new ServicioReparacionResponse(updatedServicio);
    }

    // Eliminar servicio (soft delete)
    public void delete(Long id) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    // Buscar servicios por email del cliente
    public List<ServicioReparacionResponse> findByEmail(String email) {
        return servicioRepository.findByEmailAndActivoTrue(email)
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    // Buscar por estado
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

    // Buscar por tipo de dispositivo
    public List<ServicioReparacionResponse> findByTipoDispositivo(String tipoDispositivo) {
        return servicioRepository.findByTipoDispositivoAndActivoTrue(tipoDispositivo)
                .stream()
                .map(ServicioReparacionResponse::new)
                .collect(Collectors.toList());
    }

    // Búsqueda general
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

    // Cambiar estado del servicio
    public ServicioReparacionResponse cambiarEstado(Long id, String nuevoEstado) {
        ServicioReparacion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        
        try {
            ServicioReparacion.EstadoReparacion estadoEnum = 
                ServicioReparacion.EstadoReparacion.valueOf(nuevoEstado.toUpperCase());
            servicio.setEstado(estadoEnum);
            
            // Establecer fechas según el estado
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
                    // No hacer nada para otros estados
                    break;
            }
            
            ServicioReparacion updatedServicio = servicioRepository.save(servicio);
            return new ServicioReparacionResponse(updatedServicio);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + nuevoEstado);
        }
    }

    // Obtener estadísticas del sistema
    public Map<String, Object> getEstadisticas() {
        List<ServicioReparacion> todosServicios = servicioRepository.findByActivoTrue();
        Map<String, Object> stats = new HashMap<>();
        
        // Estadísticas generales
        stats.put("totalServicios", todosServicios.size());
        stats.put("serviciosAgendados", servicioRepository.countByEstadoAndActivoTrue(
            ServicioReparacion.EstadoReparacion.AGENDADO));
        stats.put("serviciosEnReparacion", servicioRepository.countByEstadoAndActivoTrue(
            ServicioReparacion.EstadoReparacion.EN_REPARACION));
        stats.put("serviciosCompletados", servicioRepository.countByEstadoAndActivoTrue(
            ServicioReparacion.EstadoReparacion.COMPLETADO));
        
        // Estadísticas por tipo de dispositivo
        Map<String, Long> porTipoDispositivo = todosServicios.stream()
                .collect(Collectors.groupingBy(
                    ServicioReparacion::getTipoDispositivo,
                    Collectors.counting()
                ));
        stats.put("serviciosPorTipo", porTipoDispositivo);
        
        // Técnicos activos - CORREGIDO
        Set<String> tecnicos = todosServicios.stream()
                .map(ServicioReparacion::getTecnicoAsignado)
                .filter(tecnico -> tecnico != null && !tecnico.trim().isEmpty())
                .collect(Collectors.toSet());
        stats.put("totalTecnicos", tecnicos.size());
        
        return stats;
    }
}