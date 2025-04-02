package com.example.tarea3.Services;

import com.example.tarea3.Models.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    private final ScopusService scopusService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ArticleService(ScopusService scopusService) {
        this.scopusService = scopusService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Busca artículos con una consulta simple
     */
    public List<Article> searchArticles(String query, int page, int size) {
        // Calcular el índice de inicio (la API de Scopus usa base 0)
        int startIndex = (page - 1) * size;
        
        // Realizar la búsqueda en Scopus
        String response = scopusService.searchArticles(query, startIndex, size);
        
        // Convertir la respuesta a objetos Article
        return parseArticlesFromResponse(response);
    }
    
    /**
     * Busca artículos con parámetros avanzados
     */
    public List<Article> searchArticlesAdvanced(String query, int page, int size, 
                                               String sortField, String sortDirection, 
                                               String dateRange) {
        int startIndex = (page - 1) * size;
        String response = scopusService.searchArticlesAdvanced(query, startIndex, size, 
                                                           sortField, sortDirection, 
                                                           dateRange, "COMPLETE");
        return parseArticlesFromResponse(response);
    }
    
    /**
     * Obtiene el número total de resultados de la última búsqueda
     */
    public int getTotalResults(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("search-results").path("opensearch:totalResults").asInt();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Parsea la respuesta JSON de la API de Scopus a una lista de objetos Article
     */
    private List<Article> parseArticlesFromResponse(String response) {
        List<Article> articles = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode entries = root.path("search-results").path("entry");
            
            for (JsonNode entry : entries) {
                Article article = new Article();
                
                // Extraer datos básicos
                article.setId(entry.path("dc:identifier").asText());
                article.setTitle(entry.path("dc:title").asText());
                article.setPublicationName(entry.path("prism:publicationName").asText());
                article.setPublicationDate(entry.path("prism:coverDate").asText());
                article.setDoi(entry.path("prism:doi").asText());
                article.setUrl(entry.path("prism:url").asText());
                article.setCitedByCount(entry.path("citedby-count").asInt());
                
                // Extraer autores
                List<String> authors = new ArrayList<>();
                String creator = entry.path("dc:creator").asText();
                if (!creator.isEmpty()) {
                    authors.add(creator);
                }
                article.setAuthors(authors);
                
                // Acceso abierto
                article.setOpenAccess(entry.path("openaccessFlag").asText());
                
                articles.add(article);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return articles;
    }
}