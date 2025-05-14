package com.example.tarea3.Controllers;

import com.example.tarea3.Models.Article;
import com.example.tarea3.Services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final ArticleService articleService;

    @Autowired
    public SearchController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * Muestra la página de búsqueda
     */
    @GetMapping("")
    public String showSearchPage() {
        return "search";
    }

    /**
     * Realiza una búsqueda simple y muestra los resultados
     */
    @GetMapping("/results")
    public String searchArticles(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        if (query == null || query.isEmpty()) {
            return "search";
        }
        
        // Si hay consulta, hacer la búsqueda
        List<Article> articles = articleService.searchArticles(query, page, size);
        
        // Agregar resultados al modelo
        model.addAttribute("articles", articles);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "search_results";
    }

    /**
     * Realiza una búsqueda avanzada con filtros adicionales
     */
    @GetMapping("/advanced")
    public String showAdvancedSearch() {
        return "search_advanced";
    }
    
    /**
     * Procesa la búsqueda avanzada
     */
    @GetMapping("/advanced/results")
public String searchAdvanced(
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "sortField", required = false) String sortField,
        @RequestParam(value = "sortDirection", required = false) String sortDirection,
        @RequestParam(value = "dateRange", required = false) String dateRange,
        Model model) {
    
    try {
        if (query == null || query.isEmpty()) {
            return "search_advanced";
        }
        
        // Realizar búsqueda avanzada
        List<Article> articles = articleService.searchArticlesAdvanced(
                query, page, size, sortField, sortDirection, dateRange);
        
        // Agregar resultados al modelo
        model.addAttribute("articles", articles);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField != null ? sortField : "");
        model.addAttribute("sortDirection", sortDirection != null ? sortDirection : "");
        model.addAttribute("dateRange", dateRange != null ? dateRange : "");
        
        return "search_results";
    } catch (Exception e) {
        e.printStackTrace();
        // En caso de error, redirigir a la página de búsqueda avanzada con un mensaje de error
        model.addAttribute("errorMessage", "Ha ocurrido un error al procesar la búsqueda: " + e.getMessage());
        return "search_advanced";
    }
    }
}