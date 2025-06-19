package com.reparafacilspa.catalogo;

import com.reparafacilspa.reparaciones.ReparaFacilApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ReparaFacilApplication.class)
@ActiveProfiles("test")
class CatalogoApplicationTests {

    @Test
    void contextLoads() {
        // Test básico para verificar que el contexto de Spring se carga correctamente
    }
}