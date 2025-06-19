package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.model.ServicioReparacion;
import com.reparafacilspa.reparaciones.repository.ServicioReparacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Tests del Servicio de Reparación")
class ServicioReparacionServiceTest {

    @Mock
    private ServicioReparacionRepository servicioRepository;

    @InjectMocks
    private ServicioReparacionService servicioService;

    private ServicioReparacion servicioTest;
    private ServicioReparacionRequest requestTest;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        servicioTest = new ServicioReparacion();
        servicioTest.setId(1L);
        servicioTest.setNombreCliente("Juan Pérez");
        servicioTest.setTelefono("+56912345678");
        servicioTest.setEmail("juan.perez@example.com");
        servicioTest.setTipoDispositivo("Smartphone");
        servicioTest.setMarca("Samsung");
        servicioTest.setModelo("Galaxy S21");
        servicioTest.setDescripcionProblema("Pantalla rota");
        servicioTest.setFechaAgendada(new Date());
        servicioTest.setFechaCreacion(new Date());
        servicioTest.setEstado(ServicioReparacion.EstadoReparacion.AGENDADO);
        servicioTest.setPrioridad(ServicioReparacion.PrioridadReparacion.NORMAL);
        servicioTest.setActivo(true);

        requestTest = new ServicioReparacionRequest();
        requestTest.setNombreCliente("Juan Pérez");
        requestTest.setTelefono("+56912345678");
        requestTest.setEmail("juan.perez@example.com");
        requestTest.setTipoDispositivo("Smartphone");
        requestTest.setMarca("Samsung");
        requestTest.setModelo("Galaxy S21");
        requestTest.setDescripcionProblema("Pantalla rota");
        requestTest.setFechaAgendada(new Date());
    }

    @Test
    @DisplayName("Debe crear un nuevo servicio de reparación exitosamente")
    void testCrearServicioExitoso() {
        // Given
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(servicioTest);

        // When
        ServicioReparacionResponse response = servicioService.save(requestTest);

        // Then
        assertNotNull(response);
        assertEquals("Juan Pérez", response.getNombreCliente());
        assertEquals("Smartphone", response.getTipoDispositivo());
        assertEquals("AGENDADO", response.getEstado());
        
        verify(servicioRepository, times(1)).save(any(ServicioReparacion.class));
    }

    @Test
    @DisplayName("Debe encontrar servicio por ID exitosamente")
    void testFindByIdExitoso() {
        // Given
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioTest));

        // When
        ServicioReparacionResponse response = servicioService.findById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Juan Pérez", response.getNombreCliente());
        
        verify(servicioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra servicio por ID")
    void testFindByIdNoEncontrado() {
        // Given
        when(servicioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioService.findById(999L);
        });
        
        assertTrue(exception.getMessage().contains("Servicio no encontrado"));
        verify(servicioRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe obtener todos los servicios activos")
    void testFindAllServiciosActivos() {
        // Given
        List<ServicioReparacion> servicios = Arrays.asList(servicioTest);
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);

        // When
        List<ServicioReparacionResponse> responses = servicioService.findAll();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Juan Pérez", responses.get(0).getNombreCliente());
        
        verify(servicioRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Debe cambiar estado del servicio exitosamente")
    void testCambiarEstadoExitoso() {
        // Given
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioTest));
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(servicioTest);

        // When
        ServicioReparacionResponse response = servicioService.cambiarEstado(1L, "EN_REPARACION");

        // Then
        assertNotNull(response);
        verify(servicioRepository, times(1)).findById(1L);
        verify(servicioRepository, times(1)).save(any(ServicioReparacion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción con estado inválido")
    void testCambiarEstadoInvalido() {
        // Given
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioTest));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioService.cambiarEstado(1L, "ESTADO_INEXISTENTE");
        });
        
        assertTrue(exception.getMessage().contains("Estado inválido"));
        verify(servicioRepository, times(1)).findById(1L);
        verify(servicioRepository, never()).save(any(ServicioReparacion.class));
    }

    @Test
    @DisplayName("Debe buscar servicios por email")
    void testFindByEmail() {
        // Given
        List<ServicioReparacion> servicios = Arrays.asList(servicioTest);
        when(servicioRepository.findByEmailAndActivoTrue("juan.perez@example.com"))
                .thenReturn(servicios);

        // When
        List<ServicioReparacionResponse> responses = servicioService.findByEmail("juan.perez@example.com");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("juan.perez@example.com", responses.get(0).getEmail());
        
        verify(servicioRepository, times(1))
                .findByEmailAndActivoTrue("juan.perez@example.com");
    }
}