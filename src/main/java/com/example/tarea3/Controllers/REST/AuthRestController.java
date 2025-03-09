package com.example.tarea3.Controllers.Rest;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Models.Rol;
import com.example.tarea3.Repositories.UsuarioRepository;
import com.example.tarea3.Repositories.RolRepository;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Clase para solicitud de login
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters y setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Clase para solicitud de registro
    public static class SignupRequest {
        private String nombre;
        private String email;
        private String password;
        private String confirmPassword;

        // Getters y setters
        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    // Registrar un nuevo usuario
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        Map<String, String> response = new HashMap<>();
        
        // Verificar si el nombre ya existe
        if (usuarioRepository.existsByNombre(signupRequest.getNombre())) {
            response.put("mensaje", "El nombre de usuario ya está en uso");
            return ResponseEntity.badRequest().body(response);
        }

        // Verificar si las contraseñas coinciden
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            response.put("mensaje", "Las contraseñas no coinciden");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Crear nuevo usuario
            Usuario usuario = new Usuario();
            usuario.setNombre(signupRequest.getNombre());
            usuario.setEmail(signupRequest.getEmail());
            usuario.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            
            // Asignar rol de usuario por defecto
            Rol rolUsuario = rolRepository.findByNombre("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
            usuario.setRoles(new HashSet<>(Collections.singletonList(rolUsuario)));
            
            // Guardar usuario
            usuarioRepository.save(usuario);
            
            response.put("mensaje", "Usuario registrado correctamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("mensaje", "Error al registrar el usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Verificar estado de autenticación
    @PostMapping("/status")
    public ResponseEntity<?> checkAuthStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("autenticado", true);
            response.put("username", authentication.getName());
            response.put("roles", authentication.getAuthorities());
            return ResponseEntity.ok(response);
        } else {
            response.put("autenticado", false);
            return ResponseEntity.ok(response);
        }
    }
}