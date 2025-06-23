package com.example.tarea3.Services;

import com.example.tarea3.Models.Favorito;
import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Repositories.FavoritoRepository;
import com.example.tarea3.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoritoService {
    
    @Autowired
    private FavoritoRepository favoritoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Obtiene todos los favoritos de un usuario
     */
    public List<Favorito> obtenerFavoritosDeUsuario(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return favoritoRepository.findByUsuarioIdOrderByFechaAgregadoDesc(usuario.getId());
    }
    
    /**
     * Verifica si un artículo está en favoritos de un usuario
     */
    public boolean esFavorito(String nombreUsuario, String articleId) {
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return favoritoRepository.existsByUsuarioIdAndArticleId(usuario.getId(), articleId);
    }
    
    /**
     * Agrega un artículo a favoritos
     */
    @Transactional
    public boolean agregarAFavoritos(String nombreUsuario, String articleId, String title, 
                                   String authors, String publicationName, String publicationDate, 
                                   String doi, String url, String abstractText, 
                                   Integer citedByCount, String openAccess) {
        try {
            Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Verificar si ya existe en favoritos
            if (favoritoRepository.existsByUsuarioIdAndArticleId(usuario.getId(), articleId)) {
                return false; // Ya existe en favoritos
            }
            
            // Crear nuevo favorito
            Favorito favorito = new Favorito(
                    usuario.getId(),
                    articleId,
                    title,
                    authors,
                    publicationName,
                    publicationDate,
                    doi,
                    url,
                    abstractText,
                    citedByCount,
                    openAccess
            );
            
            favoritoRepository.save(favorito);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Elimina un artículo de favoritos
     */
    @Transactional
    public boolean eliminarDeFavoritos(String nombreUsuario, String articleId) {
        try {
            Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            favoritoRepository.deleteByUsuarioIdAndArticleId(usuario.getId(), articleId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Alterna el estado de favorito de un artículo (agregar/quitar)
     */
    @Transactional
    public boolean toggleFavorito(String nombreUsuario, String articleId, String title, 
                                String authors, String publicationName, String publicationDate, 
                                String doi, String url, String abstractText, 
                                Integer citedByCount, String openAccess) {
        
        if (esFavorito(nombreUsuario, articleId)) {
            return eliminarDeFavoritos(nombreUsuario, articleId);
        } else {
            return agregarAFavoritos(nombreUsuario, articleId, title, authors, 
                                   publicationName, publicationDate, doi, url, 
                                   abstractText, citedByCount, openAccess);
        }
    }
    
    /**
     * Cuenta el número de favoritos de un usuario
     */
    public long contarFavoritos(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return favoritoRepository.countByUsuarioId(usuario.getId());
    }
    
    /**
     * Obtiene un favorito específico
     */
    public Optional<Favorito> obtenerFavorito(String nombreUsuario, String articleId) {
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return favoritoRepository.findByUsuarioIdAndArticleId(usuario.getId(), articleId);
    }
}