package com.reparafacilspa.reparaciones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Operaciones de autenticación, registro y gestión de usuarios")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(
        summary = "Iniciar sesión de usuario",
        description = "Permite a un usuario autenticarse en el sistema con username y contraseña. " +
                     "Retorna información del usuario y token de sesión si es exitoso."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "LoginExitoso",
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
                            "activo": true
                        },
                        "sessionToken": "abc123-456789"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "LoginFallido",
                    value = """
                    {
                        "success": false,
                        "message": "Usuario no encontrado",
                        "user": null,
                        "sessionToken": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Errores de validación en los datos de entrada"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciales de usuario para iniciar sesión",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                        name = "EjemploLogin",
                        value = """
                        {
                            "username": "admin",
                            "password": "123456"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody LoginRequest loginRequest, 
            BindingResult bindingResult) {
        
        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(AuthResponse.error("Errores de validación: " + errors));
        }

        AuthResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Permite registrar un nuevo usuario en el sistema. " +
                     "El usuario se crea con rol CLIENTE por defecto."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Usuario registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Usuario ya existe o datos inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "UsuarioExiste",
                    value = """
                    {
                        "success": false,
                        "message": "El username ya está en uso",
                        "user": null,
                        "sessionToken": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del nuevo usuario a registrar",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                        name = "EjemploRegistro",
                        value = """
                        {
                            "username": "nuevousuario",
                            "email": "nuevo@email.com",
                            "password": "123456",
                            "nombre": "Juan",
                            "apellido": "Pérez",
                            "telefono": "+56912345678"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult) {
        
        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(AuthResponse.error("Errores de validación: " + errors));
        }

        AuthResponse response = authService.register(registerRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Verificar disponibilidad de username",
        description = "Verifica si un username está disponible para registro"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Consulta exitosa",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "UsernameDisponible",
                    value = """
                    {
                        "username": "nuevousuario",
                        "available": true,
                        "message": "Username disponible"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsername(
            @Parameter(description = "Username a verificar", required = true, example = "nuevousuario")
            @PathVariable String username) {
        boolean exists = authService.userExists(username);
        Map<String, Object> response = Map.of(
            "username", username,
            "available", !exists,
            "message", exists ? "Username no disponible" : "Username disponible"
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Verificar disponibilidad de email",
        description = "Verifica si un email está disponible para registro"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Consulta exitosa",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "EmailDisponible",
                    value = """
                    {
                        "email": "nuevo@email.com",
                        "available": true,
                        "message": "Email disponible"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @Parameter(description = "Email a verificar", required = true, example = "usuario@email.com")
            @PathVariable String email) {
        boolean exists = authService.emailExists(email);
        Map<String, Object> response = Map.of(
            "email", email,
            "available", !exists,
            "message", exists ? "Email ya registrado" : "Email disponible"
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desbloquear cuenta de usuario",
        description = "Permite desbloquear una cuenta que fue bloqueada por múltiples intentos fallidos. " +
                     "Solo para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Operación completada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "CuentaDesbloqueada",
                    value = """
                    {
                        "success": true,
                        "message": "Cuenta desbloqueada exitosamente"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/unlock/{username}")
    public ResponseEntity<Map<String, Object>> unlockAccount(
            @Parameter(description = "Username de la cuenta a desbloquear", required = true)
            @PathVariable String username) {
        boolean success = authService.unlockAccount(username);
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "Cuenta desbloqueada exitosamente" : "Error al desbloquear cuenta"
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cambiar contraseña de usuario",
        description = "Permite a un usuario cambiar su contraseña actual por una nueva"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Contraseña cambiada exitosamente"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Contraseña actual incorrecta o datos inválidos"
        )
    })
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos para cambio de contraseña",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "CambioPassword",
                        value = """
                        {
                            "username": "usuario123",
                            "oldPassword": "password_actual",
                            "newPassword": "nueva_password"
                        }
                        """
                    )
                )
            )
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.error("Username, contraseña actual y nueva contraseña son requeridos"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.error("La nueva contraseña debe tener al menos 6 caracteres"));
        }

        AuthResponse response = authService.changePassword(username, oldPassword, newPassword);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Estado de salud del servicio de autenticación",
        description = "Endpoint para verificar que el servicio de autenticación está funcionando correctamente"
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
                    "service": "Authentication Service",
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
            "service", "Authentication Service",
            "timestamp", new java.util.Date().toString()
        );
        return ResponseEntity.ok(response);
    }
}