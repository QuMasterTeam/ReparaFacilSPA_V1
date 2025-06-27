package com.reparafacilspa.reparaciones.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@TestConfiguration
@ComponentScan(
    basePackages = "com.reparafacilspa.reparaciones",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.auth\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*AuthController.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*AuthService.*")
    }
)
public class TestConfig {
    // Esta configuración excluye los componentes de autenticación para tests
}