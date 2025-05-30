package com.reparafacilspa.reparaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reparafacilspa.reparaciones.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Buscar por username
    Optional<User> findByUsername(String username);
    
    // Buscar por email
    Optional<User> findByEmail(String email);
    
    // Buscar por username o email
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Verificar si existe un username
    boolean existsByUsername(String username);
    
    // Verificar si existe un email
    boolean existsByEmail(String email);
    
    // Buscar usuarios activos
    Optional<User> findByUsernameAndActivoTrue(String username);
    
    // Buscar por username y que no esté bloqueado
    Optional<User> findByUsernameAndActivoTrueAndCuentaBloqueadaFalse(String username);
}