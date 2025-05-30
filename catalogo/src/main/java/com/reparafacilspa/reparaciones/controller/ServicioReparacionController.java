package com.reparafacilspa.reparaciones.controller;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.service.ServicioReparacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/reparaciones")
public class ServicioReparacionController {

    @Autowired
    private ServicioReparacionService servicioService;

    // Obtener todos los servicios
    @GetMapping
    public ResponseEntity<List<ServicioReparacionResponse>> getAllServicios() {
        List<ServicioReparacionResponse> servicios = servicioService.findAll();
        if (servicios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(servicios);
    }

    // Crear nuevo servicio de reparación
    @PostMapping
    public ResponseEntity<?> createServicio(@Valid @RequestBody ServicioReparacionRequest request,
                                           BindingResult bindingResult) {
        
        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Errores de validación: " + errors
            ));
        }

        try {
            ServicioReparacionResponse nuevoServicio = servicioService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Servicio de reparación agendado exitosamente",
                "servicio", nuevoServicio
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al agendar servicio: " + e.getMessage()
            ));
        }
    }

    // Obtener servicio por ID
    @GetMapping("/{id}")
    public ResponseEntity<ServicioReparacionResponse> getServicioById(@PathVariable Long id) {
        try {
            ServicioReparacionResponse servicio = servicioService.findById(id);
            return ResponseEntity.ok(servicio);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar servicios por email del cliente
    @GetMapping("/cliente/{email}")
    public ResponseEntity<List<ServicioReparacionResponse>> getServiciosByEmail(@PathVariable String email) {
        List<ServicioReparacionResponse> servicios = servicioService.findByEmail(email);
        return ResponseEntity.ok(servicios);
    }

    // Buscar servicios por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ServicioReparacionResponse>> getServiciosByEstado(@PathVariable String estado) {
        List<ServicioReparacionResponse> servicios = servicioService.findByEstado(estado);
        return ResponseEntity.ok(servicios);
    }

    // Buscar servicios por tipo de dispositivo
    @GetMapping("/tipo/{tipoDispositivo}")
    public ResponseEntity<List<ServicioReparacionResponse>> getServiciosByTipo(@PathVariable String tipoDispositivo) {
        List<ServicioReparacionResponse> servicios = servicioService.findByTipoDispositivo(tipoDispositivo);
        return ResponseEntity.ok(servicios);
    }

    // Búsqueda general
    @GetMapping("/buscar")
    public ResponseEntity<List<ServicioReparacionResponse>> searchServicios(@RequestParam String q) {
        List<ServicioReparacionResponse> servicios = servicioService.search(q);
        return ResponseEntity.ok(servicios);
    }

    // Cambiar estado de un servicio
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String nuevoEstado = request.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El estado es requerido"
                ));
            }

            ServicioReparacionResponse servicioActualizado = servicioService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estado actualizado exitosamente",
                "servicio", servicioActualizado
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al cambiar estado: " + e.getMessage()
            ));
        }
    }

    // Obtener estadísticas del sistema
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = servicioService.getEstadisticas();
        return ResponseEntity.ok(stats);
    }

    // Endpoint de salud
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = Map.of(
            "status", "UP",
            "service", "ReparaFacilSPA - Servicios de Reparación",
            "timestamp", new java.util.Date().toString()
        );
        return ResponseEntity.ok(response);
    }

    // Obtener estados disponibles
    @GetMapping("/estados")
    public ResponseEntity<Map<String, Object>> getEstadosDisponibles() {
        return ResponseEntity.ok(Map.of(
            "estados", List.of(
                Map.of("codigo", "AGENDADO", "descripcion", "Agendado - Esperando revisión"),
                Map.of("codigo", "EN_REVISION", "descripcion", "En revisión técnica"),
                Map.of("codigo", "EN_REPARACION", "descripcion", "En proceso de reparación"),
                Map.of("codigo", "ESPERANDO_REPUESTOS", "descripcion", "Esperando repuestos"),
                Map.of("codigo", "COMPLETADO", "descripcion", "Reparación completada"),
                Map.of("codigo", "ENTREGADO", "descripcion", "Entregado al cliente"),
                Map.of("codigo", "CANCELADO", "descripcion", "Servicio cancelado"),
                Map.of("codigo", "EN_GARANTIA", "descripcion", "En servicio de garantía")
            )
        ));
    }

    // Obtener tipos de dispositivos más comunes
    @GetMapping("/tipos-dispositivos")
    public ResponseEntity<List<String>> getTiposDispositivos() {
        List<String> tipos = List.of(
            "Smartphone", "Laptop", "Tablet", "Computador", 
            "Smartwatch", "Auriculares", "Consola", "Otro"
        );
        return ResponseEntity.ok(tipos);
    }
}