package com.reparafacilspa.reparaciones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.reparafacilspa.reparaciones.assemblers.UserModelAssembler;
import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v2/auth")
@Tag(name = "Autenticación V2 (HATEOAS)", description = "Operaciones de autenticación con enlaces HATEOAS para navegación dinámica")
public class AuthControllerV2 {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserModelAssembler userAssembler;

    @Operation(
        summary = "Iniciar sesión de usuario (V2 con HATEOAS)",
        description = "Permite a un usuario autenticarse en el sistema con username y contraseña. " +
                     "Retorna información del usuario con enlaces HATEOAS para navegación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login exitoso con enlaces HATEOAS",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "LoginExitosoHATEOAS",
                    value = """
                    {
                        "success": true,
                        "message": "Login exitoso",
                        "user": {
                            "id": 1,
                            "username": "admin",
                            "email": "admin@reparafacil.com",
                            "nombre": "Administrador",
                            "apellido": "Sistema",
                            "nombreCompleto": "Administrador Sistema",
                            "telefono": "+56912345678",
                            "rol": "ADMIN",
                            "activo": true,
                            "_links": {
                                "check-username": {
                                    "href": "http://localhost:8081/reparafacil-api/api/v2/auth/check-username/admin"
                                },
                                "check-email": {
                                    "href": "http://localhost:8081/reparafacil-api/api/v2/auth/check-email/admin@reparafacil.com"
                                },
                                "auth": {
                                    "href": "http://localhost:8081/reparafacil-api/api/v2/auth"
                                }
                            }
                        },
                        "sessionToken": "abc123-456789"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciales inválidas"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest, 
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Errores de validación: " + errors,
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref(),
                    "register", linkTo(methodOn(AuthControllerV2.class).register(null, null)).withRel("register").getHref()
                )
            ));
        }

        AuthResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            // Convertir UserInfo a EntityModel con HATEOAS
            EntityModel<AuthResponse.UserInfo> userModel = userAssembler.toModel(response.getUser());
            
            Map<String, Object> hateoasResponse = Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "user", userModel,
                "sessionToken", response.getSessionToken(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV2.class).login(loginRequest, bindingResult)).withSelfRel().getHref(),
                    "change-password", linkTo(methodOn(AuthControllerV2.class).changePassword(null)).withRel("change-password").getHref(),
                    "logout", linkTo(AuthControllerV2.class).slash("logout").withRel("logout").getHref()
                )
            );
            
            return ResponseEntity.ok(hateoasResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref(),
                    "register", linkTo(methodOn(AuthControllerV2.class).register(null, null)).withRel("register").getHref(),
                    "unlock", linkTo(methodOn(AuthControllerV2.class).unlockAccount(loginRequest.getUsername())).withRel("unlock").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Registrar nuevo usuario (V2 con HATEOAS)",
        description = "Permite registrar un nuevo usuario en el sistema con enlaces de navegación."
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Errores de validación: " + errors,
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref(),
                    "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref()
                )
            ));
        }

        AuthResponse response = authService.register(registerRequest);
        
        if (response.isSuccess()) {
            EntityModel<AuthResponse.UserInfo> userModel = userAssembler.toModel(response.getUser());
            
            Map<String, Object> hateoasResponse = Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "user", userModel,
                "sessionToken", response.getSessionToken(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV2.class).register(registerRequest, bindingResult)).withSelfRel().getHref(),
                    "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref(),
                    "profile", linkTo(AuthControllerV2.class).slash("profile").withRel("profile").getHref()
                )
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(hateoasResponse);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref(),
                    "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref(),
                    "check-username", linkTo(methodOn(AuthControllerV2.class).checkUsername(registerRequest.getUsername())).withRel("check-username").getHref(),
                    "check-email", linkTo(methodOn(AuthControllerV2.class).checkEmail(registerRequest.getEmail())).withRel("check-email").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Verificar disponibilidad de username (V2 con HATEOAS)",
        description = "Verifica si un username está disponible para registro con enlaces relacionados"
    )
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsername(
            @Parameter(description = "Username a verificar", required = true, example = "nuevousuario")
            @PathVariable String username) {
        boolean exists = authService.userExists(username);
        Map<String, Object> response = Map.of(
            "username", username,
            "available", !exists,
            "message", exists ? "Username no disponible" : "Username disponible",
            "_links", Map.of(
                "self", linkTo(methodOn(AuthControllerV2.class).checkUsername(username)).withSelfRel().getHref(),
                "register", linkTo(methodOn(AuthControllerV2.class).register(null, null)).withRel("register").getHref(),
                "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Verificar disponibilidad de email (V2 con HATEOAS)",
        description = "Verifica si un email está disponible para registro con enlaces relacionados"
    )
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @Parameter(description = "Email a verificar", required = true, example = "usuario@email.com")
            @PathVariable String email) {
        boolean exists = authService.emailExists(email);
        Map<String, Object> response = Map.of(
            "email", email,
            "available", !exists,
            "message", exists ? "Email ya registrado" : "Email disponible",
            "_links", Map.of(
                "self", linkTo(methodOn(AuthControllerV2.class).checkEmail(email)).withSelfRel().getHref(),
                "register", linkTo(methodOn(AuthControllerV2.class).register(null, null)).withRel("register").getHref(),
                "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desbloquear cuenta de usuario (V2 con HATEOAS)",
        description = "Permite desbloquear una cuenta que fue bloqueada por múltiples intentos fallidos"
    )
    @PostMapping("/unlock/{username}")
    public ResponseEntity<Map<String, Object>> unlockAccount(
            @Parameter(description = "Username de la cuenta a desbloquear", required = true)
            @PathVariable String username) {
        boolean success = authService.unlockAccount(username);
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "Cuenta desbloqueada exitosamente" : "Error al desbloquear cuenta",
            "_links", Map.of(
                "self", linkTo(methodOn(AuthControllerV2.class).unlockAccount(username)).withSelfRel().getHref(),
                "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref(),
                "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cambiar contraseña de usuario (V2 con HATEOAS)",
        description = "Permite a un usuario cambiar su contraseña actual por una nueva con enlaces de navegación"
    )
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Username, contraseña actual y nueva contraseña son requeridos",
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref(),
                    "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref()
                )
            ));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "La nueva contraseña debe tener al menos 6 caracteres",
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV2.class).changePassword(request)).withSelfRel().getHref(),
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref()
                )
            ));
        }

        AuthResponse response = authService.changePassword(username, oldPassword, newPassword);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV2.class).changePassword(request)).withSelfRel().getHref(),
                    "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref(),
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref()
                )
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV2.class).changePassword(request)).withSelfRel().getHref(),
                    "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Estado de salud del servicio de autenticación (V2 con HATEOAS)",
        description = "Endpoint para verificar que el servicio de autenticación está funcionando correctamente"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "Authentication Service V2 (HATEOAS)",
            "timestamp", new java.util.Date().toString(),
            "_links", Map.of(
                "self", linkTo(methodOn(AuthControllerV2.class).health()).withSelfRel().getHref(),
                "auth", linkTo(AuthControllerV2.class).withRel("auth").getHref(),
                "login", linkTo(methodOn(AuthControllerV2.class).login(null, null)).withRel("login").getHref(),
                "register", linkTo(methodOn(AuthControllerV2.class).register(null, null)).withRel("register").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }
}