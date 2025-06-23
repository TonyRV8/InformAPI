package com.example.tarea3.Controllers;

import com.example.tarea3.Models.Article;
import com.example.tarea3.Services.RecomendacionService;
import com.example.tarea3.Services.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recomendaciones")
public class RecomendacionController {
    
    @Autowired
    private RecomendacionService recomendacionService;
    
    @Autowired
    private FavoritoService favoritoService;
    
    /**
     * Muestra la página de recomendaciones
     */
    @GetMapping("")
    public String mostrarRecomendaciones(
            Authentication authentication,
            @RequestParam(value = "limite", defaultValue = "15") int limite,
            Model model) {
        
        try {
            String nombreUsuario = authentication.getName();
            
            // Verificar si el usuario tiene favoritos
            long totalFavoritos = favoritoService.contarFavoritos(nombreUsuario);
            
            if (totalFavoritos == 0) {
                model.addAttribute("sinFavoritos", true);
                model.addAttribute("nombreUsuario", nombreUsuario);
                return "recomendaciones";
            }
            
            // Generar recomendaciones
            List<Article> recomendaciones = recomendacionService.generarRecomendaciones(nombreUsuario, limite);
            
            // Obtener estadísticas para mostrar al usuario
            Map<String, Object> estadisticas = recomendacionService.obtenerEstadisticasRecomendaciones(nombreUsuario);
            
            // Agregar datos al modelo
            model.addAttribute("recomendaciones", recomendaciones);
            model.addAttribute("totalRecomendaciones", recomendaciones.size());
            model.addAttribute("totalFavoritos", totalFavoritos);
            model.addAttribute("nombreUsuario", nombreUsuario);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("limite", limite);
            
            return "recomendaciones";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al generar recomendaciones: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Regenera las recomendaciones (útil para refrescar)
     */
    @PostMapping("/regenerar")
    public String regenerarRecomendaciones(
            Authentication authentication,
            @RequestParam(value = "limite", defaultValue = "15") int limite) {
        
        return "redirect:/recomendaciones?limite=" + limite + "&refresh=true";
    }
    
    /**
     * Endpoint AJAX para obtener más recomendaciones
     */
    @GetMapping("/mas")
    @ResponseBody
    public Map<String, Object> obtenerMasRecomendaciones(
            Authentication authentication,
            @RequestParam(value = "limite", defaultValue = "10") int limite,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {
        
        try {
            String nombreUsuario = authentication.getName();
            List<Article> recomendaciones = recomendacionService.generarRecomendaciones(nombreUsuario, limite + offset);
            
            // Tomar solo las recomendaciones del offset en adelante
            List<Article> nuevasRecomendaciones = recomendaciones.stream()
                    .skip(offset)
                    .limit(limite)
                    .toList();
            
            return Map.of(
                "recomendaciones", nuevasRecomendaciones,
                "total", nuevasRecomendaciones.size(),
                "hasMore", recomendaciones.size() > (offset + limite)
            );
            
        } catch (Exception e) {
            return Map.of(
                "error", "Error al obtener más recomendaciones: " + e.getMessage(),
                "recomendaciones", List.of(),
                "total", 0,
                "hasMore", false
            );
        }
    }
}