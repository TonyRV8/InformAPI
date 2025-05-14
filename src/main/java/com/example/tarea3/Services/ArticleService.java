package com.example.tarea3.Services;

import com.example.tarea3.Models.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        try {
            int startIndex = (page - 1) * size;
            
            // Log de parámetros para depuración
            System.out.println("Búsqueda avanzada con filtrado local:");
            System.out.println("Query: " + query);
            System.out.println("Page: " + page + ", Size: " + size);
            System.out.println("SortField: " + (sortField != null ? sortField : "null"));
            System.out.println("SortDirection: " + (sortDirection != null ? sortDirection : "null"));
            System.out.println("DateRange: " + (dateRange != null ? dateRange : "null"));
            
            // Realizar búsqueda básica - usar un tamaño moderado para no exceder límites
            String response = scopusService.searchArticlesSimple(query, startIndex, 25);
            
            if (response == null || response.isEmpty()) {
                System.out.println("Respuesta vacía de ScopusService");
                return new ArrayList<>();
            }
            
            // Parsear los resultados
            List<Article> articles = parseArticlesFromResponse(response);
            System.out.println("Artículos obtenidos de la API: " + articles.size());
            
            // Filtrar por fecha si se especificó un rango
            if (dateRange != null && !dateRange.isEmpty()) {
                articles = filterByDateRange(articles, dateRange);
                System.out.println("Artículos después de filtrar por fecha: " + articles.size());
            }
            
            // Aplicar ordenamiento local
            if (sortField != null && !sortField.isEmpty() && 
                sortDirection != null && !sortDirection.isEmpty()) {
                
                articles = sortArticles(articles, sortField, sortDirection);
            }
            
            // Limitar resultados si es necesario
            if (articles.size() > size) {
                articles = articles.subList(0, size);
            }
            
            System.out.println("Total de artículos devueltos después de todos los filtros: " + articles.size());
            return articles;
        } catch (Exception e) {
            System.err.println("Error en búsqueda avanzada: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Método para filtrar artículos por rango de fecha
    private List<Article> filterByDateRange(List<Article> articles, String dateRange) {
        // Formato esperado: "YYYY-YYYY"
        String[] years = dateRange.split("-");
        if (years.length != 2) {
            return articles; // No filtrar si el formato no es el esperado
        }
        
        try {
            int startYear = Integer.parseInt(years[0]);
            int endYear = Integer.parseInt(years[1]);
            
            return articles.stream()
                .filter(article -> {
                    // Extraer el año de la fecha de publicación
                    String pubDate = article.getPublicationDate();
                    if (pubDate == null || pubDate.isEmpty()) {
                        return false;
                    }
                    
                    // El formato típico es YYYY-MM-DD, así que tomamos los primeros 4 caracteres
                    if (pubDate.length() >= 4) {
                        try {
                            int pubYear = Integer.parseInt(pubDate.substring(0, 4));
                            return pubYear >= startYear && pubYear <= endYear;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            // Si hay algún error al parsear los años, devolver la lista original
            return articles;
        }
    }
    
    // Método para ordenar artículos localmente
    private List<Article> sortArticles(List<Article> articles, String sortField, String sortDirection) {
        Comparator<Article> comparator;
        
        // Crear el comparador según el campo de ordenamiento
        switch (sortField.toLowerCase()) {
            case "title":
                comparator = Comparator.comparing(Article::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "citedby-count":
                comparator = Comparator.comparing(Article::getCitedByCount, Comparator.nullsLast(Integer::compareTo));
                break;
            case "publicationyear":
                comparator = Comparator.comparing(
                    article -> {
                        String date = article.getPublicationDate();
                        if (date == null || date.isEmpty()) return "";
                        // Extraer el año de la fecha (formato típico: YYYY-MM-DD)
                        return date.length() >= 4 ? date.substring(0, 4) : "";
                    }, 
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                );
                break;
            case "relevancy":
            default:
                // Para relevancy, mantener el orden original que viene de la API
                return new ArrayList<>(articles);
        }
        
        // Invertir el comparador si la dirección es descendente
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        
        // Crear una nueva lista y ordenarla
        List<Article> sortedList = new ArrayList<>(articles);
        sortedList.sort(comparator);
        return sortedList;
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