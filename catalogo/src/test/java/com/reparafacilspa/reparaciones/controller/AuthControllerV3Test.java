package com.reparafacilspa.reparaciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reparafacilspa.reparaciones.assemblers.UserModelAssembler;
import com.reparafacilspa.reparaciones.dto.AuthResponse;
import com.reparafacilspa.reparaciones.dto.LoginRequest;
import com.reparafacilspa.reparaciones.dto.RegisterRequest;
import com.reparafacilspa.reparaciones.dto.UpdateUserRequest;
import com.reparafacilspa.reparaciones.service.AuthServiceV3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthControllerV3.class)
@ActiveProfiles("test")
@DisplayName("AuthControllerV3 - Pruebas de integración")
class AuthControllerV3Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthServiceV3 authService;

    @MockBean
    private UserModelAssembler userAssembler;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse.UserInfo testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Usuario de prueba
        testUser = new AuthResponse.UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setNombre("Test");
        testUser.setApellido("User");
        testUser.setTelefono("+56912345678");
        testUser.setRol("CLIENTE");
        testUser.setActivo(true);

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
    @DisplayName("POST /login - Login exitoso")
    void testLoginSuccess() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.success("Login exitoso", testUser, "token123");
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.sessionToken").value("token123"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /login - Login fallido")
    void testLoginFailure() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.error("Usuario no encontrado");
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /login - Datos inválidos")
    void testLoginValidationError() throws Exception {
        // Arrange
        loginRequest.setUsername(""); // Username inválido

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("validación")))
                .andExpect(jsonPath("$._links").exists());

        verify(authService, never()).login(any());
    }

    // ===== PRUEBAS DE REGISTRO =====

    @Test
    @DisplayName("POST /register - Registro exitoso")
    void testRegisterSuccess() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.success("Usuario registrado exitosamente", testUser, "token123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /register - Usuario ya existe")
    void testRegisterUserExists() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.error("El username ya está en uso");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("El username ya está en uso"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).register(any(RegisterRequest.class));
    }

    // ===== PRUEBAS DE VERIFICACIÓN =====

    @Test
    @DisplayName("GET /check-username/{username} - Username disponible")
    void testCheckUsernameAvailable() throws Exception {
        // Arrange
        when(authService.userExists("newuser")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/check-username/newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Username disponible"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).userExists("newuser");
    }

    @Test
    @DisplayName("GET /check-username/{username} - Username no disponible")
    void testCheckUsernameNotAvailable() throws Exception {
        // Arrange
        when(authService.userExists("existinguser")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/check-username/existinguser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("existinguser"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("Username no disponible"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).userExists("existinguser");
    }

    @Test
    @DisplayName("GET /check-email/{email} - Email disponible")
    void testCheckEmailAvailable() throws Exception {
        // Arrange
        when(authService.emailExists("new@email.com")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/check-email/new@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@email.com"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Email disponible"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).emailExists("new@email.com");
    }

    // ===== PRUEBAS DE DESBLOQUEO =====

    @Test
    @DisplayName("POST /unlock/{username} - Desbloqueo exitoso")
    void testUnlockAccountSuccess() throws Exception {
        // Arrange
        when(authService.unlockAccount("blockeduser")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/unlock/blockeduser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cuenta desbloqueada exitosamente"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).unlockAccount("blockeduser");
    }

    @Test
    @DisplayName("POST /unlock/{username} - Error al desbloquear")
    void testUnlockAccountFailure() throws Exception {
        // Arrange
        when(authService.unlockAccount("nonexistent")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/unlock/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al desbloquear cuenta"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).unlockAccount("nonexistent");
    }

    // ===== PRUEBAS DE CAMBIO DE CONTRASEÑA =====

    @Test
    @DisplayName("POST /change-password - Cambio exitoso")
    void testChangePasswordSuccess() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("oldPassword", "oldpass");
        request.put("newPassword", "newpass123");

        AuthResponse authResponse = AuthResponse.success("Contraseña actualizada exitosamente", null, null);
        when(authService.changePassword("testuser", "oldpass", "newpass123")).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Contraseña actualizada exitosamente"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).changePassword("testuser", "oldpass", "newpass123");
    }

    @Test
    @DisplayName("POST /change-password - Datos faltantes")
    void testChangePasswordMissingData() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        // Falta oldPassword y newPassword

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("requeridos")))
                .andExpect(jsonPath("$._links").exists());

        verify(authService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /change-password - Contraseña muy corta")
    void testChangePasswordTooShort() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("oldPassword", "oldpass");
        request.put("newPassword", "123"); // Muy corta

        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("al menos 6 caracteres")))
                .andExpect(jsonPath("$._links").exists());

        verify(authService, never()).changePassword(anyString(), anyString(), anyString());
    }

    // ===== PRUEBAS CRUD V3 =====

    @Test
    @DisplayName("GET /users - Obtener todos los usuarios")
    void testGetAllUsers() throws Exception {
        // Arrange
        List<AuthResponse.UserInfo> users = Arrays.asList(testUser, createOtherUser());
        when(authService.findAllUsers()).thenReturn(users);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists());

        verify(authService).findAllUsers();
    }

    @Test
    @DisplayName("GET /users - Lista vacía")
    void testGetAllUsersEmpty() throws Exception {
        // Arrange
        when(authService.findAllUsers()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists());

        verify(authService).findAllUsers();
    }

    @Test
    @DisplayName("GET /users/{id} - Usuario encontrado")
    void testGetUserByIdSuccess() throws Exception {
        // Arrange
        when(authService.findUserById(1L)).thenReturn(testUser);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users/1"))
                .andExpect(status().isOk());

        verify(authService).findUserById(1L);
    }

    @Test
    @DisplayName("GET /users/{id} - Usuario no encontrado")
    void testGetUserByIdNotFound() throws Exception {
        // Arrange
        when(authService.findUserById(999L)).thenThrow(new RuntimeException("Usuario no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users/999"))
                .andExpect(status().isNotFound());

        verify(authService).findUserById(999L);
    }

    @Test
    @DisplayName("PUT /users/{id} - Actualización exitosa")
    void testUpdateUserSuccess() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.success("Usuario actualizado exitosamente", testUser, null);
        when(authService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(authResponse);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(put("/api/v3/auth/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario actualizado exitosamente"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /users/{id} - Datos inválidos")
    void testUpdateUserValidationError() throws Exception {
        // Arrange
        updateRequest.setUsername("ab"); // Muy corto

        // Act & Assert
        mockMvc.perform(put("/api/v3/auth/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("validación")))
                .andExpect(jsonPath("$._links").exists());

        verify(authService, never()).updateUser(anyLong(), any());
    }

    @Test
    @DisplayName("PUT /users/{id} - Usuario ya existe")
    void testUpdateUserAlreadyExists() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.error("El username ya está en uso");
        when(authService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v3/auth/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("El username ya está en uso"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Eliminación exitosa")
    void testDeleteUserSuccess() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.success("Usuario eliminado exitosamente", null, null);
        when(authService.deleteUser(1L)).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/v3/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario eliminado exitosamente"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /users/{id} - No se puede eliminar último admin")
    void testDeleteUserLastAdmin() throws Exception {
        // Arrange
        AuthResponse authResponse = AuthResponse.error("No se puede eliminar el último administrador del sistema");
        when(authService.deleteUser(1L)).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/v3/auth/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No se puede eliminar el último administrador del sistema"))
                .andExpect(jsonPath("$._links").exists());

        verify(authService).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /users/{id} - Usuario no encontrado")
    void testDeleteUserNotFound() throws Exception {
        // Arrange
        when(authService.deleteUser(999L)).thenThrow(new RuntimeException("Usuario no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(delete("/api/v3/auth/users/999"))
                .andExpect(status().isNotFound());

        verify(authService).deleteUser(999L);
    }

    // ===== PRUEBAS ENDPOINTS ADICIONALES =====

    @Test
    @DisplayName("GET /users/active - Usuarios activos")
    void testGetActiveUsers() throws Exception {
        // Arrange
        List<AuthResponse.UserInfo> activeUsers = Arrays.asList(testUser);
        when(authService.findActiveUsers()).thenReturn(activeUsers);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists());

        verify(authService).findActiveUsers();
    }

    @Test
    @DisplayName("GET /users/role/{role} - Usuarios por rol")
    void testGetUsersByRole() throws Exception {
        // Arrange
        List<AuthResponse.UserInfo> clientUsers = Arrays.asList(testUser);
        when(authService.findUsersByRole("CLIENTE")).thenReturn(clientUsers);
        when(userAssembler.toModel(any())).thenReturn(null); // Mock HATEOAS

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users/role/CLIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists());

        verify(authService).findUsersByRole("CLIENTE");
    }

    @Test
    @DisplayName("GET /users/role/{role} - Rol inválido")
    void testGetUsersByRoleInvalid() throws Exception {
        // Arrange
        when(authService.findUsersByRole("INVALID_ROLE")).thenThrow(new RuntimeException("Rol inválido: INVALID_ROLE"));

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users/role/INVALID_ROLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$._links").exists());

        verify(authService).findUsersByRole("INVALID_ROLE");
    }

    @Test
    @DisplayName("GET /users/statistics - Estadísticas de usuarios")
    void testGetUserStatistics() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", 10);
        stats.put("usuariosActivos", 8);
        stats.put("usuariosBloqueados", 1);
        
        Map<String, Long> porRol = new HashMap<>();
        porRol.put("ADMIN", 1L);
        porRol.put("CLIENTE", 7L);
        stats.put("usuariosPorRol", porRol);
        
        when(authService.getUserStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(10))
                .andExpect(jsonPath("$.usuariosActivos").value(8))
                .andExpect(jsonPath("$.usuariosBloqueados").value(1))
                .andExpect(jsonPath("$.usuariosPorRol.ADMIN").value(1))
                .andExpect(jsonPath("$.usuariosPorRol.CLIENTE").value(7))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.users").exists());

        verify(authService).getUserStatistics();
    }

    // ===== PRUEBAS DE HEALTH CHECK =====

    @Test
    @DisplayName("GET /health - Estado del servicio")
    void testHealth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v3/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Authentication Service V3 (HATEOAS + CRUD)"))
                .andExpect(jsonPath("$.version").value("3.0.0"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.auth").exists())
                .andExpect(jsonPath("$._links.users").exists());
    }

    // ===== PRUEBAS DE CASOS EXTREMOS =====

    @Test
    @DisplayName("POST /login - Request malformado")
    void testLoginMalformedRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("POST /register - Request malformado")
    void testRegisterMalformedRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v3/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("PUT /users/{id} - ID inválido")
    void testUpdateUserInvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v3/auth/users/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).updateUser(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /users/{id} - ID inválido")
    void testDeleteUserInvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v3/auth/users/abc"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).deleteUser(anyLong());
    }

    // ===== PRUEBAS DE CORS =====

    @Test
    @DisplayName("OPTIONS /login - CORS preflight")
    void testCorsPreflightLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/v3/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPTIONS /users - CORS preflight")
    void testCorsPreflightUsers() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/v3/auth/users")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk());
    }

    // ===== MÉTODOS HELPER =====

    private AuthResponse.UserInfo createOtherUser() {
        AuthResponse.UserInfo user = new AuthResponse.UserInfo();
        user.setId(2L);
        user.setUsername("otheruser");
        user.setEmail("other@email.com");
        user.setNombre("Other");
        user.setApellido("User");
        user.setTelefono("+56987654321");
        user.setRol("EMPRENDEDOR");
        user.setActivo(true);
        return user;
    }
}