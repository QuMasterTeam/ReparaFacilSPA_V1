package com.reparafacilspa.reparaciones.assemblers;

import com.reparafacilspa.reparaciones.controller.ServicioReparacionControllerV2;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ServicioReparacionModelAssembler implements RepresentationModelAssembler<ServicioReparacionResponse, EntityModel<ServicioReparacionResponse>> {

    @Override
    public EntityModel<ServicioReparacionResponse> toModel(ServicioReparacionResponse servicio) {
        return EntityModel.of(servicio)
                .add(linkTo(methodOn(ServicioReparacionControllerV2.class).getServicioById(servicio.getId())).withSelfRel())
                .add(linkTo(ServicioReparacionControllerV2.class).withRel("servicios"))
                .add(linkTo(methodOn(ServicioReparacionControllerV2.class).getServiciosByEmail(servicio.getEmail())).withRel("servicios-cliente"))
                .add(linkTo(methodOn(ServicioReparacionControllerV2.class).getServiciosByEstado(servicio.getEstado())).withRel("servicios-estado"))
                .add(linkTo(methodOn(ServicioReparacionControllerV2.class).getServiciosByTipo(servicio.getTipoDispositivo())).withRel("servicios-tipo"));
    }
}