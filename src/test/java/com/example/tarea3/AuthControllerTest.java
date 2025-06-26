package com.example.tarea3;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setupClass() {
        System.out.println("=== INICIANDO PRUEBAS DE AuthController ===");
    }

    @Test
    @Order(1)
    @DisplayName("Root (/) debe redirigir a /login")
    void testRootRedirectsToLogin() throws Exception {
        System.out.println("Probando redirección de root a login...");
        
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        
        System.out.println("✓ Root redirige correctamente a /login");
    }

    @Test
    @Order(2)
    @DisplayName("GET /login debe mostrar la página de login")
    void testShowLoginPage() throws Exception {
        System.out.println("Probando carga de página de login...");
        
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
        
        System.out.println("✓ Página de login carga correctamente");
    }

    @Test
    @Order(3)
    @DisplayName("GET /registro debe mostrar la página de registro")
    void testShowRegistroPage() throws Exception {
        System.out.println("Probando carga de página de registro...");
        
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"));
        
        System.out.println("✓ Página de registro carga correctamente");
    }

    @Test
    @Order(4)
    @DisplayName("GET /usuarios/registro debe mostrar la página de registro (alias)")
    void testShowRegistroPageAlias() throws Exception {
        System.out.println("Probando alias de página de registro...");
        
        mockMvc.perform(get("/usuarios/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"));
        
        System.out.println("✓ Alias de registro funciona correctamente");
    }

    @Test
    @Order(5)
    @DisplayName("GET /dashboard debe mostrar la página de dashboard")
    void testShowDashboardPage() throws Exception {
        System.out.println("Probando carga de página de dashboard...");
        
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
        
        System.out.println("✓ Página de dashboard carga correctamente");
    }

    @Test
    @Order(6)
    @DisplayName("Verificar que todas las rutas públicas son accesibles")
    void testPublicRoutesAccessible() throws Exception {
        System.out.println("Verificando accesibilidad de rutas públicas...");
        
        String[] publicRoutes = {"/login", "/registro", "/usuarios/registro", "/dashboard"};
        
        for (String route : publicRoutes) {
            mockMvc.perform(get(route))
                    .andExpect(status().isOk())
                    .andExpect(view().name(getExpectedViewName(route)));
            
            System.out.println("✓ Ruta pública accesible: " + route);
        }
        
        System.out.println("✓ Todas las rutas públicas son accesibles");
    }

    @Test
    @Order(7)
    @DisplayName("Verificar manejo de rutas inexistentes")
    void testNonExistentRoutes() throws Exception {
        System.out.println("Probando manejo de rutas inexistentes...");
        
        mockMvc.perform(get("/ruta-inexistente"))
                .andExpect(status().isNotFound());
        
        System.out.println("✓ Rutas inexistentes manejadas correctamente");
    }

    @Test
    @Order(8)
    @DisplayName("Verificar headers de respuesta")
    void testResponseHeaders() throws Exception {
        System.out.println("Verificando headers de respuesta...");
        
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"));
        
        System.out.println("✓ Headers de respuesta correctos");
    }

    @AfterAll
    static void tearDownClass() {
        System.out.println("=== PRUEBAS DE AuthController COMPLETADAS ===");
        System.out.println("📊 Resumen:");
        System.out.println("├── Redirección de root: ✓");
        System.out.println("├── Página de login: ✓");
        System.out.println("├── Página de registro: ✓");
        System.out.println("├── Alias de registro: ✓");
        System.out.println("├── Página de dashboard: ✓");
        System.out.println("├── Rutas públicas: ✓");
        System.out.println("├── Rutas inexistentes: ✓");
        System.out.println("└── Headers de respuesta: ✓");
    }

    // Método auxiliar para obtener el nombre de vista esperado
    private String getExpectedViewName(String route) {
        switch (route) {
            case "/login":
                return "login";
            case "/registro":
            case "/usuarios/registro":
                return "registro";
            case "/dashboard":
                return "dashboard";
            default:
                return "unknown";
        }
    }
}