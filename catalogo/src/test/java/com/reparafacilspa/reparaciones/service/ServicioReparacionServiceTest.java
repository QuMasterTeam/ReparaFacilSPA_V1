package com.reparafacilspa.reparaciones.service;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.model.ServicioReparacion;
import com.reparafacilspa.reparaciones.repository.ServicioReparacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioReparacionServiceV3 - Pruebas unitarias")
class ServicioReparacionServiceTest {

    @Mock
    private ServicioReparacionRepository servicioRepository;

    @InjectMocks
    private ServicioReparacionServiceV3 servicioService;

    private ServicioReparacion testServicio;
    private ServicioReparacionRequest servicioRequest;

    @BeforeEach
    void setUp() {
        // Servicio de prueba
        testServicio = new ServicioReparacion();
        testServicio.setId(1L);
        testServicio.setNombreCliente("Juan Pérez");
        testServicio.setTelefono("+56912345678");
        testServicio.setEmail("juan@email.com");
        testServicio.setTipoDispositivo("Smartphone");
        testServicio.setMarca("Samsung");
        testServicio.setModelo("Galaxy S21");
        testServicio.setDescripcionProblema("Pantalla rota");
        testServicio.setFechaAgendada(new Date());
        testServicio.setFechaCreacion(new Date());
        testServicio.setEstado(ServicioReparacion.EstadoReparacion.AGENDADO);
        testServicio.setPrioridad(ServicioReparacion.PrioridadReparacion.NORMAL);
        testServicio.setActivo(true);
        testServicio.setTecnicoAsignado("Carlos González");
        testServicio.setCostoEstimado(new BigDecimal("50000"));
        testServicio.setGarantiaDias(30);

        // Request de prueba
        servicioRequest = new ServicioReparacionRequest();
        servicioRequest.setNombreCliente("María López");
        servicioRequest.setTelefono("+56987654321");
        servicioRequest.setEmail("maria@email.com");
        servicioRequest.setTipoDispositivo("Laptop");
        servicioRequest.setMarca("HP");
        servicioRequest.setModelo("Pavilion");
        servicioRequest.setDescripcionProblema("No enciende");
        servicioRequest.setFechaAgendada(new Date());
    }

    // ===== PRUEBAS CRUD BÁSICO =====

    @Test
    @DisplayName("findAll retorna lista de servicios activos")
    void testFindAll() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio, createOtroServicio());
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Juan Pérez", result.get(0).getNombreCliente());
        
        verify(servicioRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("save crea nuevo servicio exitosamente")
    void testSave() {
        // Arrange
        when(servicioRepository.save(any(ServicioReparacion.class))).thenAnswer(invocation -> {
            ServicioReparacion servicio = invocation.getArgument(0);
            servicio.setId(2L);
            return servicio;
        });

        // Act
        ServicioReparacionResponse result = servicioService.save(servicioRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("María López", result.getNombreCliente());
        assertEquals("Laptop", result.getTipoDispositivo());
        assertEquals("AGENDADO", result.getEstado());
        assertTrue(result.getActivo());
        
        verify(servicioRepository).save(argThat(servicio ->
            servicio.getNombreCliente().equals("María López") &&
            servicio.getEstado() == ServicioReparacion.EstadoReparacion.AGENDADO &&
            servicio.getActivo()
        ));
    }

    @Test
    @DisplayName("findById retorna servicio existente")
    void testFindByIdSuccess() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));

        // Act
        ServicioReparacionResponse result = servicioService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Juan Pérez", result.getNombreCliente());
        assertEquals("Samsung", result.getMarca());
        
        verify(servicioRepository).findById(1L);
    }

    @Test
    @DisplayName("findById lanza excepción cuando servicio no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(servicioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.findById(999L));
        assertEquals("Servicio no encontrado con ID: 999", exception.getMessage());
        
        verify(servicioRepository).findById(999L);
    }

    @Test
    @DisplayName("update actualiza servicio exitosamente")
    void testUpdate() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(testServicio);

        // Act
        ServicioReparacionResponse result = servicioService.update(1L, servicioRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        verify(servicioRepository).findById(1L);
        verify(servicioRepository).save(argThat(servicio ->
            servicio.getNombreCliente().equals("María López") &&
            servicio.getTipoDispositivo().equals("Laptop")
        ));
    }

    @Test
    @DisplayName("delete realiza soft delete del servicio")
    void testDelete() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(testServicio);

        // Act
        servicioService.delete(1L);

        // Assert
        verify(servicioRepository).findById(1L);
        verify(servicioRepository).save(argThat(servicio -> !servicio.getActivo()));
    }

    // ===== PRUEBAS DE BÚSQUEDA =====

    @Test
    @DisplayName("findByEmail retorna servicios del cliente")
    void testFindByEmail() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByEmailAndActivoTrue("juan@email.com")).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByEmail("juan@email.com");

        // Assert
        assertEquals(1, result.size());
        assertEquals("juan@email.com", result.get(0).getEmail());
        
        verify(servicioRepository).findByEmailAndActivoTrue("juan@email.com");
    }

    @Test
    @DisplayName("findByEstado retorna servicios del estado especificado")
    void testFindByEstado() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion.AGENDADO))
            .thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByEstado("AGENDADO");

        // Assert
        assertEquals(1, result.size());
        assertEquals("AGENDADO", result.get(0).getEstado());
        
        verify(servicioRepository).findByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion.AGENDADO);
    }

    @Test
    @DisplayName("findByEstado retorna lista vacía con estado inválido")
    void testFindByEstadoInvalid() {
        // Act
        List<ServicioReparacionResponse> result = servicioService.findByEstado("ESTADO_INVALIDO");

        // Assert
        assertTrue(result.isEmpty());
        
        verify(servicioRepository, never()).findByEstadoAndActivoTrue(any());
    }

    @Test
    @DisplayName("findByTipoDispositivo retorna servicios del tipo especificado")
    void testFindByTipoDispositivo() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByTipoDispositivoAndActivoTrue("Smartphone")).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByTipoDispositivo("Smartphone");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Smartphone", result.get(0).getTipoDispositivo());
        
        verify(servicioRepository).findByTipoDispositivoAndActivoTrue("Smartphone");
    }

    @Test
    @DisplayName("search encuentra servicios por término de búsqueda")
    void testSearch() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.search("Samsung");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Samsung", result.get(0).getMarca());
        
        verify(servicioRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("search no encuentra servicios con término no coincidente")
    void testSearchNoMatches() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.search("iPhone");

        // Assert
        assertTrue(result.isEmpty());
        
        verify(servicioRepository).findByActivoTrue();
    }

    // ===== PRUEBAS DE CAMBIO DE ESTADO =====

    @Test
    @DisplayName("cambiarEstado actualiza estado exitosamente")
    void testCambiarEstado() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(testServicio);

        // Act
        ServicioReparacionResponse result = servicioService.cambiarEstado(1L, "EN_REPARACION");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        verify(servicioRepository).findById(1L);
        verify(servicioRepository).save(argThat(servicio ->
            servicio.getEstado() == ServicioReparacion.EstadoReparacion.EN_REPARACION &&
            servicio.getFechaInicioReparacion() != null
        ));
    }

    @Test
    @DisplayName("cambiarEstado a COMPLETADO establece fecha fin")
    void testCambiarEstadoCompletado() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(testServicio);

        // Act
        ServicioReparacionResponse result = servicioService.cambiarEstado(1L, "COMPLETADO");

        // Assert
        assertNotNull(result);
        
        verify(servicioRepository).save(argThat(servicio ->
            servicio.getEstado() == ServicioReparacion.EstadoReparacion.COMPLETADO &&
            servicio.getFechaFinReparacion() != null
        ));
    }

    @Test
    @DisplayName("cambiarEstado lanza excepción con estado inválido")
    void testCambiarEstadoInvalid() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.cambiarEstado(1L, "ESTADO_INVALIDO"));
        assertEquals("Estado inválido: ESTADO_INVALIDO", exception.getMessage());
        
        verify(servicioRepository).findById(1L);
        verify(servicioRepository, never()).save(any());
    }

    // ===== PRUEBAS DE ESTADÍSTICAS =====

    @Test
    @DisplayName("getEstadisticas retorna estadísticas completas")
    void testGetEstadisticas() {
        // Arrange
        ServicioReparacion servicio2 = createOtroServicio();
        servicio2.setEstado(ServicioReparacion.EstadoReparacion.EN_REPARACION);
        servicio2.setTipoDispositivo("Laptop");
        servicio2.setTecnicoAsignado("Ana García");

        List<ServicioReparacion> servicios = Arrays.asList(testServicio, servicio2);
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);
        when(servicioRepository.countByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion.AGENDADO))
            .thenReturn(1L);
        when(servicioRepository.countByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion.EN_REPARACION))
            .thenReturn(1L);
        when(servicioRepository.countByEstadoAndActivoTrue(ServicioReparacion.EstadoReparacion.COMPLETADO))
            .thenReturn(0L);

        // Act
        Map<String, Object> stats = servicioService.getEstadisticas();

        // Assert
        assertEquals(2, stats.get("totalServicios"));
        assertEquals(1L, stats.get("serviciosAgendados"));
        assertEquals(1L, stats.get("serviciosEnReparacion"));
        assertEquals(0L, stats.get("serviciosCompletados"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> porTipo = (Map<String, Long>) stats.get("serviciosPorTipo");
        assertEquals(1L, porTipo.get("Smartphone"));
        assertEquals(1L, porTipo.get("Laptop"));
        
        assertEquals(2, stats.get("totalTecnicos"));
        
        verify(servicioRepository).findByActivoTrue();
    }

    // ===== PRUEBAS MÉTODOS PERSONALIZADOS =====

    @Test
    @DisplayName("findByTecnicoAsignado retorna servicios del técnico")
    void testFindByTecnicoAsignado() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByTecnicoAsignadoAndActivoTrue("Carlos González")).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByTecnicoAsignado("Carlos González");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Carlos González", result.get(0).getTecnicoAsignado());
        
        verify(servicioRepository).findByTecnicoAsignadoAndActivoTrue("Carlos González");
    }

    @Test
    @DisplayName("findByFechaAgendada retorna servicios de la fecha")
    void testFindByFechaAgendada() {
        // Arrange
        Date fecha = new Date();
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByFechaAgendadaAndActivoTrue(any(Date.class))).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByFechaAgendada("2024-01-20");

        // Assert
        assertEquals(1, result.size());
        
        verify(servicioRepository).findByFechaAgendadaAndActivoTrue(any(Date.class));
    }

    @Test
    @DisplayName("findByFechaAgendada lanza excepción con formato inválido")
    void testFindByFechaAgendadaInvalidFormat() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.findByFechaAgendada("fecha-invalida"));
        assertEquals("Formato de fecha inválido. Use yyyy-MM-dd", exception.getMessage());
    }

    @Test
    @DisplayName("countByEmail retorna cantidad de servicios del cliente")
    void testCountByEmail() {
        // Arrange
        when(servicioRepository.countByEmailAndActivoTrue("juan@email.com")).thenReturn(3L);

        // Act
        long count = servicioService.countByEmail("juan@email.com");

        // Assert
        assertEquals(3L, count);
        
        verify(servicioRepository).countByEmailAndActivoTrue("juan@email.com");
    }

    @Test
    @DisplayName("findByEmailAndFechaAgendada retorna servicios filtrados")
    void testFindByEmailAndFechaAgendada() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByEmailAndFechaAgendadaAndActivoTrue(eq("juan@email.com"), any(Date.class)))
            .thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByEmailAndFechaAgendada("juan@email.com", "2024-01-20");

        // Assert
        assertEquals(1, result.size());
        assertEquals("juan@email.com", result.get(0).getEmail());
        
        verify(servicioRepository).findByEmailAndFechaAgendadaAndActivoTrue(eq("juan@email.com"), any(Date.class));
    }

    @Test
    @DisplayName("findByTecnicoAsignadoAndEstado retorna servicios filtrados")
    void testFindByTecnicoAsignadoAndEstado() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByTecnicoAsignadoAndEstadoAndActivoTrue(
            "Carlos González", ServicioReparacion.EstadoReparacion.AGENDADO))
            .thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByTecnicoAsignadoAndEstado("Carlos González", "AGENDADO");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Carlos González", result.get(0).getTecnicoAsignado());
        assertEquals("AGENDADO", result.get(0).getEstado());
        
        verify(servicioRepository).findByTecnicoAsignadoAndEstadoAndActivoTrue(
            "Carlos González", ServicioReparacion.EstadoReparacion.AGENDADO);
    }

    @Test
    @DisplayName("findByEmailAndFechaAgendadaBetween retorna servicios en rango de fechas")
    void testFindByEmailAndFechaAgendadaBetween() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByEmailAndFechaAgendadaBetweenAndActivoTrue(
            eq("juan@email.com"), any(Date.class), any(Date.class)))
            .thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByEmailAndFechaAgendadaBetween(
            "juan@email.com", "2024-01-01", "2024-01-31");

        // Assert
        assertEquals(1, result.size());
        assertEquals("juan@email.com", result.get(0).getEmail());
        
        verify(servicioRepository).findByEmailAndFechaAgendadaBetweenAndActivoTrue(
            eq("juan@email.com"), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("findByTecnicoAsignadoAndFechaAgendadaBetween retorna servicios en rango")
    void testFindByTecnicoAsignadoAndFechaAgendadaBetween() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByTecnicoAsignadoAndFechaAgendadaBetweenAndActivoTrue(
            eq("Carlos González"), any(Date.class), any(Date.class)))
            .thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findByTecnicoAsignadoAndFechaAgendadaBetween(
            "Carlos González", "2024-01-01", "2024-01-31");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Carlos González", result.get(0).getTecnicoAsignado());
        
        verify(servicioRepository).findByTecnicoAsignadoAndFechaAgendadaBetweenAndActivoTrue(
            eq("Carlos González"), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("countByTecnicoAsignado retorna cantidad de servicios del técnico")
    void testCountByTecnicoAsignado() {
        // Arrange
        when(servicioRepository.countByTecnicoAsignadoAndActivoTrue("Carlos González")).thenReturn(5L);

        // Act
        long count = servicioService.countByTecnicoAsignado("Carlos González");

        // Assert
        assertEquals(5L, count);
        
        verify(servicioRepository).countByTecnicoAsignadoAndActivoTrue("Carlos González");
    }

    // ===== PRUEBAS MÉTODOS ADICIONALES V3 =====

    @Test
    @DisplayName("findDeletedServicios retorna servicios eliminados")
    void testFindDeletedServicios() {
        // Arrange
        ServicioReparacion servicioEliminado = createOtroServicio();
        servicioEliminado.setActivo(false);
        
        List<ServicioReparacion> todosServicios = Arrays.asList(testServicio, servicioEliminado);
        when(servicioRepository.findAll()).thenReturn(todosServicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.findDeletedServicios();

        // Assert
        assertEquals(1, result.size());
        assertFalse(result.get(0).getActivo());
        
        verify(servicioRepository).findAll();
    }

    @Test
    @DisplayName("restoreServicio restaura servicio eliminado")
    void testRestoreServicio() {
        // Arrange
        testServicio.setActivo(false);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(testServicio));
        when(servicioRepository.save(any(ServicioReparacion.class))).thenReturn(testServicio);

        // Act
        ServicioReparacionResponse result = servicioService.restoreServicio(1L);

        // Assert
        assertNotNull(result);
        
        verify(servicioRepository).findById(1L);
        verify(servicioRepository).save(argThat(servicio -> servicio.getActivo()));
    }

    @Test
    @DisplayName("searchAdvanced filtra por múltiples criterios")
    void testSearchAdvanced() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.searchAdvanced(
            "Juan", "juan@email.com", "Smartphone", "AGENDADO", "Carlos"
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Juan Pérez", result.get(0).getNombreCliente());
        
        verify(servicioRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("searchAdvanced no encuentra servicios que no coinciden")
    void testSearchAdvancedNoMatches() {
        // Arrange
        List<ServicioReparacion> servicios = Arrays.asList(testServicio);
        when(servicioRepository.findByActivoTrue()).thenReturn(servicios);

        // Act
        List<ServicioReparacionResponse> result = servicioService.searchAdvanced(
            "Pedro", null, null, null, null
        );

        // Assert
        assertTrue(result.isEmpty());
        
        verify(servicioRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("getDetailedStatistics retorna estadísticas detalladas")
    void testGetDetailedStatistics() {
        // Arrange
        ServicioReparacion servicioEliminado = createOtroServicio();
        servicioEliminado.setActivo(false);
        servicioEliminado.setEstado(ServicioReparacion.EstadoReparacion.CANCELADO);
        
        ServicioReparacion servicioCompletado = createOtroServicio();
        servicioCompletado.setId(3L);
        servicioCompletado.setEstado(ServicioReparacion.EstadoReparacion.COMPLETADO);
        servicioCompletado.setPrioridad(ServicioReparacion.PrioridadReparacion.ALTA);
        servicioCompletado.setTecnicoAsignado("Ana García");

        List<ServicioReparacion> todosServicios = Arrays.asList(testServicio, servicioEliminado, servicioCompletado);
        List<ServicioReparacion> serviciosActivos = Arrays.asList(testServicio, servicioCompletado);
        
        when(servicioRepository.findAll()).thenReturn(todosServicios);
        when(servicioRepository.findByActivoTrue()).thenReturn(serviciosActivos);

        // Act
        Map<String, Object> stats = servicioService.getDetailedStatistics();

        // Assert
        assertEquals(3, stats.get("totalServicios"));
        assertEquals(2, stats.get("serviciosActivos"));
        assertEquals(1, stats.get("serviciosEliminados"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> porEstado = (Map<String, Long>) stats.get("serviciosPorEstado");
        assertEquals(1L, porEstado.get("AGENDADO"));
        assertEquals(1L, porEstado.get("COMPLETADO"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> porTipo = (Map<String, Long>) stats.get("serviciosPorTipo");
        assertEquals(2L, porTipo.get("Smartphone"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> porPrioridad = (Map<String, Long>) stats.get("serviciosPorPrioridad");
        assertEquals(1L, porPrioridad.get("NORMAL"));
        assertEquals(1L, porPrioridad.get("ALTA"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> porTecnico = (Map<String, Long>) stats.get("serviciosPorTecnico");
        assertEquals(1L, porTecnico.get("Carlos González"));
        assertEquals(1L, porTecnico.get("Ana García"));
        
        verify(servicioRepository).findAll();
        verify(servicioRepository).findByActivoTrue();
    }

    // ===== PRUEBAS DE CASOS EXTREMOS =====

    @Test
    @DisplayName("update lanza excepción cuando servicio no existe")
    void testUpdateNotFound() {
        // Arrange
        when(servicioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.update(999L, servicioRequest));
        assertEquals("Servicio no encontrado con ID: 999", exception.getMessage());
        
        verify(servicioRepository).findById(999L);
        verify(servicioRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete lanza excepción cuando servicio no existe")
    void testDeleteNotFound() {
        // Arrange
        when(servicioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.delete(999L));
        assertEquals("Servicio no encontrado con ID: 999", exception.getMessage());
        
        verify(servicioRepository).findById(999L);
        verify(servicioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstado lanza excepción cuando servicio no existe")
    void testCambiarEstadoNotFound() {
        // Arrange
        when(servicioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.cambiarEstado(999L, "EN_REPARACION"));
        assertEquals("Servicio no encontrado con ID: 999", exception.getMessage());
        
        verify(servicioRepository).findById(999L);
        verify(servicioRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByFechaAgendadaBetween lanza excepción con fechas inválidas")
    void testFindByEmailAndFechaAgendadaBetweenInvalidDate() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> servicioService.findByEmailAndFechaAgendadaBetween("email", "fecha-invalida", "2024-01-31"));
        assertEquals("Formato de fecha inválido. Use yyyy-MM-dd", exception.getMessage());
    }

    // ===== MÉTODOS HELPER =====

    private ServicioReparacion createOtroServicio() {
        ServicioReparacion servicio = new ServicioReparacion();
        servicio.setId(2L);
        servicio.setNombreCliente("Ana García");
        servicio.setTelefono("+56987654321");
        servicio.setEmail("ana@email.com");
        servicio.setTipoDispositivo("Smartphone");
        servicio.setMarca("iPhone");
        servicio.setModelo("iPhone 13");
        servicio.setDescripcionProblema("Batería no carga");
        servicio.setFechaAgendada(new Date());
        servicio.setFechaCreacion(new Date());
        servicio.setEstado(ServicioReparacion.EstadoReparacion.AGENDADO);
        servicio.setPrioridad(ServicioReparacion.PrioridadReparacion.NORMAL);
        servicio.setActivo(true);
        servicio.setTecnicoAsignado("María Torres");
        servicio.setCostoEstimado(new BigDecimal("75000"));
        servicio.setGarantiaDias(30);
        return servicio;
    }
}