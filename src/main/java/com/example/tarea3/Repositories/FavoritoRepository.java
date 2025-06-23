package com.example.tarea3.Repositories;

import com.example.tarea3.Models.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    
    /**
     * Buscar todos los favoritos de un usuario específico
     */
    List<Favorito> findByUsuarioIdOrderByFechaAgregadoDesc(Long usuarioId);
    
    /**
     * Verificar si un artículo ya está en favoritos de un usuario
     */
    boolean existsByUsuarioIdAndArticleId(Long usuarioId, String articleId);
    
    /**
     * Buscar un favorito específico por usuario y artículo
     */
    Optional<Favorito> findByUsuarioIdAndArticleId(Long usuarioId, String articleId);
    
    /**
     * Eliminar un favorito específico por usuario y artículo
     */
    void deleteByUsuarioIdAndArticleId(Long usuarioId, String articleId);
    
    /**
     * Contar el número de favoritos de un usuario
     */
    long countByUsuarioId(Long usuarioId);
}