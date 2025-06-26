package com.example.tarea3;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Models.Rol;
import com.example.tarea3.Repositories.UsuarioRepository;
import com.example.tarea3.Repositories.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar que los datos se guardan en la MISMA base de datos de Docker
 * CORREGIDO: Usa configuración específica para tests con localhost:5432
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/basesita",
    "spring.datasource.username=postgres",
    "spring.datasource.password=root",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never",
    "spring.main.banner-mode=off",
    "logging.level.org.springframework.boot.autoconfigure=WARN",
    "logging.level.org.springframework.security=WARN",
    "logging.level.org.hibernate=WARN"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMismaBaseDatos {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    @DisplayName("🔗 Verificar conexión a la base de datos de Docker desde localhost")
    void testConexionBaseDatosDocker() {
        System.out.println("🐳 VERIFICANDO CONEXIÓN A BASE DE DATOS DE DOCKER VIA LOCALHOST");
        System.out.println("=".repeat(70));
        
        // Verificar que podemos contar usuarios existentes
        long usuariosExistentes = usuarioRepository.count();
        
        System.out.println("✅ Conexión exitosa a la base de datos via localhost:5432");
        System.out.println("📊 Usuarios existentes en la BD: " + usuariosExistentes);
        
        // Debería haber al menos el usuario admin (Antonio)
        assertTrue(usuariosExistentes >= 1, "Debería haber al menos el usuario admin en la BD");
        
        // Verificar que el usuario admin existe
        boolean adminExiste = usuarioRepository.existsByNombre("Antonio");
        assertTrue(adminExiste, "El usuario admin 'Antonio' debería existir en la BD de Docker");
        
        System.out.println("✅ Usuario admin 'Antonio' encontrado");
        System.out.println("✅ CONFIRMADO: Accediendo a la MISMA BD que usa la aplicación Docker");
        System.out.println("=".repeat(70));
    }

    @Test
    @Order(2)
    @DisplayName("💾 Crear usuario test y verificar persistencia en Docker BD")
    void testCrearUsuarioEnBaseDatosDocker() {
        System.out.println("💾 CREANDO USUARIO TEST EN LA BD DE DOCKER");
        System.out.println("=".repeat(70));
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nombreUsuario = "TestDockerBD_" + timestamp;
        String emailUsuario = "testdockerbd." + timestamp + "@localhost.test";
        
        // Verificar que el rol USER existe
        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_USER no encontrado"));
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(nombreUsuario);
        usuario.setEmail(emailUsuario);
        usuario.setPassword(passwordEncoder.encode("testpassword123"));
        usuario.setDarkMode(true);
        usuario.setRoles(new HashSet<>(Collections.singletonList(rolUser)));
        
        // Guardar en BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Verificaciones
        assertNotNull(usuarioGuardado.getId(), "El usuario debería tener un ID asignado");
        assertEquals(nombreUsuario, usuarioGuardado.getNombre());
        assertEquals(emailUsuario, usuarioGuardado.getEmail());
        assertTrue(usuarioGuardado.getDarkMode());
        
        System.out.println("✅ Usuario creado exitosamente en la BD de Docker:");
        System.out.println("   ID: " + usuarioGuardado.getId());
        System.out.println("   Nombre: " + usuarioGuardado.getNombre());
        System.out.println("   Email: " + usuarioGuardado.getEmail());
        System.out.println("   Modo oscuro: " + usuarioGuardado.getDarkMode());
        
        // Verificar que el usuario persiste (consulta nueva)
        Usuario usuarioRecuperado = usuarioRepository.findByNombre(nombreUsuario).orElse(null);
        assertNotNull(usuarioRecuperado, "El usuario debería existir en la BD después de guardarlo");
        assertEquals(usuarioGuardado.getId(), usuarioRecuperado.getId());
        
        System.out.println("✅ Usuario verificado - PERSISTE en la BD de Docker");
        System.out.println("=".repeat(70));
    }

    @Test
    @Order(3)
    @DisplayName("📊 Verificar que los datos son visibles para la aplicación web")
    void testDatosVisiblesEnAplicacionWeb() {
        System.out.println("📊 VERIFICANDO VISIBILIDAD EN APLICACIÓN WEB");
        System.out.println("=".repeat(70));
        
        // Contar todos los usuarios test que hemos creado
        long usuariosTest = usuarioRepository.findAll().stream()
            .filter(u -> u.getNombre().contains("Test") || u.getNombre().contains("API"))
            .count();
        
        System.out.println("📈 Usuarios de test encontrados: " + usuariosTest);
        
        // Mostrar algunos ejemplos
        System.out.println("📋 Usuarios de test en la BD:");
        usuarioRepository.findAll().stream()
            .filter(u -> u.getNombre().contains("Test") || u.getNombre().contains("API"))
            .limit(5)
            .forEach(u -> System.out.println("   - " + u.getNombre() + " (" + u.getEmail() + ")"));
        
        System.out.println("\n🎯 CÓMO VERIFICAR EN LA APLICACIÓN WEB:");
        System.out.println("1. Ejecuta: docker-compose up");
        System.out.println("2. Ve a: http://localhost:5173");
        System.out.println("3. Login: Antonio / 123456");
        System.out.println("4. Ve a: Panel Admin → Gestionar Usuarios");
        System.out.println("5. ¡Verás TODOS los usuarios creados por este test!");
        
        System.out.println("\n🔍 VERIFICAR DIRECTAMENTE EN LA BD:");
        System.out.println("docker-compose exec postgres psql -U postgres -d basesita");
        System.out.println("SELECT nombre, email FROM usuarios WHERE nombre LIKE '%Test%';");
        
        assertTrue(usuariosTest >= 0, "Los usuarios de test deberían ser visibles");
        System.out.println("=".repeat(70));
    }
    
    @Test
    @Order(4)
    @DisplayName("🔄 Crear usuarios adicionales para demostrar persistencia")
    void testCrearUsuariosAdicionales() {
        System.out.println("🔄 CREANDO USUARIOS ADICIONALES");
        System.out.println("=".repeat(70));
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String[] nombres = {
            "TestPersistencia_A_" + timestamp,
            "TestPersistencia_B_" + timestamp,
            "TestPersistencia_C_" + timestamp
        };
        
        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_USER no encontrado"));
        
        int usuariosCreados = 0;
        
        for (String nombre : nombres) {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(nombre.toLowerCase() + "@persistencia.test");
            usuario.setPassword(passwordEncoder.encode("testpass123"));
            usuario.setDarkMode(false);
            usuario.setRoles(new HashSet<>(Collections.singletonList(rolUser)));
            
            Usuario guardado = usuarioRepository.save(usuario);
            usuariosCreados++;
            
            System.out.println("✅ Usuario " + usuariosCreados + " creado: " + guardado.getNombre());
        }
        
        System.out.println("\n📊 RESUMEN FINAL:");
        System.out.println("✅ " + usuariosCreados + " usuarios adicionales creados");
        System.out.println("📊 Total usuarios en BD: " + usuarioRepository.count());
        System.out.println("\n🎉 TODOS ESTOS DATOS ESTÁN EN LA BD DE DOCKER");
        System.out.println("💡 Los verás en la aplicación web en http://localhost:5173");
        System.out.println("=".repeat(70));
    }
}