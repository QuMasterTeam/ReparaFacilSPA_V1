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

import jakarta.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Login de usuario
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, 
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

    // Registro de usuario
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
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

    // Verificar disponibilidad de username
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsername(@PathVariable String username) {
        boolean exists = authService.userExists(username);
        Map<String, Object> response = Map.of(
            "username", username,
            "available", !exists,
            "message", exists ? "Username no disponible" : "Username disponible"
        );
        return ResponseEntity.ok(response);
    }

    // Verificar disponibilidad de email
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(@PathVariable String email) {
        boolean exists = authService.emailExists(email);
        Map<String, Object> response = Map.of(
            "email", email,
            "available", !exists,
            "message", exists ? "Email ya registrado" : "Email disponible"
        );
        return ResponseEntity.ok(response);
    }

    // Desbloquear cuenta (solo para administradores)
    @PostMapping("/unlock/{username}")
    public ResponseEntity<Map<String, Object>> unlockAccount(@PathVariable String username) {
        boolean success = authService.unlockAccount(username);
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "Cuenta desbloqueada exitosamente" : "Error al desbloquear cuenta"
        );
        return ResponseEntity.ok(response);
    }

    // Cambiar contraseña
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody Map<String, String> request) {
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

    // Endpoint de salud para auth
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