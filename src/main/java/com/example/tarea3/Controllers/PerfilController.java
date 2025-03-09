package com.example.tarea3.Controllers;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Repositories.UsuarioRepository;
import com.example.tarea3.Services.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public PerfilController(UsuarioRepository usuarioRepository, 
                           PasswordEncoder passwordEncoder,
                           UserDetailsServiceImpl userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/perfil/editar")
    public String mostrarFormularioEdicion(Authentication authentication, Model model) {
        // Obtener usuario usando el ID en lugar del nombre
        String currentUsername = authentication.getName();
        Usuario usuario = usuarioRepository.findByNombre(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "editar_perfil";
    }

    @PostMapping("/perfil/editar")
    public String actualizarPerfil(
            Authentication authentication,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam(required = false) String password) {

        // Obtener usuario actual
        String currentUsername = authentication.getName();
        Usuario usuario = usuarioRepository.findByNombre(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Guardar el nombre antiguo para verificar si ha cambiado
        String nombreAnterior = usuario.getNombre();
        boolean nombreCambiado = !nombreAnterior.equals(nombre);

        // Actualizar datos del usuario
        usuario.setNombre(nombre);
        usuario.setEmail(email);

        // Si el usuario ingresó una nueva contraseña, encriptarla y actualizarla
        if (password != null && !password.isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        // Guardar los cambios
        usuarioRepository.save(usuario);

        // Si el nombre ha cambiado, forzar un nuevo login
        if (nombreCambiado) {
            // Crear un nuevo objeto UserDetails con la información actualizada
            UserDetails userDetails = userDetailsService.loadUserByUsername(nombre);
            
            // Crear una nueva autenticación
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            // Actualizar el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        return "redirect:/home?success=true";
    }
}