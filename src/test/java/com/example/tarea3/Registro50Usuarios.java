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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
public class Registro50Usuarios {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
        Optional<Rol> rolUser = rolRepository.findByNombre("ROLE_USER");
        if (rolUser.isEmpty()) {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("ROLE_USER");
            rolRepository.save(nuevoRol);
        }
    }

    @Test
    @DisplayName("🚀 Registrar 50 usuarios normales")
    void testRegistrar50UsuariosNormales() {
        System.out.println("🚀 REGISTRANDO 50 USUARIOS NORMALES");
        System.out.println("=".repeat(50));
        
        int usuariosRegistrados = 0;
        int usuariosExistentes = 0;
        
        for (int i = 0; i < 50; i++) {
            String nombre = nombres[i];
            String email = generarEmail(nombre, i);
            String password = "password123";
            
            try {
                if (usuarioRepository.existsByNombre(nombre)) {
                    usuariosExistentes++;
                    System.out.println("⚠️  Usuario ya existe: " + nombre);
                    continue;
                }
                
                Usuario usuario = new Usuario();
                usuario.setNombre(nombre);
                usuario.setEmail(email);
                usuario.setPassword(passwordEncoder.encode(password));
                usuario.setDarkMode(i % 3 == 0);
                
                Rol rolUsuario = rolRepository.findByNombre("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Rol ROLE_USER no encontrado"));
                usuario.setRoles(new HashSet<>(Collections.singletonList(rolUsuario)));
                
                Usuario usuarioGuardado = usuarioRepository.save(usuario);
                
                assertNotNull(usuarioGuardado.getId());
                assertEquals(nombre, usuarioGuardado.getNombre());
                assertEquals(email, usuarioGuardado.getEmail());
                
                usuariosRegistrados++;
                System.out.println("✅ Usuario " + (i+1) + "/50: " + nombre + " (" + email + ")");
                
            } catch (Exception e) {
                System.err.println("❌ Error registrando usuario " + nombre + ": " + e.getMessage());
            }
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESUMEN:");
        System.out.println("✅ Usuarios registrados: " + usuariosRegistrados);
        System.out.println("⚠️  Usuarios existentes: " + usuariosExistentes);
        System.out.println("📝 Total usuarios en BD: " + usuarioRepository.count());
        System.out.println("🎉 ¡REGISTRO COMPLETADO!");
        System.out.println("=".repeat(50));
    }
    
    private String generarEmail(String nombre, int indice) {
        String nombreLimpio = nombre.toLowerCase()
            .replace(" ", ".")
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n");
        
        String dominio = dominios[indice % dominios.length];
        return nombreLimpio + indice + "@" + dominio;
    }
}