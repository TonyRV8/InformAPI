package com.example.tarea3.Controllers.REST;

import com.example.tarea3.Models.Article;
import com.example.tarea3.Services.RecomendacionService;
import com.example.tarea3.Services.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionesRestController {
    
    @Autowired
    private RecomendacionService recomendacionService;
    
    @Autowired
    private FavoritoService favoritoService;
    
    /**
     * Obtener recomendaciones para el usuario autenticado
     */
    @GetMapping
    public ResponseEntity<?> obtenerRecomendaciones(
            Authentication authentication,
            @RequestParam(value = "limite", defaultValue = "15") int limite) {
        
        try {
            if (authentication == null) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "No autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String nombreUsuario = authentication.getName();
            
            // Verificar si el usuario tiene favoritos
            long totalFavoritos = favoritoService.contarFavoritos(nombreUsuario);
            
            if (totalFavoritos == 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "El usuario no tiene favoritos. Agregue artículos a favoritos para generar recomendaciones.");
                response.put("recomendaciones", List.of());
                response.put("totalFavoritos", 0);
                response.put("totalRecomendaciones", 0);
                return ResponseEntity.ok(response);
            }
            
            // Generar recomendaciones
            List<Article> recomendaciones = recomendacionService.generarRecomendaciones(nombreUsuario, limite);
            
            // Obtener estadísticas
            Map<String, Object> estadisticas = recomendacionService.obtenerEstadisticasRecomendaciones(nombreUsuario);
            
            // Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("recomendaciones", recomendaciones);
            response.put("totalRecomendaciones", recomendaciones.size());
            response.put("totalFavoritos", totalFavoritos);
            response.put("usuario", nombreUsuario);
            response.put("estadisticas", estadisticas);
            response.put("limite", limite);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Error al generar recomendaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener estadísticas de recomendaciones (palabras clave, etc.)
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(Authentication authentication) {
        try {
            if (authentication == null) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "No autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String nombreUsuario = authentication.getName();
            Map<String, Object> estadisticas = recomendacionService.obtenerEstadisticasRecomendaciones(nombreUsuario);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Generar recomendaciones con parámetros personalizados
     */
    @PostMapping("/generar")
    public ResponseEntity<?> generarRecomendacionesPersonalizadas(
            Authentication authentication,
            @RequestBody RecomendacionRequest request) {
        
        try {
            if (authentication == null) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "No autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String nombreUsuario = authentication.getName();
            int limite = request.getLimite() != null ? request.getLimite() : 15;
            
            // Validar límite
            if (limite < 1 || limite > 50) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "El límite debe estar entre 1 y 50");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generar recomendaciones
            List<Article> recomendaciones = recomendacionService.generarRecomendaciones(nombreUsuario, limite);
            
            Map<String, Object> response = new HashMap<>();
            response.put("recomendaciones", recomendaciones);
            response.put("total", recomendaciones.size());
            response.put("limite", limite);
            response.put("usuario", nombreUsuario);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Error al generar recomendaciones personalizadas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Verificar si el usuario puede obtener recomendaciones
     */
    @GetMapping("/disponibles")
    public ResponseEntity<?> verificarDisponibilidad(Authentication authentication) {
        try {
            if (authentication == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("disponibles", false);
                response.put("razon", "No autenticado");
                return ResponseEntity.ok(response);
            }
            
            String nombreUsuario = authentication.getName();
            long totalFavoritos = favoritoService.contarFavoritos(nombreUsuario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("disponibles", totalFavoritos > 0);
            response.put("totalFavoritos", totalFavoritos);
            response.put("usuario", nombreUsuario);
            response.put("razon", totalFavoritos > 0 ? 
                "Recomendaciones disponibles" : 
                "Necesita agregar artículos a favoritos");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("disponibles", false);
            response.put("razon", "Error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Clase para solicitudes de recomendaciones personalizadas
     */
    public static class RecomendacionRequest {
        private Integer limite;
        private List<String> palabrasClaveAdicionales;
        private String filtroFecha;
        
        // Getters y setters
        public Integer getLimite() {
            return limite;
        }
        
        public void setLimite(Integer limite) {
            this.limite = limite;
        }
        
        public List<String> getPalabrasClaveAdicionales() {
            return palabrasClaveAdicionales;
        }
        
        public void setPalabrasClaveAdicionales(List<String> palabrasClaveAdicionales) {
            this.palabrasClaveAdicionales = palabrasClaveAdicionales;
        }
        
        public String getFiltroFecha() {
            return filtroFecha;
        }
        
        public void setFiltroFecha(String filtroFecha) {
            this.filtroFecha = filtroFecha;
        }
    }
}