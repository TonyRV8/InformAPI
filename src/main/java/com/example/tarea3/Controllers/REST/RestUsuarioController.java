package com.example.tarea3.Controllers.REST;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Services.UsuarioService;
import com.example.tarea3.Repositories.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class RestUsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RestUsuarioController(UsuarioService usuarioService, 
                                UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Obtener todos los usuarios (solo admin)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    // Obtener usuario por ID (solo admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario == null) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(usuario);
    }
// Actualizar usuario (solo admin)
@PutMapping("/{id}")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public ResponseEntity<?> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetails) {
    Usuario usuario = usuarioService.obtenerPorId(id);
    if (usuario == null) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario no encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    usuario.setNombre(usuarioDetails.getNombre());
    usuario.setEmail(usuarioDetails.getEmail());
    
    if (usuarioDetails.getPassword() != null && !usuarioDetails.getPassword().isEmpty()) {
        usuario.setPassword(passwordEncoder.encode(usuarioDetails.getPassword()));
    }
    
    usuarioService.guardar(usuario);
    
    Map<String, String> response = new HashMap<>();
    response.put("mensaje", "Usuario actualizado correctamente");
    return ResponseEntity.ok(response);
}

    // Eliminar usuario (solo admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario == null) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        usuarioService.eliminar(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    // Obtener información del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String username = authentication.getName();
        Usuario usuario = usuarioService.obtenerPorNombre(username);
        
        if (usuario == null) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(usuario);
    }
    
    // Actualizar perfil del usuario actual
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UsuarioUpdateRequest updateRequest) {
        if (authentication == null) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String username = authentication.getName();
        Usuario usuario = usuarioService.obtenerPorNombre(username);
        
        if (usuario == null) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        // Actualizar datos
        if (updateRequest.getNombre() != null) {
            usuario.setNombre(updateRequest.getNombre());
        }
        
        if (updateRequest.getEmail() != null) {
            usuario.setEmail(updateRequest.getEmail());
        }
        
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        
        usuarioService.guardar(usuario);
        
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Perfil actualizado correctamente");
        return ResponseEntity.ok(response);
    }
    
    // Clase para solicitudes de actualización
    public static class UsuarioUpdateRequest {
        private String nombre;
        private String email;
        private String password;
        
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
    }
}