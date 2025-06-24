package com.reparafacilspa.reparaciones.controller;

import com.reparafacilspa.reparaciones.assemblers.ServicioReparacionModelAssembler;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.service.ServicioReparacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v2/reparaciones")
@Tag(name = "Servicios de Reparación V2 (HATEOAS)", description = "Gestión completa de servicios de reparación con enlaces HATEOAS para navegación dinámica")
public class ServicioReparacionControllerV2 {

    @Autowired
    private ServicioReparacionService servicioService;

    @Autowired
    private ServicioReparacionModelAssembler assembler;

    @Operation(
        summary = "Obtener todos los servicios de reparación (V2 con HATEOAS)",
        description = "Retorna una lista de todos los servicios de reparación activos con enlaces de navegación"
    )
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getAllServicios() {
        List<ServicioReparacionResponse> servicios = servicioService.findAll();
        
        if (servicios.isEmpty()) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> emptyCollection = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV2.class).withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("create"));
            return ResponseEntity.ok(emptyCollection);
        }

        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV2.class).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("create"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("estadisticas").withRel("estadisticas"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("estados").withRel("estados"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tipos-dispositivos").withRel("tipos-dispositivos"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Crear nuevo servicio de reparación (V2 con HATEOAS)",
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
                    "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref(),
                    "tipos-dispositivos", linkTo(ServicioReparacionControllerV2.class).slash("tipos-dispositivos").withRel("tipos-dispositivos").getHref()
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
                    "self", linkTo(ServicioReparacionControllerV2.class).slash(nuevoServicio.getId()).withSelfRel().getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref(),
                    "cambiar-estado", linkTo(ServicioReparacionControllerV2.class).slash(nuevoServicio.getId()).slash("estado").withRel("cambiar-estado").getHref()
                )
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al agendar servicio: " + e.getMessage(),
                "_links", Map.of(
                    "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Obtener servicio por ID (V2 con HATEOAS)",
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

    @Operation(
        summary = "Buscar servicios por email del cliente (V2 con HATEOAS)",
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
                .add(linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).slash("count").withRel("total-cliente"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Buscar servicios por estado (V2 con HATEOAS)",
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
                .add(linkTo(ServicioReparacionControllerV2.class).slash("estado").slash(estado).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("estados").withRel("estados"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Buscar servicios por tipo de dispositivo (V2 con HATEOAS)",
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
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tipo").slash(tipoDispositivo).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tipos-dispositivos").withRel("tipos-dispositivos"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Cambiar estado de un servicio (V2 con HATEOAS)",
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
                        "servicio", linkTo(ServicioReparacionControllerV2.class).slash(id).withRel("servicio").getHref(),
                        "estados", linkTo(ServicioReparacionControllerV2.class).slash("estados").withRel("estados").getHref()
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
                    "self", linkTo(ServicioReparacionControllerV2.class).slash(id).slash("estado").withSelfRel().getHref(),
                    "servicio", linkTo(ServicioReparacionControllerV2.class).slash(id).withRel("servicio").getHref(),
                    "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref()
                )
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al cambiar estado: " + e.getMessage(),
                "_links", Map.of(
                    "servicio", linkTo(ServicioReparacionControllerV2.class).slash(id).withRel("servicio").getHref(),
                    "estados", linkTo(ServicioReparacionControllerV2.class).slash("estados").withRel("estados").getHref()
                )
            ));
        }
    }

    // MÉTODOS PERSONALIZADOS NUEVOS PARA HATEOAS

    @Operation(
        summary = "Obtener servicios por técnico asignado (V2 con HATEOAS)",
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
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).slash("count").withRel("total-tecnico"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Obtener servicios por fecha agendada (V2 con HATEOAS)",
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
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("fecha").slash(fecha).withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));

            return ResponseEntity.ok(serviciosModel);
        } catch (Exception e) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> errorModel = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));
            return ResponseEntity.badRequest().body(errorModel);
        }
    }

    @Operation(
        summary = "Contar servicios por cliente (V2 con HATEOAS)",
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
                "self", linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).slash("count").withSelfRel().getHref(),
                "servicios-cliente", linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).withRel("servicios-cliente").getHref(),
                "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref()
            )
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener servicios de cliente en fecha específica (V2 con HATEOAS)",
        description = "Retorna servicios de un cliente en una fecha específica"
    )
    @GetMapping("/cliente/{email}/fecha/{fecha}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByEmailAndFecha(
            @Parameter(description = "Email del cliente", required = true, example = "cliente@email.com")
            @PathVariable String email,
            @Parameter(description = "Fecha agendada (formato: yyyy-MM-dd)", required = true, example = "2024-01-20")
            @PathVariable String fecha) {
        try {
            List<ServicioReparacionResponse> servicios = servicioService.findByEmailAndFechaAgendada(email, fecha);
            
            CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
                CollectionModel.of(servicios.stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList()))
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).slash("fecha").slash(fecha).withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).withRel("servicios-cliente"))
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("fecha").slash(fecha).withRel("servicios-fecha"))
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));

            return ResponseEntity.ok(serviciosModel);
        } catch (Exception e) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> errorModel = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));
            return ResponseEntity.badRequest().body(errorModel);
        }
    }

    @Operation(
        summary = "Obtener servicios de técnico por estado (V2 con HATEOAS)",
        description = "Retorna servicios de un técnico específico filtrados por estado"
    )
    @GetMapping("/tecnico/{tecnicoAsignado}/estado/{estado}")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByTecnicoAndEstado(
            @Parameter(description = "Nombre del técnico asignado", required = true, example = "Carlos González")
            @PathVariable String tecnicoAsignado,
            @Parameter(description = "Estado del servicio", required = true, example = "EN_REPARACION")
            @PathVariable String estado) {
        List<ServicioReparacionResponse> servicios = servicioService.findByTecnicoAsignadoAndEstado(tecnicoAsignado, estado);
        
        CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
            CollectionModel.of(servicios.stream()
                .map(assembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).slash("estado").slash(estado).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).withRel("servicios-tecnico"))
                .add(linkTo(ServicioReparacionControllerV2.class).slash("estado").slash(estado).withRel("servicios-estado"))
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Obtener servicios de cliente entre fechas (V2 con HATEOAS)",
        description = "Retorna servicios de un cliente entre dos fechas específicas"
    )
    @GetMapping("/cliente/{email}/fechas")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByEmailAndFechasBetween(
            @Parameter(description = "Email del cliente", required = true, example = "cliente@email.com")
            @PathVariable String email,
            @Parameter(description = "Fecha inicio (formato: yyyy-MM-dd)", required = true, example = "2024-01-01")
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha fin (formato: yyyy-MM-dd)", required = true, example = "2024-01-31")
            @RequestParam String fechaFin) {
        try {
            List<ServicioReparacionResponse> servicios = servicioService.findByEmailAndFechaAgendadaBetween(email, fechaInicio, fechaFin);
            
            CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
                CollectionModel.of(servicios.stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList()))
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).slash("fechas").withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("cliente").slash(email).withRel("servicios-cliente"))
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));

            return ResponseEntity.ok(serviciosModel);
        } catch (Exception e) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> errorModel = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));
            return ResponseEntity.badRequest().body(errorModel);
        }
    }

    @Operation(
        summary = "Obtener servicios de técnico entre fechas (V2 con HATEOAS)",
        description = "Retorna servicios de un técnico entre dos fechas específicas"
    )
    @GetMapping("/tecnico/{tecnicoAsignado}/fechas")
    public ResponseEntity<CollectionModel<EntityModel<ServicioReparacionResponse>>> getServiciosByTecnicoAndFechasBetween(
            @Parameter(description = "Nombre del técnico asignado", required = true, example = "Carlos González")
            @PathVariable String tecnicoAsignado,
            @Parameter(description = "Fecha inicio (formato: yyyy-MM-dd)", required = true, example = "2024-01-01")
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha fin (formato: yyyy-MM-dd)", required = true, example = "2024-01-31")
            @RequestParam String fechaFin) {
        try {
            List<ServicioReparacionResponse> servicios = servicioService.findByTecnicoAsignadoAndFechaAgendadaBetween(tecnicoAsignado, fechaInicio, fechaFin);
            
            CollectionModel<EntityModel<ServicioReparacionResponse>> serviciosModel = 
                CollectionModel.of(servicios.stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList()))
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).slash("fechas").withSelfRel())
                    .add(linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).withRel("servicios-tecnico"))
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));

            return ResponseEntity.ok(serviciosModel);
        } catch (Exception e) {
            CollectionModel<EntityModel<ServicioReparacionResponse>> errorModel = 
                CollectionModel.<EntityModel<ServicioReparacionResponse>>empty()
                    .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));
            return ResponseEntity.badRequest().body(errorModel);
        }
    }

    @Operation(
        summary = "Contar servicios por técnico (V2 con HATEOAS)",
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
                "self", linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).slash("count").withSelfRel().getHref(),
                "servicios-tecnico", linkTo(ServicioReparacionControllerV2.class).slash("tecnico").slash(tecnicoAsignado).withRel("servicios-tecnico").getHref(),
                "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref()
            )
        );
        
        return ResponseEntity.ok(response);
    }

    // MÉTODOS EXISTENTES ADAPTADOS CON HATEOAS

    @Operation(
        summary = "Búsqueda general de servicios (V2 con HATEOAS)",
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
                .add(linkTo(ServicioReparacionControllerV2.class).slash("buscar").withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"));

        return ResponseEntity.ok(serviciosModel);
    }

    @Operation(
        summary = "Obtener estadísticas del sistema (V2 con HATEOAS)",
        description = "Retorna métricas y estadísticas generales del sistema de reparaciones con enlaces relacionados"
    )
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = servicioService.getEstadisticas();
        
        // Agregar enlaces HATEOAS a las estadísticas
        Map<String, Object> linksMap = Map.of(
            "self", linkTo(ServicioReparacionControllerV2.class).slash("estadisticas").withSelfRel().getHref(),
            "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref(),
            "servicios-agendados", linkTo(ServicioReparacionControllerV2.class).slash("estado").slash("AGENDADO").withRel("servicios-agendados").getHref(),
            "servicios-en-reparacion", linkTo(ServicioReparacionControllerV2.class).slash("estado").slash("EN_REPARACION").withRel("servicios-en-reparacion").getHref(),
            "servicios-completados", linkTo(ServicioReparacionControllerV2.class).slash("estado").slash("COMPLETADO").withRel("servicios-completados").getHref()
        );
        
        stats.put("_links", linksMap);
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Obtener estados disponibles (V2 con HATEOAS)",
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
                "self", linkTo(ServicioReparacionControllerV2.class).slash("estados").withSelfRel().getHref(),
                "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener tipos de dispositivos soportados (V2 con HATEOAS)",
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
                "self", linkTo(ServicioReparacionControllerV2.class).slash("tipos-dispositivos").withSelfRel().getHref(),
                "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Estado de salud del servicio (V2 con HATEOAS)",
        description = "Endpoint para verificar que el servicio de reparaciones está funcionando correctamente"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "ReparaFacilSPA - Servicios de Reparación V2 (HATEOAS)",
            "timestamp", new java.util.Date().toString(),
            "_links", Map.of(
                "self", linkTo(ServicioReparacionControllerV2.class).slash("health").withSelfRel().getHref(),
                "servicios", linkTo(ServicioReparacionControllerV2.class).withRel("servicios").getHref(),
                "estadisticas", linkTo(ServicioReparacionControllerV2.class).slash("estadisticas").withRel("estadisticas").getHref(),
                "estados", linkTo(ServicioReparacionControllerV2.class).slash("estados").withRel("estados").getHref(),
                "tipos-dispositivos", linkTo(ServicioReparacionControllerV2.class).slash("tipos-dispositivos").withRel("tipos-dispositivos").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }
}