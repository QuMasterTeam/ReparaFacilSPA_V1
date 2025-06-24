package com.reparafacilspa.reparaciones.assemblers;

import com.reparafacilspa.reparaciones.controller.AuthControllerV2;
import com.reparafacilspa.reparaciones.dto.AuthResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<AuthResponse.UserInfo, EntityModel<AuthResponse.UserInfo>> {

    @Override
    public EntityModel<AuthResponse.UserInfo> toModel(AuthResponse.UserInfo user) {
        return EntityModel.of(user)
                .add(linkTo(methodOn(AuthControllerV2.class).checkUsername(user.getUsername())).withRel("check-username"))
                .add(linkTo(methodOn(AuthControllerV2.class).checkEmail(user.getEmail())).withRel("check-email"))
                .add(linkTo(AuthControllerV2.class).withRel("auth"));
    }
}