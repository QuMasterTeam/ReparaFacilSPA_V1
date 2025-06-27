package com.reparafacilspa.reparaciones.integration;

import com.reparafacilspa.reparaciones.dto.ServicioReparacionRequest;
import com.reparafacilspa.reparaciones.dto.ServicioReparacionResponse;
import com.reparafacilspa.reparaciones.repository.ServicioReparacionRepository;
import com.reparafacilspa.reparaciones.service.ServicioReparacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración Simple - Servicios de Reparación")
class ServicioReparacionSimpleIntegrationTest {

    @Autowired
    private ServicioReparacionService servicioService;

    @Autowired
    private ServicioReparacionRepository servicioRepository;

    private ServicioReparacionRequest servicioRequest;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos de test
        servicioRepository.deleteAll();

        // Configurar datos de prueba
        servicioRequest = new ServicioReparacionRequest();
        servicioRequest.setNombreCliente("María González");
        servicioRequest.setTelefono("+56987654321");
        servicioRequest.setEmail("maria.gonzalez@example.com");
        servicioRequest.setTipoDispositivo("Laptop");
        servicioRequest.setMarca("HP");
        servicioRequest.setModelo("Pavilion 15");
        servicioRequest.setDescripcionProblema("No enciende");
        servicioRequest.setFechaAgendada(new Date());
    }

    @Test
    @DisplayName("Debe crear servicio de reparación exitosamente")
    void testCrearServicioIntegracion() {
        // When
        ServicioReparacionResponse response = servicioService.save(servicioRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("María González", response.getNombreCliente());
        assertEquals("Laptop", response.getTipoDispositivo());
        assertEquals("AGENDADO", response.getEstado());
        
        // Verificar en base de datos
        assertTrue(servicioRepository.existsById(response.getId()));
    }

    @Test
    @DisplayName("Debe encontrar servicio por ID")
    void testFindByIdIntegracion() {
        // Given - Crear servicio primero
        ServicioReparacionResponse servicioCreado = servicioService.save(servicioRequest);

        // When
        ServicioReparacionResponse servicioEncontrado = servicioService.findById(servicioCreado.getId());

        // Then
        assertNotNull(servicioEncontrado);
        assertEquals(servicioCreado.getId(), servicioEncontrado.getId());
        assertEquals("María González", servicioEncontrado.getNombreCliente());
        assertEquals("maria.gonzalez@example.com", servicioEncontrado.getEmail());
    }

    @Test
    @DisplayName("Debe obtener lista de servicios activos")
    void testFindAllIntegracion() {
        // Given - Crear algunos servicios
        servicioService.save(servicioRequest);
        
        // Crear segundo servicio
        ServicioReparacionRequest segundoRequest = new ServicioReparacionRequest();
        segundoRequest.setNombreCliente("Juan Pérez");
        segundoRequest.setTelefono("+56912345678");
        segundoRequest.setEmail("juan.perez@example.com");
        segundoRequest.setTipoDispositivo("Smartphone");
        segundoRequest.setMarca("Samsung");
        segundoRequest.setModelo("Galaxy S21");
        segundoRequest.setDescripcionProblema("Pantalla rota");
        segundoRequest.setFechaAgendada(new Date());
        servicioService.save(segundoRequest);

        // When
        List<ServicioReparacionResponse> servicios = servicioService.findAll();

        // Then
        assertNotNull(servicios);
        assertEquals(2, servicios.size());
    }

    @Test
    @DisplayName("Debe buscar servicios por email")
    void testFindByEmailIntegracion() {
        // Given
        servicioService.save(servicioRequest);

        // When
        List<ServicioReparacionResponse> servicios = servicioService.findByEmail("maria.gonzalez@example.com");

        // Then
        assertNotNull(servicios);
        assertEquals(1, servicios.size());
        assertEquals("maria.gonzalez@example.com", servicios.get(0).getEmail());
    }

    @Test
    @DisplayName("Debe cambiar estado del servicio")
    void testCambiarEstadoIntegracion() {
        // Given - Crear servicio
        ServicioReparacionResponse servicioCreado = servicioService.save(servicioRequest);

        // When
        ServicioReparacionResponse servicioActualizado = servicioService.cambiarEstado(
            servicioCreado.getId(), "EN_REPARACION");

        // Then
        assertNotNull(servicioActualizado);
        assertEquals("EN_REPARACION", servicioActualizado.getEstado());
        assertNotNull(servicioActualizado.getFechaInicioReparacion());
    }

    @Test
    @DisplayName("Debe lanzar excepción para servicio inexistente")
    void testServicioInexistente() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            servicioService.findById(999L);
        });
    }

    @Test
    @DisplayName("Debe obtener estadísticas del sistema")
    void testEstadisticasIntegracion() {
        // Given - Crear algunos servicios
        servicioService.save(servicioRequest);

        // When
        var estadisticas = servicioService.getEstadisticas();

        // Then
        assertNotNull(estadisticas);
        assertEquals(1, estadisticas.get("totalServicios"));
        assertEquals(1L, estadisticas.get("serviciosAgendados"));
    }
}