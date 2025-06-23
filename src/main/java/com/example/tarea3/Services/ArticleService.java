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
            
            // Verificar si entries es nulo o está vacío
            if (entries == null || entries.isEmpty() || entries.size() == 0) {
                System.out.println("No se encontraron resultados en la respuesta de la API");
                return articles;
            }
            
            System.out.println("Número de artículos encontrados: " + entries.size());
            
            for (JsonNode entry : entries) {
                Article article = new Article();
                
                // Extraer datos básicos con verificación de nulos
                article.setId(getTextOrEmpty(entry, "dc:identifier"));
                article.setTitle(getTextOrEmpty(entry, "dc:title"));
                article.setPublicationName(getTextOrEmpty(entry, "prism:publicationName"));
                article.setPublicationDate(getTextOrEmpty(entry, "prism:coverDate"));
                article.setDoi(getTextOrEmpty(entry, "prism:doi"));
                
                // URL: utiliza DOI para generarla si está disponible
                String doi = getTextOrEmpty(entry, "prism:doi");
                if (!doi.isEmpty()) {
                    article.setUrl("https://doi.org/" + doi);
                } else {
                    article.setUrl(getTextOrEmpty(entry, "prism:url"));
                }
                
                // Citaciones (con manejo de errores)
                try {
                    article.setCitedByCount(entry.path("citedby-count").asInt());
                } catch (Exception e) {
                    article.setCitedByCount(0);
                }
                
                // Extraer autores - manejar tanto formato de string como de array
                List<String> authors = new ArrayList<>();
                JsonNode creator = entry.path("dc:creator");
                if (!creator.isMissingNode() && !creator.isNull()) {
                    if (creator.isArray()) {
                        // Si es un array de autores
                        for (JsonNode author : creator) {
                            authors.add(author.asText());
                        }
                    } else {
                        // Si es un string único
                        authors.add(creator.asText());
                    }
                }
                article.setAuthors(authors);
                
                // Abstract (si está disponible)
                String abstract_ = getTextOrEmpty(entry, "dc:description");
                article.setAbstract(abstract_);
                
                // Acceso abierto
                article.setOpenAccess(getTextOrEmpty(entry, "openaccessFlag"));
                
                articles.add(article);
            }
            
            System.out.println("Artículos procesados correctamente: " + articles.size());
        } catch (Exception e) {
            System.err.println("Error al procesar la respuesta JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        return articles;
    }
    
    // Método auxiliar para obtener texto o cadena vacía si es nulo
    private String getTextOrEmpty(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field == null || field.isMissingNode() || field.isNull()) {
            return "";
        }
        return field.asText();
    }
}