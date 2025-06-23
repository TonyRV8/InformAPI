package com.example.tarea3.Controllers;

import com.example.tarea3.Models.Favorito;
import com.example.tarea3.Services.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/favoritos")
public class FavoritoController {
    
    @Autowired
    private FavoritoService favoritoService;
    
    /**
     * Muestra la página de favoritos del usuario
     */
    @GetMapping("")
    public String mostrarFavoritos(Authentication authentication, Model model) {
        try {
            String nombreUsuario = authentication.getName();
            List<Favorito> favoritos = favoritoService.obtenerFavoritosDeUsuario(nombreUsuario);
            long totalFavoritos = favoritoService.contarFavoritos(nombreUsuario);
            
            model.addAttribute("favoritos", favoritos);
            model.addAttribute("totalFavoritos", totalFavoritos);
            model.addAttribute("nombreUsuario", nombreUsuario);
            
            return "favoritos";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al cargar los favoritos: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Alterna el estado de favorito de un artículo (AJAX y formulario)
     */
    @PostMapping("/toggle")
    public String toggleFavorito(
            Authentication authentication,
            @RequestParam String articleId,
            @RequestParam String title,
            @RequestParam(required = false) String authors,
            @RequestParam(required = false) String publicationName,
            @RequestParam(required = false) String publicationDate,
            @RequestParam(required = false) String doi,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String abstractText,
            @RequestParam(required = false, defaultValue = "0") Integer citedByCount,
            @RequestParam(required = false) String openAccess,
            @RequestParam(required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {
        
        try {
            String nombreUsuario = authentication.getName();
            
            boolean resultado = favoritoService.toggleFavorito(
                    nombreUsuario, articleId, title, authors, publicationName, 
                    publicationDate, doi, url, abstractText, citedByCount, openAccess
            );
            
            if (resultado) {
                boolean esFavorito = favoritoService.esFavorito(nombreUsuario, articleId);
                if (esFavorito) {
                    redirectAttributes.addFlashAttribute("success", "Artículo agregado a favoritos");
                } else {
                    redirectAttributes.addFlashAttribute("success", "Artículo eliminado de favoritos");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al procesar el favorito");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        
        // Redirigir a la URL especificada o a la página de favoritos por defecto
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/favoritos";
    }
    
    /**
     * Elimina un artículo específico de favoritos
     */
    @PostMapping("/eliminar/{articleId}")
    public String eliminarFavorito(
            Authentication authentication,
            @PathVariable String articleId,
            RedirectAttributes redirectAttributes) {
        
        try {
            String nombreUsuario = authentication.getName();
            boolean resultado = favoritoService.eliminarDeFavoritos(nombreUsuario, articleId);
            
            if (resultado) {
                redirectAttributes.addFlashAttribute("success", "Artículo eliminado de favoritos");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar el artículo de favoritos");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        
        return "redirect:/favoritos";
    }
    
    /**
     * API REST para verificar si un artículo está en favoritos
     */
    @GetMapping("/check/{articleId}")
    @ResponseBody
    public String verificarFavorito(Authentication authentication, @PathVariable String articleId) {
        try {
            String nombreUsuario = authentication.getName();
            boolean esFavorito = favoritoService.esFavorito(nombreUsuario, articleId);
            return "{\"esFavorito\": " + esFavorito + "}";
        } catch (Exception e) {
            return "{\"esFavorito\": false, \"error\": \"" + e.getMessage() + "\"}";
        }
    }
}