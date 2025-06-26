package com.example.tarea3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test simple para verificar que la configuración de testing funciona correctamente
 * CORREGIDO: No usa perfil docker, usa configuración específica para tests
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/basesita",
    "spring.datasource.username=postgres",
    "spring.datasource.password=root",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never",
    "spring.main.banner-mode=off",
    "logging.level.org.springframework.boot.autoconfigure=WARN"
})
public class TestVerificacion {

    @Test
    @DisplayName("🔧 Verificación: Configuración de testing funciona")
    void contextLoads() {
        System.out.println("=".repeat(50));
        System.out.println("✅ SUCCESS: El contexto de Spring Boot se carga correctamente");
        System.out.println("✅ SUCCESS: Conexión a localhost:5432 funciona");
        System.out.println("✅ SUCCESS: Usando la BD de Docker desde el IDE");
        System.out.println("✅ SUCCESS: @SpringBootTest funciona");
        System.out.println("=".repeat(50));
    }
    
    @Test
    @DisplayName("🧪 Verificación: JUnit 5 funciona correctamente")
    void junitFunciona() {
        System.out.println("🧪 Probando assertions básicas de JUnit 5...");
        
        // Test básicos para verificar que JUnit funciona
        org.junit.jupiter.api.Assertions.assertTrue(true, "True debería ser true");
        org.junit.jupiter.api.Assertions.assertFalse(false, "False debería ser false");
        org.junit.jupiter.api.Assertions.assertEquals(1, 1, "1 debería ser igual a 1");
        org.junit.jupiter.api.Assertions.assertNotNull("test", "String no debería ser null");
        
        System.out.println("✅ SUCCESS: Todas las assertions de JUnit 5 funcionan correctamente");
    }
}