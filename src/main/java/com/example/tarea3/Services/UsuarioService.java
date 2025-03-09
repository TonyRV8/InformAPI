package com.example.tarea3.Services;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener todos los usuarios
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // Obtener usuario por ID
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);  // Devuelve null si no existe
    }
    
    // Obtener usuario por nombre
    public Usuario obtenerPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre).orElse(null);
    }

    // Guardar o actualizar usuario
    public void guardar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    // Eliminar usuario por ID
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}