package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.dto.UpdateUserRequest;
import com.reparafacilspa.reparaciones.model.User;
import com.reparafacilspa.reparaciones.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceV3 - Pruebas unitarias")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceV3 authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private UpdateUserRequest updateRequest;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setNombre("Test");
        testUser.setApellido("User");
        testUser.setTelefono("+56912345678");
        testUser.setRol(User.UserRole.CLIENTE);
        testUser.setActivo(true);
        testUser.setFechaCreacion(new Date());
        testUser.setIntentosLogin(0);
        testUser.setCuentaBloqueada(false);

        // Request de login
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Request de registro
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@email.com");
        registerRequest.setPassword("password123");
        registerRequest.setNombre("New");
        registerRequest.setApellido("User");
        registerRequest.setTelefono("+56987654321");

        // Request de actualización
        updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@email.com");
        updateRequest.setNombre("Updated");
        updateRequest.setApellido("User");
        updateRequest.setTelefono("+56999888777");
        updateRequest.setRol("CLIENTE");
        updateRequest.setActivo(true);
    }

    // ===== PRUEBAS DE LOGIN =====

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login exitoso", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertNotNull(response.getSessionToken());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Login falla - usuario no encontrado")
    void testLoginUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Usuario no encontrado", response.getMessage());
        assertNull(response.getUser());
        assertNull(response.getSessionToken());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login falla - cuenta desactivada")
    void testLoginAccountDeactivated() {
        // Arrange
        testUser.setActivo(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cuenta desactivada. Contacte al administrador", response.getMessage());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login falla - cuenta bloqueada")
    void testLoginAccountBlocked() {
        // Arrange
        testUser.setCuentaBloqueada(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cuenta bloqueada por múltiples intentos fallidos", response.getMessage());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login falla - contraseña incorrecta")
    void testLoginWrongPassword() {
        // Arrange
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Contraseña incorrecta"));
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class)); // Para incrementar intentos
    }

    @Test
    @DisplayName("Login falla - cuenta se bloquea después de 5 intentos fallidos")
    void testLoginAccountGetsBlockedAfterMaxAttempts() {
        // Arrange
        testUser.setIntentosLogin(4); // Ya tiene 4 intentos fallidos
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cuenta bloqueada por múltiples intentos fallidos", response.getMessage());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(argThat(user -> 
            user.getIntentosLogin() == 5 && user.getCuentaBloqueada()));
    }

    // ===== PRUEBAS DE REGISTRO =====

    @Test
    @DisplayName("Registro exitoso con datos válidos")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Usuario registrado exitosamente", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("newuser", response.getUser().getUsername());
        assertEquals("new@email.com", response.getUser().getEmail());
        assertNotNull(response.getSessionToken());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@email.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Registro falla - username ya existe")
    void testRegisterUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("El username ya está en uso", response.getMessage());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registro falla - email ya existe")
    void testRegisterEmailExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(true);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("El email ya está registrado", response.getMessage());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // ===== PRUEBAS DE VERIFICACIÓN =====

    @Test
    @DisplayName("userExists retorna true cuando el usuario existe")
    void testUserExistsTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean exists = authService.userExists("testuser");

        // Assert
        assertTrue(exists);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("userExists retorna false cuando el usuario no existe")
    void testUserExistsFalse() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean exists = authService.userExists("nonexistent");

        // Assert
        assertFalse(exists);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    @DisplayName("emailExists retorna true cuando el email existe")
    void testEmailExistsTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        // Act
        boolean exists = authService.emailExists("test@email.com");

        // Assert
        assertTrue(exists);
        verify(userRepository).existsByEmail("test@email.com");
    }

    // ===== PRUEBAS DE DESBLOQUEO =====

    @Test
    @DisplayName("Desbloqueo exitoso de cuenta")
    void testUnlockAccountSuccess() {
        // Arrange
        testUser.setCuentaBloqueada(true);
        testUser.setIntentosLogin(5);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = authService.unlockAccount("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(argThat(user -> 
            !user.getCuentaBloqueada() && user.getIntentosLogin() == 0));
    }

    @Test
    @DisplayName("Desbloqueo falla - usuario no encontrado")
    void testUnlockAccountUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        boolean result = authService.unlockAccount("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    // ===== PRUEBAS DE CAMBIO DE CONTRASEÑA =====

    @Test
    @DisplayName("Cambio de contraseña exitoso")
    void testChangePasswordSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.changePassword("testuser", "password123", "newpassword123");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Contraseña actualizada exitosamente", response.getMessage());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Cambio de contraseña falla - usuario no encontrado")
    void testChangePasswordUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        AuthResponse response = authService.changePassword("nonexistent", "oldpass", "newpass");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Usuario no encontrado", response.getMessage());
        
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Cambio de contraseña falla - contraseña actual incorrecta")
    void testChangePasswordWrongCurrentPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.changePassword("testuser", "wrongpassword", "newpassword123");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Contraseña actual incorrecta", response.getMessage());
        
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    // ===== PRUEBAS CRUD V3 =====

    @Test
    @DisplayName("findAllUsers retorna lista de usuarios")
    void testFindAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@email.com");
        user2.setNombre("User");
        user2.setApellido("Two");
        user2.setRol(User.UserRole.EMPRENDEDOR);
        user2.setActivo(true);

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<AuthResponse.UserInfo> result = authService.findAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("findUserById retorna usuario existente")
    void testFindUserByIdSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse.UserInfo result = authService.findUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@email.com", result.getEmail());
        
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findUserById lanza excepción cuando usuario no existe")
    void testFindUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.findUserById(999L));
        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
        
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("updateUser exitoso con datos válidos")
    void testUpdateUserSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.updateUser(1L, updateRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Usuario actualizado exitosamente", response.getMessage());
        assertNotNull(response.getUser());
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("updateduser");
        verify(userRepository).existsByEmail("updated@email.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser falla - username ya existe")
    void testUpdateUserUsernameExists() {
        // Arrange
        updateRequest.setUsername("existinguser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act
        AuthResponse response = authService.updateUser(1L, updateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("El username ya está en uso", response.getMessage());
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("deleteUser exitoso")
    void testDeleteUserSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, createAdminUser()));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.deleteUser(1L);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Usuario eliminado exitosamente", response.getMessage());
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user -> !user.getActivo()));
    }

    @Test
    @DisplayName("deleteUser falla - último administrador")
    void testDeleteUserLastAdmin() {
        // Arrange
        testUser.setRol(User.UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser)); // Solo un admin

        // Act
        AuthResponse response = authService.deleteUser(1L);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("No se puede eliminar el último administrador del sistema", response.getMessage());
        
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("findActiveUsers retorna solo usuarios activos")
    void testFindActiveUsers() {
        // Arrange
        User inactiveUser = new User();
        inactiveUser.setId(3L);
        inactiveUser.setUsername("inactive");
        inactiveUser.setActivo(false);

        List<User> allUsers = Arrays.asList(testUser, inactiveUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<AuthResponse.UserInfo> result = authService.findActiveUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertTrue(result.get(0).getActivo());
        
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("findUsersByRole retorna usuarios del rol especificado")
    void testFindUsersByRole() {
        // Arrange
        User emprendedor = createEmprendedorUser();
        List<User> allUsers = Arrays.asList(testUser, emprendedor);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<AuthResponse.UserInfo> result = authService.findUsersByRole("EMPRENDEDOR");

        // Assert
        assertEquals(1, result.size());
        assertEquals("EMPRENDEDOR", result.get(0).getRol());
        
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("findUsersByRole lanza excepción con rol inválido")
    void testFindUsersByRoleInvalidRole() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.findUsersByRole("INVALID_ROLE"));
        assertEquals("Rol inválido: INVALID_ROLE", exception.getMessage());
    }

    @Test
    @DisplayName("countUsersByRole cuenta usuarios por rol")
    void testCountUsersByRole() {
        // Arrange
        List<User> users = Arrays.asList(testUser, createEmprendedorUser());
        when(userRepository.findAll()).thenReturn(users);

        // Act
        long count = authService.countUsersByRole("CLIENTE");

        // Assert
        assertEquals(1, count);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getUserStatistics retorna estadísticas completas")
    void testGetUserStatistics() {
        // Arrange
        User blockedUser = new User();
        blockedUser.setCuentaBloqueada(true);
        blockedUser.setActivo(true);
        blockedUser.setRol(User.UserRole.CLIENTE);

        List<User> users = Arrays.asList(testUser, createEmprendedorUser(), blockedUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        java.util.Map<String, Object> stats = authService.getUserStatistics();

        // Assert
        assertEquals(3, stats.get("totalUsuarios"));
        assertEquals(3L, stats.get("usuariosActivos"));
        assertEquals(1L, stats.get("usuariosBloqueados"));
        
        @SuppressWarnings("unchecked")
        java.util.Map<String, Long> porRol = (java.util.Map<String, Long>) stats.get("usuariosPorRol");
        assertEquals(2L, porRol.get("CLIENTE"));
        assertEquals(1L, porRol.get("EMPRENDEDOR"));
        
        verify(userRepository).findAll();
    }

    // ===== MÉTODOS HELPER =====

    private User createAdminUser() {
        User admin = new User();
        admin.setId(10L);
        admin.setUsername("admin");
        admin.setEmail("admin@email.com");
        admin.setRol(User.UserRole.ADMIN);
        admin.setActivo(true);
        return admin;
    }

    private User createEmprendedorUser() {
        User emprendedor = new User();
        emprendedor.setId(5L);
        emprendedor.setUsername("emprendedor");
        emprendedor.setEmail("emprendedor@email.com");
        emprendedor.setRol(User.UserRole.EMPRENDEDOR);
        emprendedor.setActivo(true);
        return emprendedor;
    }
}