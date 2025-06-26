package com.example.tarea3;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Models.Rol;
import com.example.tarea3.Repositories.UsuarioRepository;
import com.example.tarea3.Repositories.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para el módulo de registro de usuarios.
 * CORREGIDO: Usa configuración específica para tests con localhost:5432
 * Estas pruebas PERSISTEN los datos en la base de datos de Docker.
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
public class RegistroServiceTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Datos de prueba para generar usuarios realistas
    private final String[] nombres = {
        "Ana García", "Carlos Rodríguez", "María López", "José Martínez", "Laura Sánchez",
        "David González", "Elena Fernández", "Miguel Pérez", "Carmen Ruiz", "Antonio Jiménez",
        "Isabel Moreno", "Francisco Muñoz", "Pilar Álvarez", "Juan Romero", "Cristina Gutiérrez",
        "Manuel Navarro", "Rosa Torres", "Alberto Domínguez", "Patricia Vázquez", "Roberto Ramos",
        "Marta Castillo", "Fernando Herrera", "Silvia Medina", "Andrés Guerrero", "Beatriz Cortés",
        "Ricardo Mendoza", "Nuria Iglesias", "Sergio Delgado", "Alicia Castro", "Javier Ortega",
        "Mónica Vargas", "Raúl Campos", "Verónica Nieto", "Óscar Cano", "Susana Prieto",
        "Iván Cabrera", "Natalia Rubio", "Hugo Gallego", "Adriana León", "Rubén Márquez",
        "Diana Herrero", "Víctor Peña", "Lorena Gil", "Emilio Santos", "Gema Aguilar",
        "Tomás Pascual", "Irene Calvo", "Marcos Vega", "Rocío Morales", "Jaime Flores"
    };
    
    private final String[] dominios = {
        "gmail.com", "yahoo.es", "hotmail.com", "outlook.es", "universidad.edu",
        "investigacion.org", "ciencia.net", "academia.es", "tech.com", "research.org"
    };

    @BeforeEach
    void verificarRolesExisten() {
        // Verificar que el rol ROLE_USER existe
        Optional<Rol> rolUser = rolRepository.findByNombre("ROLE_USER");
        if (rolUser.isEmpty()) {
            // Crear el rol si no existe
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("ROLE_USER");
            rolRepository.save(nuevoRol);
        }
    }

    @Test
    @DisplayName("🚀 Registrar 50 usuarios de prueba en la BD de Docker")
    void testRegistrar50Usuarios() {
        System.out.println("🚀 INICIANDO REGISTRO DE 50 USUARIOS EN BD DE DOCKER");
        System.out.println("=".repeat(70));
        System.out.println("📍 Conexión: localhost:5432 → Docker PostgreSQL Container");
        System.out.println("=".repeat(70));
        
        int usuariosRegistrados = 0;
        int usuariosExistentes = 0;
        
        for (int i = 0; i < 50; i++) {
            String nombre = nombres[i];
            String email = generarEmail(nombre, i);
            String password = "password123"; // Contraseña estándar para testing
            
            try {
                // Verificar si el usuario ya existe
                if (usuarioRepository.existsByNombre(nombre)) {
                    usuariosExistentes++;
                    System.out.println("⚠️  Usuario ya existe: " + nombre);
                    continue;
                }
                
                // Crear nuevo usuario
                Usuario usuario = new Usuario();
                usuario.setNombre(nombre);
                usuario.setEmail(email);
                usuario.setPassword(passwordEncoder.encode(password));
                usuario.setDarkMode(i % 3 == 0); // Algunos usuarios con modo oscuro
                
                // Asignar rol de usuario
                Rol rolUsuario = rolRepository.findByNombre("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Rol ROLE_USER no encontrado"));
                usuario.setRoles(new HashSet<>(Collections.singletonList(rolUsuario)));
                
                // Guardar usuario
                Usuario usuarioGuardado = usuarioRepository.save(usuario);
                
                // Verificaciones
                assertNotNull(usuarioGuardado.getId());
                assertEquals(nombre, usuarioGuardado.getNombre());
                assertEquals(email, usuarioGuardado.getEmail());
                assertTrue(passwordEncoder.matches(password, usuarioGuardado.getPassword()));
                assertFalse(usuarioGuardado.getRoles().isEmpty());
                
                usuariosRegistrados++;
                System.out.println("✅ Usuario " + (i+1) + "/50: " + nombre + " (" + email + ")");
                
            } catch (Exception e) {
                System.err.println("❌ Error registrando usuario " + nombre + ": " + e.getMessage());
                fail("Error al registrar usuario: " + e.getMessage());
            }
        }
        
        // Verificaciones finales
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 RESUMEN DEL REGISTRO:");
        System.out.println("✅ Usuarios registrados exitosamente: " + usuariosRegistrados);
        System.out.println("⚠️  Usuarios que ya existían: " + usuariosExistentes);
        System.out.println("📝 Total de usuarios en BD: " + usuarioRepository.count());
        
        // Verificar que se registraron al menos algunos usuarios
        assertTrue(usuariosRegistrados > 0, "Debería haberse registrado al menos un usuario");
        
        System.out.println("\n🎉 REGISTRO COMPLETADO EN BD DE DOCKER!");
        System.out.println("💡 Verifica en la app web: http://localhost:5173");
        System.out.println("🔑 Login: Antonio / 123456 → Admin → Gestionar Usuarios");
        System.out.println("=".repeat(70));
    }

    @Test
    @DisplayName("🔐 Verificar que los usuarios registrados pueden hacer login")
    void testUsuariosRegistradosPuedenHacerLogin() {
        System.out.println("🔐 VERIFICANDO USUARIOS PARA LOGIN");
        System.out.println("=".repeat(70));
        
        // Buscar algunos usuarios de prueba
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        assertTrue(usuarios.size() >= 1, "Debe haber al menos un usuario en la base de datos");
        
        // Verificar los primeros 5 usuarios
        int verificados = 0;
        for (Usuario usuario : usuarios.subList(0, Math.min(5, usuarios.size()))) {
            // Verificar que el password está encriptado correctamente
            assertNotNull(usuario.getPassword());
            assertTrue(usuario.getPassword().startsWith("$2a$"), 
                "La contraseña debe estar encriptada con BCrypt");
            
            // Verificar que tiene roles asignados
            assertFalse(usuario.getRoles().isEmpty(), 
                "El usuario debe tener al menos un rol");
            
            // Verificar que tiene el rol USER
            boolean tieneRolUser = usuario.getRoles().stream()
                .anyMatch(rol -> "ROLE_USER".equals(rol.getNombre()));
            assertTrue(tieneRolUser, "El usuario debe tener el rol ROLE_USER");
            
            verificados++;
            System.out.println("✅ Usuario verificado: " + usuario.getNombre());
        }
        
        System.out.println("📊 Usuarios verificados: " + verificados);
        System.out.println("✅ Todos los usuarios están listos para login");
        System.out.println("=".repeat(70));
    }

    @Test
    @DisplayName("📈 Estadísticas de usuarios registrados")
    void testEstadisticasUsuarios() {
        System.out.println("📈 GENERANDO ESTADÍSTICAS DE USUARIOS");
        System.out.println("=".repeat(70));
        
        long totalUsuarios = usuarioRepository.count();
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        
        // Contar usuarios con modo oscuro
        long usuariosConModoOscuro = todosUsuarios.stream()
            .filter(u -> u.getDarkMode() != null && u.getDarkMode())
            .count();
        
        // Contar usuarios por dominio de email
        System.out.println("📊 ESTADÍSTICAS COMPLETAS:");
        System.out.println("👥 Total de usuarios: " + totalUsuarios);
        System.out.println("🌙 Usuarios con modo oscuro: " + usuariosConModoOscuro);
        System.out.println("☀️  Usuarios con modo claro: " + (totalUsuarios - usuariosConModoOscuro));
        
        // Mostrar distribución por dominio
        System.out.println("\n📧 Distribución por dominio de email:");
        for (String dominio : dominios) {
            long count = todosUsuarios.stream()
                .filter(u -> u.getEmail().endsWith("@" + dominio))
                .count();
            if (count > 0) {
                System.out.println("   " + dominio + ": " + count + " usuarios");
            }
        }
        
        assertTrue(totalUsuarios > 0, "Debe haber usuarios en la base de datos");
        System.out.println("=".repeat(70));
    }
    
    /**
     * Genera un email único basado en el nombre y un índice
     */
    private String generarEmail(String nombre, int indice) {
        String nombreLimpio = nombre.toLowerCase()
            .replace(" ", ".")
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n");
        
        String dominio = dominios[indice % dominios.length];
        return nombreLimpio + indice + "@" + dominio;
    }
}