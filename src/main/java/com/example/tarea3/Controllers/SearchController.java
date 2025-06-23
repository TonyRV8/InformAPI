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
        
        try {
            // Realizar la búsqueda
            List<Article> articles = articleService.searchArticles(query, page, size);
            
            // Agregar resultados al modelo
            model.addAttribute("articles", articles);
            model.addAttribute("query", query);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            
            return "search_results";
        } catch (Exception e) {
            System.err.println("Error en búsqueda: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al realizar la búsqueda: " + e.getMessage());
            return "error";
        }
    }
}