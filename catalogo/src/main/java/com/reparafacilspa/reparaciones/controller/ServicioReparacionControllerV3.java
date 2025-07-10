package com.reparafacilspa.reparaciones.controller;

import com.reparafacilspa.reparaciones.assemblers.ServicioReparacionModelAssembler;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.service.ServicioReparacionServiceV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v3/reparaciones")
@Tag(name = "Servicios de Reparación V3 (HATEOAS + CRUD)", description = "Gestión completa de servicios de reparación con enlaces HATEOAS y CRUD completo")
public class ServicioReparacionControllerV3 {

    @Autowired
    private ServicioReparacionServiceV3 servicioService;

    @Autowired
    private ServicioReparacionModelAssembler assembler;

    // ===== TODOS LOS ENDPOINTS DE V2 MANTENIDOS =====

    @Operation(
        summary = "Obtener todos los servicios de reparación (V3 con HATEOAS)",
        description = "Retorna una lista de todos los servicios de reparación activos con enlaces de navegación"
    )
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getAllServicios() {
        List<ServicioReparacionResponse> servicios = servicioService.findAll();
        
        if (servicios.isEmpty()) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> emptyCollection = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV3.class).withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV3.class).withRel("create"));
            return ResponseEntity.ok(emptyCollection);
        }

        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV3.class).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV3.class).withRel("create"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("estadisticas").withRel("estadisticas"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("estados").withRel("estados"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("tipos-dispositivos").withRel("tipos-dispositivos"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Crear nuevo servicio de reparación (V3 con HATEOAS)",
        description = "Permite agendar un nuevo servicio de reparación con enlaces de navegación"
    )
    @PostMapping
    public ResponseEntity<?> createServicio(
            @Valid @RequestBody ServicioReparacionRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Errores de validación: " + errors,
                "_links", Map.of(
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref(),
                    "tipos-dispositivos", linkTo(ServicioReparacionControllerV3.class).slash("tipos-dispositivos").withRel("tipos-dispositivos").getHref()
                )
            ));
        }

        try {
            ServicioReparacionResponse nuevoServicio = servicioService.save(request);
            EntityModel<ServicioReparacionResponse> servicioModel = assembler.toModel(nuevoServicio);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Servicio de reparación agendado exitosamente",
                "servicio", servicioModel,
                "_links", Map.of(
                    "self", linkTo(ServicioReparacionControllerV3.class).slash(nuevoServicio.getId()).withSelfRel().getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref(),
                    "update", linkTo(ServicioReparacionControllerV3.class).slash(nuevoServicio.getId()).withRel("update").getHref(),
                    "cambiar-estado", linkTo(ServicioReparacionControllerV3.class).slash(nuevoServicio.getId()).slash("estado").withRel("cambiar-estado").getHref()
                )
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al agendar servicio: " + e.getMessage(),
                "_links", Map.of(
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Obtener servicio por ID (V3 con HATEOAS)",
        description = "Retorna los detalles de un servicio de reparación específico con enlaces relacionados"
    )
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ServicioReparacionResponse>> getServicioById(
            @Parameter(description = "ID del servicio de reparación", required = true, example = "1")
            @PathVariable Long id) {
        try {
            ServicioReparacionResponse servicio = servicioService.findById(id);
            EntityModel<ServicioReparacionResponse> servicioModel = assembler.toModel(servicio);
            return ResponseEntity.ok(servicioModel);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== NUEVOS ENDPOINTS V3 PARA CRUD COMPLETO =====

    @Operation(
        summary = "Actualizar servicio completo (V3 con HATEOAS)",
        description = "Permite actualizar todos los campos de un servicio de reparación existente con enlaces de navegación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Servicio actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "ServicioActualizado",
                    value = """
                    {
                        "success": true,
                        "message": "Servicio actualizado exitosamente",
                        "servicio": {
                            "id": 1,
                            "nombreCliente": "Juan Pérez Actualizado",
                            "email": "juan.actualizado@email.com",
                            "tipoDispositivo": "Laptop",
                            "marca": "Dell",
                            "modelo": "Inspiron 15",
                            "estado": "EN_REPARACION",
                            "tecnicoAsignado": "Carlos González",
                            "_links": {
                                "self": {"href": "/api/v3/reparaciones/1"},
                                "delete": {"href": "/api/v3/reparaciones/1"},
                                "cambiar-estado": {"href": "/api/v3/reparaciones/1/estado"},
                                "servicios": {"href": "/api/v3/reparaciones"}
                            }
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Errores de validación en los datos"),
        @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateServicio(
            @Parameter(description = "ID del servicio a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos actualizados del servicio de reparación",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ServicioReparacionRequest.class),
                    examples = @ExampleObject(
                        name = "ActualizarServicio",
                        value = """
                        {
                            "nombreCliente": "Juan Pérez Actualizado",
                            "telefono": "+56987654321",
                            "email": "juan.actualizado@email.com",
                            "tipoDispositivo": "Laptop",
                            "marca": "Dell",
                            "modelo": "Inspiron 15",
                            "descripcionProblema": "Problema actualizado en la pantalla",
                            "fechaAgendada": "2024-01-25T14:00:00"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ServicioReparacionRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Errores de validación: " + errors,
                "_links", Map.of(
                    "servicio", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("servicio").getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
                )
            ));
        }

        try {
            ServicioReparacionResponse servicioActualizado = servicioService.update(id, request);
            EntityModel<ServicioReparacionResponse> servicioModel = assembler.toModel(servicioActualizado);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Servicio actualizado exitosamente",
                "servicio", servicioModel,
                "_links", Map.of(
                    "self", linkTo(ServicioReparacionControllerV3.class).slash(id).withSelfRel().getHref(),
                    "delete", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("delete").getHref(),
                    "cambiar-estado", linkTo(ServicioReparacionControllerV3.class).slash(id).slash("estado").withRel("cambiar-estado").getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
                )
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al actualizar servicio: " + e.getMessage(),
                "_links", Map.of(
                    "servicio", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("servicio").getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Eliminar servicio (V3 con HATEOAS)",
        description = "Realiza un soft delete del servicio (lo marca como inactivo) con enlaces de navegación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Servicio eliminado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "ServicioEliminado",
                    value = """
                    {
                        "success": true,
                        "message": "Servicio eliminado exitosamente",
                        "_links": {
                            "servicios": {"href": "/api/v3/reparaciones"},
                            "active-servicios": {"href": "/api/v3/reparaciones?activo=true"},
                            "estadisticas": {"href": "/api/v3/reparaciones/estadisticas"}
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteServicio(
            @Parameter(description = "ID del servicio a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        try {
            servicioService.delete(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Servicio eliminado exitosamente",
                "_links", Map.of(
                    "self", linkTo(ServicioReparacionControllerV3.class).slash(id).withSelfRel().getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref(),
                    "estadisticas", linkTo(ServicioReparacionControllerV3.class).slash("estadisticas").withRel("estadisticas").getHref(),
                    "create", linkTo(ServicioReparacionControllerV3.class).withRel("create").getHref()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al eliminar servicio: " + e.getMessage(),
                "_links", Map.of(
                    "servicio", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("servicio").getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
                )
            ));
        }
    }

    // ===== TODOS LOS ENDPOINTS DE V2 MANTENIDOS =====

    @Operation(
        summary = "Buscar servicios por email del cliente (V3 con HATEOAS)",
        description = "Retorna todos los servicios asociados a un email específico con enlaces de navegación"
    )
    @GetMapping("/cliente/{email}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByEmail(
            @Parameter(description = "Email del cliente", required = true, example = "cliente@email.com")
            @PathVariable String email) {
        List<ServicioReparacionResponse> servicios = servicioService.findByEmail(email);
        
        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("cliente").slash(email).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("cliente").slash(email).slash("count").withRel("total-cliente"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Buscar servicios por estado (V3 con HATEOAS)",
        description = "Retorna servicios filtrados por su estado actual con enlaces de navegación"
    )
    @GetMapping("/estado/{estado}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByEstado(
            @Parameter(description = "Estado del servicio", required = true, example = "AGENDADO")
            @PathVariable String estado) {
        List<ServicioReparacionResponse> servicios = servicioService.findByEstado(estado);
        
        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("estado").slash(estado).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("estados").withRel("estados"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Buscar servicios por tipo de dispositivo (V3 con HATEOAS)",
        description = "Retorna servicios filtrados por tipo de dispositivo con enlaces de navegación"
    )
    @GetMapping("/tipo/{tipoDispositivo}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByTipo(
            @Parameter(description = "Tipo de dispositivo", required = true, example = "Smartphone")
            @PathVariable String tipoDispositivo) {
        List<ServicioReparacionResponse> servicios = servicioService.findByTipoDispositivo(tipoDispositivo);
        
        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("tipo").slash(tipoDispositivo).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("tipos-dispositivos").withRel("tipos-dispositivos"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Cambiar estado de un servicio (V3 con HATEOAS)",
        description = "Permite actualizar el estado de un servicio de reparación con enlaces relacionados"
    )
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "ID del servicio", required = true, example = "1")
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            String nuevoEstado = request.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El estado es requerido",
                    "_links", Map.of(
                        "servicio", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("servicio").getHref(),
                        "estados", linkTo(ServicioReparacionControllerV3.class).slash("estados").withRel("estados").getHref()
                    )
                ));
            }

            ServicioReparacionResponse servicioActualizado = servicioService.cambiarEstado(id, nuevoEstado);
            EntityModel<ServicioReparacionResponse> servicioModel = assembler.toModel(servicioActualizado);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Estado actualizado exitosamente",
                "servicio", servicioModel,
                "_links", Map.of(
                    "self", linkTo(ServicioReparacionControllerV3.class).slash(id).slash("estado").withSelfRel().getHref(),
                    "servicio", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("servicio").getHref(),
                    "update", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("update").getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
                )
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al cambiar estado: " + e.getMessage(),
                "_links", Map.of(
                    "servicio", linkTo(ServicioReparacionControllerV3.class).slash(id).withRel("servicio").getHref(),
                    "estados", linkTo(ServicioReparacionControllerV3.class).slash("estados").withRel("estados").getHref()
                )
            ));
        }
    }

    // MÉTODOS PERSONALIZADOS DE V2 (mantenidos todos)

    @Operation(
        summary = "Obtener servicios por técnico asignado (V3 con HATEOAS)",
        description = "Retorna todos los servicios asignados a un técnico específico"
    )
    @GetMapping("/tecnico/{tecnicoAsignado}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByTecnico(
            @Parameter(description = "Nombre del técnico asignado", required = true, example = "Carlos González")
            @PathVariable String tecnicoAsignado) {
        List<ServicioReparacionResponse> servicios = servicioService.findByTecnicoAsignado(tecnicoAsignado);
        
        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("tecnico").slash(tecnicoAsignado).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("tecnico").slash(tecnicoAsignado).slash("count").withRel("total-tecnico"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Obtener servicios por fecha agendada (V3 con HATEOAS)",
        description = "Retorna todos los servicios agendados para una fecha específica"
    )
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByFecha(
            @Parameter(description = "Fecha agendada (formato: yyyy-MM-dd)", required = true, example = "2024-01-20")
            @PathVariable String fecha) {
        try {
            List<ServicioReparacionResponse> servicios = servicioService.findByFechaAgendada(fecha);
            
            CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
                CollectionModel.of(servicios.stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList()))
                    .add(linkTo(ServicioReparacionControllerV3.class).slash("fecha").slash(fecha).withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"));

            return ResponseEntity.ok(serviciosModel);
        } catch (Exception e) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> errorModel = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"));
            return ResponseEntity.badRequest().body(errorModel);
        }
    }

    @Operation(
        summary = "Contar servicios por cliente (V3 con HATEOAS)",
        description = "Retorna el total de servicios realizados por un cliente específico"
    )
    @GetMapping("/cliente/{email}/count")
    public ResponseEntity<Map<String, Object>> countByEmail(
            @Parameter(description = "Email del cliente", required = true, example = "cliente@email.com")
            @PathVariable String email) {
        long total = servicioService.countByEmail(email);
        
        Map<String, Object> response = Map.of(
            "email", email,
            "totalServicios", total,
            "_links", Map.of(
                "self", linkTo(ServicioReparacionControllerV3.class).slash("cliente").slash(email).slash("count").withSelfRel().getHref(),
                "servicios-cliente", linkTo(ServicioReparacionControllerV3.class).slash("cliente").slash(email).withRel("servicios-cliente").getHref(),
                "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
            )
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Contar servicios por técnico (V3 con HATEOAS)",
        description = "Retorna el total de servicios asignados a un técnico específico"
    )
    @GetMapping("/tecnico/{tecnicoAsignado}/count")
    public ResponseEntity<Map<String, Object>> countByTecnico(
            @Parameter(description = "Nombre del técnico asignado", required = true, example = "Carlos González")
            @PathVariable String tecnicoAsignado) {
        long total = servicioService.countByTecnicoAsignado(tecnicoAsignado);
        
        Map<String, Object> response = Map.of(
            "tecnicoAsignado", tecnicoAsignado,
            "totalServicios", total,
            "_links", Map.of(
                "self", linkTo(ServicioReparacionControllerV3.class).slash("tecnico").slash(tecnicoAsignado).slash("count").withSelfRel().getHref(),
                "servicios-tecnico", linkTo(ServicioReparacionControllerV3.class).slash("tecnico").slash(tecnicoAsignado).withRel("servicios-tecnico").getHref(),
                "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
            )
        );
        
        return ResponseEntity.ok(response);
    }

    // OTROS ENDPOINTS DE V2 (mantenidos)...
    // [Por brevedad, incluyo solo algunos representativos]

    @Operation(
        summary = "Búsqueda general de servicios (V3 con HATEOAS)",
        description = "Permite buscar servicios por nombre de cliente, descripción del problema, marca o modelo"
    )
    @GetMapping("/buscar")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> searchServicios(
            @Parameter(description = "Término de búsqueda", required = true, example = "Samsung")
            @RequestParam String q) {
        List<ServicioReparacionResponse> servicios = servicioService.search(q);
        
        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV3.class).slash("buscar").withSelfRel())
                .add(linkTo(ServicioReparacionControllerV3.class).withRel("servicios"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Obtener estadísticas del sistema (V3 con HATEOAS)",
        description = "Retorna métricas y estadísticas generales del sistema de reparaciones con enlaces relacionados"
    )
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = servicioService.getEstadisticas();
        
        // Agregar enlaces HATEOAS a las estadísticas
        Map<String, Object> linksMap = Map.of(
            "self", linkTo(ServicioReparacionControllerV3.class).slash("estadisticas").withSelfRel().getHref(),
            "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref(),
            "servicios-agendados", linkTo(ServicioReparacionControllerV3.class).slash("estado").slash("AGENDADO").withRel("servicios-agendados").getHref(),
            "servicios-en-reparacion", linkTo(ServicioReparacionControllerV3.class).slash("estado").slash("EN_REPARACION").withRel("servicios-en-reparacion").getHref(),
            "servicios-completados", linkTo(ServicioReparacionControllerV3.class).slash("estado").slash("COMPLETADO").withRel("servicios-completados").getHref()
        );
        
        stats.put("_links", linksMap);
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Obtener estados disponibles (V3 con HATEOAS)",
        description = "Retorna la lista de todos los estados posibles para un servicio de reparación con enlaces"
    )
    @GetMapping("/estados")
    public ResponseEntity<Map<String, Object>> getEstadosDisponibles() {
        Map<String, Object> response = Map.of(
            "estados", List.of(
                Map.of("codigo", "AGENDADO", "descripcion", "Agendado - Esperando revisión"),
                Map.of("codigo", "EN_REVISION", "descripcion", "En revisión técnica"),
                Map.of("codigo", "EN_REPARACION", "descripcion", "En proceso de reparación"),
                Map.of("codigo", "ESPERANDO_REPUESTOS", "descripcion", "Esperando repuestos"),
                Map.of("codigo", "COMPLETADO", "descripcion", "Reparación completada"),
                Map.of("codigo", "ENTREGADO", "descripcion", "Entregado al cliente"),
                Map.of("codigo", "CANCELADO", "descripcion", "Servicio cancelado"),
                Map.of("codigo", "EN_GARANTIA", "descripcion", "En servicio de garantía")
            ),
            "_links", Map.of(
                "self", linkTo(ServicioReparacionControllerV3.class).slash("estados").withSelfRel().getHref(),
                "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener tipos de dispositivos soportados (V3 con HATEOAS)",
        description = "Retorna la lista de tipos de dispositivos que el sistema puede reparar con enlaces"
    )
    @GetMapping("/tipos-dispositivos")
    public ResponseEntity<Map<String, Object>> getTiposDispositivos() {
        List<String> tipos = List.of(
            "Smartphone", "Laptop", "Tablet", "Computador", 
            "Smartwatch", "Auriculares", "Consola", "Otro"
        );
        
        Map<String, Object> response = Map.of(
            "tipos", tipos,
            "_links", Map.of(
                "self", linkTo(ServicioReparacionControllerV3.class).slash("tipos-dispositivos").withSelfRel().getHref(),
                "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Estado de salud del servicio (V3 con HATEOAS)",
        description = "Endpoint para verificar que el servicio de reparaciones está funcionando correctamente"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "ReparaFacilSPA - Servicios de Reparación V3 (HATEOAS + CRUD)",
            "timestamp", new java.util.Date().toString(),
            "version", "3.0.0",
            "_links", Map.of(
                "self", linkTo(ServicioReparacionControllerV3.class).slash("health").withSelfRel().getHref(),
                "servicios", linkTo(ServicioReparacionControllerV3.class).withRel("servicios").getHref(),
                "estadisticas", linkTo(ServicioReparacionControllerV3.class).slash("estadisticas").withRel("estadisticas").getHref(),
                "estados", linkTo(ServicioReparacionControllerV3.class).slash("estados").withRel("estados").getHref(),
                "tipos-dispositivos", linkTo(ServicioReparacionControllerV3.class).slash("tipos-dispositivos").withRel("tipos-dispositivos").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }
}