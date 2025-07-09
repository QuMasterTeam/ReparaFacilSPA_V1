package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.model.User;
import com.reparafacilspa.reparaciones.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    // Login de usuario
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Buscar usuario por username
            Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
            
            if (userOptional.isEmpty()) {
                return AuthResponse.error("Usuario no encontrado");
            }

            User user = userOptional.get();

            // Verificar si la cuenta está activa
            if (!user.getActivo()) {
                return AuthResponse.error("Cuenta desactivada. Contacte al administrador");
            }

            // Verificar si la cuenta está bloqueada
            if (user.getCuentaBloqueada()) {
                return AuthResponse.error("Cuenta bloqueada por múltiples intentos fallidos");
            }

            // Verificar contraseña
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Incrementar intentos de login
                user.setIntentosLogin(user.getIntentosLogin() + 1);
                
                // Bloquear cuenta si supera los intentos máximos
                if (user.getIntentosLogin() >= MAX_LOGIN_ATTEMPTS) {
                    user.setCuentaBloqueada(true);
                    userRepository.save(user);
                    return AuthResponse.error("Cuenta bloqueada por múltiples intentos fallidos");
                }
                
                userRepository.save(user);
                return AuthResponse.error("Contraseña incorrecta. Intentos restantes: " + 
                    (MAX_LOGIN_ATTEMPTS - user.getIntentosLogin()));
            }

            // Login exitoso - resetear intentos y actualizar último login
            user.setIntentosLogin(0);
            user.setUltimoLogin(new Date());
            userRepository.save(user);

            // Crear UserInfo desde User
            AuthResponse.UserInfo userInfo = convertToUserInfo(user);

            // Generar token de sesión simple
            String sessionToken = generateSessionToken();

            return AuthResponse.success("Login exitoso", userInfo, sessionToken);

        } catch (Exception e) {
            return AuthResponse.error("Error interno del servidor");
        }
    }

    // Registro de usuario
    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            // Verificar si el username ya existe
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return AuthResponse.error("El username ya está en uso");
            }

            // Verificar si el email ya existe
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return AuthResponse.error("El email ya está registrado");
            }

            // Crear nuevo usuario
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setNombre(registerRequest.getNombre());
            newUser.setApellido(registerRequest.getApellido());
            newUser.setTelefono(registerRequest.getTelefono());
            newUser.setRol(User.UserRole.CLIENTE); // Por defecto es cliente
            newUser.setActivo(true);
            newUser.setFechaCreacion(new Date());
            newUser.setIntentosLogin(0);
            newUser.setCuentaBloqueada(false);

            // Guardar usuario
            User savedUser = userRepository.save(newUser);

            // Crear UserInfo desde User
            AuthResponse.UserInfo userInfo = convertToUserInfo(savedUser);

            // Generar token de sesión
            String sessionToken = generateSessionToken();

            return AuthResponse.success("Usuario registrado exitosamente", userInfo, sessionToken);

        } catch (Exception e) {
            return AuthResponse.error("Error al registrar usuario: " + e.getMessage());
        }
    }

    // Verificar si un usuario existe
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // Verificar si un email existe
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Desbloquear cuenta (para administradores)
    public boolean unlockAccount(String username) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setCuentaBloqueada(false);
                user.setIntentosLogin(0);
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Cambiar contraseña
    public AuthResponse changePassword(String username, String oldPassword, String newPassword) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                return AuthResponse.error("Usuario no encontrado");
            }

            User user = userOptional.get();

            // Verificar contraseña actual
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return AuthResponse.error("Contraseña actual incorrecta");
            }

            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return AuthResponse.success("Contraseña actualizada exitosamente", null, null);

        } catch (Exception e) {
            return AuthResponse.error("Error al cambiar contraseña");
        }
    }

    // Método helper para convertir User a UserInfo
    private AuthResponse.UserInfo convertToUserInfo(User user) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setNombre(user.getNombre());
        userInfo.setApellido(user.getApellido());
        userInfo.setTelefono(user.getTelefono());
        userInfo.setRol(user.getRol().name());
        userInfo.setActivo(user.getActivo());
        return userInfo;
    }

    // Generar token de sesión simple
    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}