package com.reparafacilspa.reparaciones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.reparafacilspa.reparaciones.assemblers.UserModelAssembler;
import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.dto.UpdateUserRequest;
import com.reparafacilspa.reparaciones.service.AuthServiceV3;

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
@RequestMapping("/api/v3/auth")
@Tag(name = "Autenticación V3 (HATEOAS + CRUD)", description = "Operaciones completas de autenticación y gestión de usuarios con enlaces HATEOAS y CRUD completo")
public class AuthControllerV3 {

    @Autowired
    private AuthServiceV3 authService;

    @Autowired
    private UserModelAssembler userAssembler;

    // ===== ENDPOINTS HEREDADOS DE V2 =====

    @Operation(
        summary = "Iniciar sesión de usuario (V3 con HATEOAS)",
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
                                "self": {"href": "/api/v3/auth/users/1"},
                                "update": {"href": "/api/v3/auth/users/1"},
                                "users": {"href": "/api/v3/auth/users"},
                                "auth": {"href": "/api/v3/auth"}
                            }
                        },
                        "sessionToken": "abc123-456789"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
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
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                    "register", linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("register").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
                )
            ));
        }

        AuthResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            EntityModel<AuthResponse.UserInfo> userModel = userAssembler.toModel(response.getUser());
            
            Map<String, Object> hateoasResponse = Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "user", userModel,
                "sessionToken", response.getSessionToken(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV3.class).login(loginRequest, bindingResult)).withSelfRel().getHref(),
                    "change-password", linkTo(methodOn(AuthControllerV3.class).changePassword(null)).withRel("change-password").getHref(),
                    "profile", linkTo(methodOn(AuthControllerV3.class).getUserById(response.getUser().getId())).withRel("profile").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
                )
            );
            
            return ResponseEntity.ok(hateoasResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                    "register", linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("register").getHref(),
                    "unlock", linkTo(methodOn(AuthControllerV3.class).unlockAccount(loginRequest.getUsername())).withRel("unlock").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Registrar nuevo usuario (V3 con HATEOAS)",
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
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                    "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
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
                    "self", linkTo(methodOn(AuthControllerV3.class).register(registerRequest, bindingResult)).withSelfRel().getHref(),
                    "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                    "profile", linkTo(methodOn(AuthControllerV3.class).getUserById(response.getUser().getId())).withRel("profile").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
                )
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(hateoasResponse);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                    "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                    "check-username", linkTo(methodOn(AuthControllerV3.class).checkUsername(registerRequest.getUsername())).withRel("check-username").getHref(),
                    "check-email", linkTo(methodOn(AuthControllerV3.class).checkEmail(registerRequest.getEmail())).withRel("check-email").getHref()
                )
            ));
        }
    }

    @Operation(
        summary = "Verificar disponibilidad de username (V3 con HATEOAS)",
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
                "self", linkTo(methodOn(AuthControllerV3.class).checkUsername(username)).withSelfRel().getHref(),
                "register", linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("register").getHref(),
                "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Verificar disponibilidad de email (V3 con HATEOAS)",
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
                "self", linkTo(methodOn(AuthControllerV3.class).checkEmail(email)).withSelfRel().getHref(),
                "register", linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("register").getHref(),
                "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desbloquear cuenta de usuario (V3 con HATEOAS)",
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
                "self", linkTo(methodOn(AuthControllerV3.class).unlockAccount(username)).withSelfRel().getHref(),
                "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cambiar contraseña de usuario (V3 con HATEOAS)",
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
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                    "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
                )
            ));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "La nueva contraseña debe tener al menos 6 caracteres",
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV3.class).changePassword(request)).withSelfRel().getHref(),
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
                )
            ));
        }

        AuthResponse response = authService.changePassword(username, oldPassword, newPassword);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV3.class).changePassword(request)).withSelfRel().getHref(),
                    "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
                )
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "_links", Map.of(
                    "self", linkTo(methodOn(AuthControllerV3.class).changePassword(request)).withSelfRel().getHref(),
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
                )
            ));
        }
    }

    // ===== NUEVOS ENDPOINTS V3 PARA CRUD COMPLETO =====

    @Operation(
        summary = "Obtener todos los usuarios (V3 con HATEOAS)",
        description = "Retorna una lista de todos los usuarios del sistema con enlaces de navegación. " +
                     "Funcionalidad administrativa."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "ListaUsuarios",
                    value = """
                    {
                        "_embedded": {
                            "userInfoList": [
                                {
                                    "id": 1,
                                    "username": "admin",
                                    "email": "admin@reparafacil.com",
                                    "nombre": "Administrador",
                                    "apellido": "Sistema",
                                    "rol": "ADMIN",
                                    "activo": true,
                                    "_links": {
                                        "self": {"href": "/api/v3/auth/users/1"},
                                        "update": {"href": "/api/v3/auth/users/1"},
                                        "delete": {"href": "/api/v3/auth/users/1"}
                                    }
                                }
                            ]
                        },
                        "_links": {
                            "self": {"href": "/api/v3/auth/users"},
                            "create": {"href": "/api/v3/auth/register"},
                            "statistics": {"href": "/api/v3/auth/users/statistics"}
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "204", description = "No hay usuarios registrados")
    })
    @GetMapping("/users")
    public ResponseEntity<CollectionModel<EntityModel<AuthResponse.UserInfo>>> getAllUsers() {
        List<AuthResponse.UserInfo> users = authService.findAllUsers();
        
        if (users.isEmpty()) {
            CollectionModel<EntityModel<AuthResponse.UserInfo>> emptyCollection = 
                CollectionModel.<EntityModel<AuthResponse.UserInfo>>empty()
                    .add(linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withSelfRel())
                    .add(linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("create"))
                    .add(linkTo(AuthControllerV3.class).withRel("auth"));
            return ResponseEntity.ok(emptyCollection);
        }

        CollectionModel<EntityModel<AuthResponse.UserInfo>> usersModel = 
            CollectionModel.of(users.stream()
                .map(userAssembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withSelfRel())
                .add(linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("create"))
                .add(linkTo(methodOn(AuthControllerV3.class).getUserStatistics()).withRel("statistics"))
                .add(linkTo(methodOn(AuthControllerV3.class).getActiveUsers()).withRel("active-users"))
                .add(linkTo(AuthControllerV3.class).withRel("auth"));

        return ResponseEntity.ok(usersModel);
    }

    @Operation(
        summary = "Obtener usuario por ID (V3 con HATEOAS)",
        description = "Retorna los detalles de un usuario específico con enlaces relacionados"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.UserInfo.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<EntityModel<AuthResponse.UserInfo>> getUserById(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id) {
        try {
            AuthResponse.UserInfo user = authService.findUserById(id);
            EntityModel<AuthResponse.UserInfo> userModel = userAssembler.toModel(user);
            return ResponseEntity.ok(userModel);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Actualizar usuario completo (V3 con HATEOAS)",
        description = "Permite actualizar todos los campos de un usuario existente con enlaces de navegación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "UsuarioActualizado",
                    value = """
                    {
                        "success": true,
                        "message": "Usuario actualizado exitosamente",
                        "user": {
                            "id": 1,
                            "username": "admin_actualizado",
                            "email": "admin_nuevo@reparafacil.com",
                            "nombre": "Administrador",
                            "apellido": "Actualizado",
                            "rol": "ADMIN",
                            "activo": true,
                            "_links": {
                                "self": {"href": "/api/v3/auth/users/1"},
                                "delete": {"href": "/api/v3/auth/users/1"},
                                "users": {"href": "/api/v3/auth/users"}
                            }
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Errores de validación o datos duplicados"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos actualizados del usuario",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = UpdateUserRequest.class),
                    examples = @ExampleObject(
                        name = "ActualizarUsuario",
                        value = """
                        {
                            "username": "usuario_actualizado",
                            "email": "actualizado@email.com",
                            "password": "nuevapassword",
                            "nombre": "Nombre Actualizado",
                            "apellido": "Apellido Actualizado",
                            "telefono": "+56987654321",
                            "rol": "CLIENTE",
                            "activo": true
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody UpdateUserRequest updateRequest,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Errores de validación: " + errors,
                "_links", Map.of(
                    "user", linkTo(methodOn(AuthControllerV3.class).getUserById(id)).withRel("user").getHref(),
                    "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref(),
                    "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
                )
            ));
        }

        try {
            AuthResponse response = authService.updateUser(id, updateRequest);
            
            if (response.isSuccess()) {
                EntityModel<AuthResponse.UserInfo> userModel = userAssembler.toModel(response.getUser());
                
                Map<String, Object> hateoasResponse = Map.of(
                    "success", response.isSuccess(),
                    "message", response.getMessage(),
                    "user", userModel,
                    "_links", Map.of(
                        "self", linkTo(methodOn(AuthControllerV3.class).updateUser(id, updateRequest, bindingResult)).withSelfRel().getHref(),
                        "user", linkTo(methodOn(AuthControllerV3.class).getUserById(id)).withRel("user").getHref(),
                        "delete", linkTo(methodOn(AuthControllerV3.class).deleteUser(id)).withRel("delete").getHref(),
                        "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref()
                    )
                );
                
                return ResponseEntity.ok(hateoasResponse);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", response.isSuccess(),
                    "message", response.getMessage(),
                    "_links", Map.of(
                        "user", linkTo(methodOn(AuthControllerV3.class).getUserById(id)).withRel("user").getHref(),
                        "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref(),
                        "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
                    )
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Eliminar usuario (V3 con HATEOAS)",
        description = "Realiza un soft delete del usuario (lo marca como inactivo) con enlaces de navegación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario eliminado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "UsuarioEliminado",
                    value = """
                    {
                        "success": true,
                        "message": "Usuario eliminado exitosamente",
                        "_links": {
                            "users": {"href": "/api/v3/auth/users"},
                            "active-users": {"href": "/api/v3/auth/users/active"},
                            "statistics": {"href": "/api/v3/auth/users/statistics"},
                            "auth": {"href": "/api/v3/auth"}
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "No se puede eliminar (ej: último administrador)"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        try {
            AuthResponse response = authService.deleteUser(id);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", response.isSuccess(),
                    "message", response.getMessage(),
                    "_links", Map.of(
                        "self", linkTo(methodOn(AuthControllerV3.class).deleteUser(id)).withSelfRel().getHref(),
                        "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref(),
                        "active-users", linkTo(methodOn(AuthControllerV3.class).getActiveUsers()).withRel("active-users").getHref(),
                        "statistics", linkTo(methodOn(AuthControllerV3.class).getUserStatistics()).withRel("statistics").getHref(),
                        "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
                    )
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", response.isSuccess(),
                    "message", response.getMessage(),
                    "_links", Map.of(
                        "user", linkTo(methodOn(AuthControllerV3.class).getUserById(id)).withRel("user").getHref(),
                        "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref(),
                        "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
                    )
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== ENDPOINTS ADICIONALES V3 =====

    @Operation(
        summary = "Obtener usuarios activos (V3 con HATEOAS)",
        description = "Retorna solo los usuarios que están activos en el sistema"
    )
    @GetMapping("/users/active")
    public ResponseEntity<CollectionModel<EntityModel<AuthResponse.UserInfo>>> getActiveUsers() {
        List<AuthResponse.UserInfo> activeUsers = authService.findActiveUsers();
        
        CollectionModel<EntityModel<AuthResponse.UserInfo>> usersModel = 
            CollectionModel.of(activeUsers.stream()
                .map(userAssembler::toModel)
                .collect(Collectors.toList()))
                .add(linkTo(methodOn(AuthControllerV3.class).getActiveUsers()).withSelfRel())
                .add(linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("all-users"))
                .add(linkTo(methodOn(AuthControllerV3.class).getUserStatistics()).withRel("statistics"))
                .add(linkTo(AuthControllerV3.class).withRel("auth"));

        return ResponseEntity.ok(usersModel);
    }

    @Operation(
        summary = "Obtener usuarios por rol (V3 con HATEOAS)",
        description = "Retorna usuarios filtrados por su rol en el sistema"
    )
    @GetMapping("/users/role/{role}")
    public ResponseEntity<CollectionModel<EntityModel<AuthResponse.UserInfo>>> getUsersByRole(
            @Parameter(
                description = "Rol del usuario", 
                required = true, 
                example = "ADMIN",
                schema = @Schema(allowableValues = {"ADMIN", "EMPRENDEDOR", "CLIENTE"})
            )
            @PathVariable String role) {
        try {
            List<AuthResponse.UserInfo> users = authService.findUsersByRole(role);
            
            CollectionModel<EntityModel<AuthResponse.UserInfo>> usersModel = 
                CollectionModel.of(users.stream()
                    .map(userAssembler::toModel)
                    .collect(Collectors.toList()))
                    .add(linkTo(methodOn(AuthControllerV3.class).getUsersByRole(role)).withSelfRel())
                    .add(linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("all-users"))
                    .add(linkTo(methodOn(AuthControllerV3.class).getUserStatistics()).withRel("statistics"))
                    .add(linkTo(AuthControllerV3.class).withRel("auth"));

            return ResponseEntity.ok(usersModel);
        } catch (Exception e) {
            CollectionModel<EntityModel<AuthResponse.UserInfo>> errorModel = 
                CollectionModel.<EntityModel<AuthResponse.UserInfo>>empty()
                    .add(linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users"))
                    .add(linkTo(AuthControllerV3.class).withRel("auth"));
            return ResponseEntity.badRequest().body(errorModel);
        }
    }

    @Operation(
        summary = "Obtener estadísticas de usuarios (V3 con HATEOAS)",
        description = "Retorna métricas y estadísticas generales de usuarios del sistema"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Estadísticas obtenidas exitosamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "EstadisticasUsuarios",
                value = """
                {
                    "totalUsuarios": 57,
                    "usuariosActivos": 54,
                    "usuariosBloqueados": 2,
                    "usuariosPorRol": {
                        "ADMIN": 1,
                        "EMPRENDEDOR": 6,
                        "CLIENTE": 50
                    },
                    "_links": {
                        "self": {"href": "/api/v3/auth/users/statistics"},
                        "users": {"href": "/api/v3/auth/users"},
                        "active-users": {"href": "/api/v3/auth/users/active"},
                        "admins": {"href": "/api/v3/auth/users/role/ADMIN"},
                        "emprendedores": {"href": "/api/v3/auth/users/role/EMPRENDEDOR"},
                        "clientes": {"href": "/api/v3/auth/users/role/CLIENTE"}
                    }
                }
                """
            )
        )
    )
    @GetMapping("/users/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = authService.getUserStatistics();
        
        // Agregar enlaces HATEOAS a las estadísticas
        Map<String, Object> linksMap = Map.of(
            "self", linkTo(methodOn(AuthControllerV3.class).getUserStatistics()).withSelfRel().getHref(),
            "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref(),
            "active-users", linkTo(methodOn(AuthControllerV3.class).getActiveUsers()).withRel("active-users").getHref(),
            "admins", linkTo(methodOn(AuthControllerV3.class).getUsersByRole("ADMIN")).withRel("admins").getHref(),
            "emprendedores", linkTo(methodOn(AuthControllerV3.class).getUsersByRole("EMPRENDEDOR")).withRel("emprendedores").getHref(),
            "clientes", linkTo(methodOn(AuthControllerV3.class).getUsersByRole("CLIENTE")).withRel("clientes").getHref(),
            "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref()
        );
        
        stats.put("_links", linksMap);
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Estado de salud del servicio de autenticación (V3 con HATEOAS)",
        description = "Endpoint para verificar que el servicio de autenticación está funcionando correctamente"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "Authentication Service V3 (HATEOAS + CRUD)",
            "timestamp", new java.util.Date().toString(),
            "version", "3.0.0",
            "_links", Map.of(
                "self", linkTo(methodOn(AuthControllerV3.class).health()).withSelfRel().getHref(),
                "auth", linkTo(AuthControllerV3.class).withRel("auth").getHref(),
                "login", linkTo(methodOn(AuthControllerV3.class).login(null, null)).withRel("login").getHref(),
                "register", linkTo(methodOn(AuthControllerV3.class).register(null, null)).withRel("register").getHref(),
                "users", linkTo(methodOn(AuthControllerV3.class).getAllUsers()).withRel("users").getHref(),
                "statistics", linkTo(methodOn(AuthControllerV3.class).getUserStatistics()).withRel("statistics").getHref()
            )
        );
        return ResponseEntity.ok(response);
    }
}