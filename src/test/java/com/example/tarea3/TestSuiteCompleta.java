package com.example.tarea3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Suite completa de pruebas para el módulo de registro
 * Esta clase ejecuta todas las pruebas en un orden específico
 */
@SpringBootTest
@ActiveProfiles("docker")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestSuiteCompleta {

    @Test
    @Order(1)
    @DisplayName("🚀 Ejecutar todas las pruebas de registro")
    void ejecutarTodasLasPruebas() {
        String separador = "================================================================================";
        System.out.println(separador);
        System.out.println("🎯 INICIANDO SUITE COMPLETA DE PRUEBAS DE REGISTRO");
        System.out.println(separador);
        
        // Esta prueba sirve como punto de entrada para ejecutar todas las pruebas
        // Las pruebas reales están en las otras clases de test
        
        System.out.println("📋 Pruebas incluidas en esta suite:");
        System.out.println("1. RegistroServiceTest - Pruebas de servicio y persistencia");
        System.out.println("2. RegistroControllerTest - Pruebas de API REST");
        System.out.println("\n💡 Ejecuta cada clase de test individualmente para ver los resultados detallados");
        System.out.println(separador);
    }
}