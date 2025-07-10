package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.dto.UpdateUserRequest;
import com.reparafacilspa.reparaciones.model.User;
import com.reparafacilspa.reparaciones.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceV3 {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    // ===== MÉTODOS EXISTENTES DE V2 =====

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

    // ===== NUEVOS MÉTODOS V3 PARA CRUD COMPLETO =====

    // Obtener todos los usuarios (para administradores)
    public List<AuthResponse.UserInfo> findAllUsers() {
        try {
            return userRepository.findAll()
                    .stream()
                    .map(this::convertToUserInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuarios: " + e.getMessage());
        }
    }

    // Obtener usuario por ID
    public AuthResponse.UserInfo findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return convertToUserInfo(user);
    }

    // Actualizar usuario completo
    public AuthResponse updateUser(Long id, UpdateUserRequest request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            // Validar si el nuevo username ya existe (si es diferente al actual)
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                return AuthResponse.error("El username ya está en uso");
            }

            // Validar si el nuevo email ya existe (si es diferente al actual)
            if (!user.getEmail().equals(request.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                return AuthResponse.error("El email ya está registrado");
            }

            // Actualizar campos
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setNombre(request.getNombre());
            user.setApellido(request.getApellido());
            user.setTelefono(request.getTelefono());
            
            // Solo actualizar contraseña si se proporciona una nueva
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            
            // Solo actualizar rol si se proporciona
            if (request.getRol() != null) {
                try {
                    User.UserRole newRole = User.UserRole.valueOf(request.getRol().toUpperCase());
                    user.setRol(newRole);
                } catch (IllegalArgumentException e) {
                    return AuthResponse.error("Rol inválido: " + request.getRol());
                }
            }

            // Solo actualizar estado activo si se proporciona
            if (request.getActivo() != null) {
                user.setActivo(request.getActivo());
            }

            User updatedUser = userRepository.save(user);
            AuthResponse.UserInfo userInfo = convertToUserInfo(updatedUser);

            return AuthResponse.success("Usuario actualizado exitosamente", userInfo, null);

        } catch (RuntimeException e) {
            return AuthResponse.error(e.getMessage());
        } catch (Exception e) {
            return AuthResponse.error("Error al actualizar usuario: " + e.getMessage());
        }
    }

    // Eliminar usuario (soft delete)
    public AuthResponse deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            // Verificar que no sea el último administrador
            if (user.getRol() == User.UserRole.ADMIN) {
                long totalAdmins = userRepository.findAll().stream()
                        .filter(u -> u.getRol() == User.UserRole.ADMIN && u.getActivo())
                        .count();
                
                if (totalAdmins <= 1) {
                    return AuthResponse.error("No se puede eliminar el último administrador del sistema");
                }
            }

            // Soft delete
            user.setActivo(false);
            userRepository.save(user);

            return AuthResponse.success("Usuario eliminado exitosamente", null, null);

        } catch (RuntimeException e) {
            return AuthResponse.error(e.getMessage());
        } catch (Exception e) {
            return AuthResponse.error("Error al eliminar usuario: " + e.getMessage());
        }
    }

    // Obtener usuarios activos solamente
    public List<AuthResponse.UserInfo> findActiveUsers() {
        try {
            return userRepository.findAll()
                    .stream()
                    .filter(User::getActivo)
                    .map(this::convertToUserInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuarios activos: " + e.getMessage());
        }
    }

    // Obtener usuarios por rol
    public List<AuthResponse.UserInfo> findUsersByRole(String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            return userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRol() == userRole)
                    .map(this::convertToUserInfo)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol inválido: " + role);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuarios por rol: " + e.getMessage());
        }
    }

    // Contar usuarios por rol
    public long countUsersByRole(String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            return userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRol() == userRole && u.getActivo())
                    .count();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    // Obtener estadísticas de usuarios
    public java.util.Map<String, Object> getUserStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsuarios", allUsers.size());
        stats.put("usuariosActivos", allUsers.stream().filter(User::getActivo).count());
        stats.put("usuariosBloqueados", allUsers.stream().filter(User::getCuentaBloqueada).count());
        
        // Contar por rol
        java.util.Map<String, Long> porRol = allUsers.stream()
                .filter(User::getActivo)
                .collect(java.util.stream.Collectors.groupingBy(
                    u -> u.getRol().name(),
                    java.util.stream.Collectors.counting()
                ));
        stats.put("usuariosPorRol", porRol);
        
        return stats;
    }

    // ===== MÉTODOS HELPER =====

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