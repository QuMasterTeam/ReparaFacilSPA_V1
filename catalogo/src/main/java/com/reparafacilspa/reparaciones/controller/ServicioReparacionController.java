package com.reparafacilspa.reparaciones.controller;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.service.ServicioReparacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/reparaciones")
@Tag(name = "Servicios de Reparación", description = "Gestión completa de servicios de reparación de dispositivos")
public class ServicioReparacionController {

    @Autowired
    private ServicioReparacionService servicioService;

    @Operation(
        summary = "Obtener todos los servicios de reparación",
        description = "Retorna una lista de todos los servicios de reparación activos en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de servicios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ServicioReparacionResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "204", 
            description = "No hay servicios registrados"
        )
    })
    @GetMapping
    public ResponseEntity<List<ServicioReparacionResponse>> getAllServicios() {
        List<ServicioReparacionResponse> servicios = servicioService.findAll();
        if (servicios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(servicios);
    }

    @Operation(
        summary = "Crear nuevo servicio de reparación",
        description = "Permite agendar un nuevo servicio de reparación para un dispositivo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Servicio creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "ServicioCreado",
                    value = """
                    {
                        "success": true,
                        "message": "Servicio de reparación agendado exitosamente",
                        "servicio": {
                            "id": 1,
                            "nombreCliente": "Juan Pérez",
                            "email": "juan@email.com",
                            "tipoDispositivo": "Smartphone",
                            "marca": "Samsung",
                            "modelo": "Galaxy S21",
                            "estado": "AGENDADO"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Errores de validación en los datos"
        )
    })
    @PostMapping
    public ResponseEntity<?> createServicio(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del servicio de reparación a crear",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ServicioReparacionRequest.class),
                    examples = @ExampleObject(
                        name = "NuevoServicio",
                        value = """
                        {
                            "nombreCliente": "Juan Pérez",
                            "telefono": "+56912345678",
                            "email": "juan@email.com",
                            "tipoDispositivo": "Smartphone",
                            "marca": "Samsung",
                            "modelo": "Galaxy S21",
                            "descripcionProblema": "La pantalla no enciende después de una caída",
                            "fechaAgendada": "2024-01-20T10:00:00"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ServicioReparacionRequest request,
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

    @Operation(
        summary = "Obtener servicio por ID",
        description = "Retorna los detalles de un servicio de reparación específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Servicio encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ServicioReparacionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Servicio no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServicioReparacionResponse> getServicioById(
            @Parameter(description = "ID del servicio de reparación", required = true, example = "1")
            @PathVariable Long id) {
        try {
            ServicioReparacionResponse servicio = servicioService.findById(id);
            return ResponseEntity.ok(servicio);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Buscar servicios por email del cliente",
        description = "Retorna todos los servicios asociados a un email específico"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Servicios encontrados",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ServicioReparacionResponse.class))
        )
    )
    @GetMapping("/cliente/{email}")
    public ResponseEntity<List<ServicioReparacionResponse>> getServiciosByEmail(
            @Parameter(description = "Email del cliente", required = true, example = "cliente@email.com")
            @PathVariable String email) {
        List<ServicioReparacionResponse> servicios = servicioService.findByEmail(email);
        return ResponseEntity.ok(servicios);
    }

    @Operation(
        summary = "Buscar servicios por estado",
        description = "Retorna servicios filtrados por su estado actual"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Servicios encontrados",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ServicioReparacionResponse.class))
        )
    )
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ServicioReparacionResponse>> getServiciosByEstado(
            @Parameter(
                description = "Estado del servicio", 
                required = true, 
                example = "AGENDADO",
                schema = @Schema(allowableValues = {
                    "AGENDADO", "EN_REVISION", "EN_REPARACION", 
                    "ESPERANDO_REPUESTOS", "COMPLETADO", "ENTREGADO", 
                    "CANCELADO", "EN_GARANTIA"
                })
            )
            @PathVariable String estado) {
        List<ServicioReparacionResponse> servicios = servicioService.findByEstado(estado);
        return ResponseEntity.ok(servicios);
    }

    @Operation(
        summary = "Buscar servicios por tipo de dispositivo",
        description = "Retorna servicios filtrados por tipo de dispositivo"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Servicios encontrados",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ServicioReparacionResponse.class))
        )
    )
    @GetMapping("/tipo/{tipoDispositivo}")
    public ResponseEntity<List<ServicioReparacionResponse>> getServiciosByTipo(
            @Parameter(
                description = "Tipo de dispositivo", 
                required = true, 
                example = "Smartphone",
                schema = @Schema(allowableValues = {
                    "Smartphone", "Laptop", "Tablet", "Computador", 
                    "Smartwatch", "Auriculares", "Consola", "Otro"
                })
            )
            @PathVariable String tipoDispositivo) {
        List<ServicioReparacionResponse> servicios = servicioService.findByTipoDispositivo(tipoDispositivo);
        return ResponseEntity.ok(servicios);
    }

    @Operation(
        summary = "Búsqueda general de servicios",
        description = "Permite buscar servicios por nombre de cliente, descripción del problema, marca o modelo"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Resultados de búsqueda",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ServicioReparacionResponse.class))
        )
    )
    @GetMapping("/buscar")
    public ResponseEntity<List<ServicioReparacionResponse>> searchServicios(
            @Parameter(description = "Término de búsqueda", required = true, example = "Samsung")
            @RequestParam String q) {
        List<ServicioReparacionResponse> servicios = servicioService.search(q);
        return ResponseEntity.ok(servicios);
    }

    @Operation(
        summary = "Cambiar estado de un servicio",
        description = "Permite actualizar el estado de un servicio de reparación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estado actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "EstadoActualizado",
                    value = """
                    {
                        "success": true,
                        "message": "Estado actualizado exitosamente",
                        "servicio": {
                            "id": 1,
                            "estado": "EN_REPARACION",
                            "estadoDescripcion": "En proceso de reparación"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Estado inválido o servicio no encontrado"
        )
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "ID del servicio", required = true, example = "1")
            @PathVariable Long id, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Nuevo estado del servicio",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "CambioEstado",
                        value = """
                        {
                            "estado": "EN_REPARACION"
                        }
                        """
                    )
                )
            )
            @RequestBody Map<String, String> request) {
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

    @Operation(
        summary = "Obtener estadísticas del sistema",
        description = "Retorna métricas y estadísticas generales del sistema de reparaciones"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Estadísticas obtenidas exitosamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "EstadisticasEjemplo",
                value = """
                {
                    "totalServicios": 150,
                    "serviciosAgendados": 25,
                    "serviciosEnReparacion": 45,
                    "serviciosCompletados": 80,
                    "serviciosPorTipo": {
                        "Smartphone": 65,
                        "Laptop": 35,
                        "Tablet": 25,
                        "Computador": 15,
                        "Otro": 10
                    },
                    "totalTecnicos": 6
                }
                """
            )
        )
    )
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = servicioService.getEstadisticas();
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Estado de salud del servicio",
        description = "Endpoint para verificar que el servicio de reparaciones está funcionando correctamente"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Servicio funcionando correctamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "ServicioActivo",
                value = """
                {
                    "status": "UP",
                    "service": "ReparaFacilSPA - Servicios de Reparación",
                    "timestamp": "2024-01-15T10:30:00"
                }
                """
            )
        )
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = Map.of(
            "status", "UP",
            "service", "ReparaFacilSPA - Servicios de Reparación",
            "timestamp", new java.util.Date().toString()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener estados disponibles",
        description = "Retorna la lista de todos los estados posibles para un servicio de reparación"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Lista de estados obtenida exitosamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "EstadosDisponibles",
                value = """
                {
                    "estados": [
                        {
                            "codigo": "AGENDADO",
                            "descripcion": "Agendado - Esperando revisión"
                        },
                        {
                            "codigo": "EN_REVISION",
                            "descripcion": "En revisión técnica"
                        },
                        {
                            "codigo": "EN_REPARACION",
                            "descripcion": "En proceso de reparación"
                        }
                    ]
                }
                """
            )
        )
    )
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

    @Operation(
        summary = "Obtener tipos de dispositivos soportados",
        description = "Retorna la lista de tipos de dispositivos que el sistema puede reparar"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Lista de tipos obtenida exitosamente",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(type = "string")),
            examples = @ExampleObject(
                name = "TiposDispositivos",
                value = """
                [
                    "Smartphone",
                    "Laptop", 
                    "Tablet",
                    "Computador",
                    "Smartwatch",
                    "Auriculares",
                    "Consola",
                    "Otro"
                ]
                """
            )
        )
    )
    @GetMapping("/tipos-dispositivos")
    public ResponseEntity<List<String>> getTiposDispositivos() {
        List<String> tipos = List.of(
            "Smartphone", "Laptop", "Tablet", "Computador", 
            "Smartwatch", "Auriculares", "Consola", "Otro"
        );
        return ResponseEntity.ok(tipos);
    }
}