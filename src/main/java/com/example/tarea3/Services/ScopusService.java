package com.example.tarea3.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ScopusService {

    private final RestTemplate restTemplate;
    
    @Value("${scopus.api.key}")
    private String apiKey;
    
    private static final String BASE_URL = "https://api.elsevier.com/content/search/scopus";

    public ScopusService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Busca artículos en Scopus según los términos de búsqueda proporcionados
     * 
     * @param query Consulta de búsqueda (ej: "TITLE-ABS-KEY(inteligencia artificial)")
     * @param startIndex Índice de inicio para paginación
     * @param count Número de resultados por página
     * @return Respuesta en formato String (XML o JSON)
     */
    public String searchArticles(String query, int startIndex, int count) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("apiKey", apiKey)
                .queryParam("query", query)
                .queryParam("start", startIndex)
                .queryParam("count", count);
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);
        
        return response.getBody();
    }
    
    /**
     * Busca artículos con parámetros adicionales como campo de ordenación,
     * filtro de fecha, etc.
     */
    public String searchArticlesAdvanced(String query, int startIndex, int count, 
                                        String sortField, String sortDirection, 
                                        String dateRange, String view) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("apiKey", apiKey)
                .queryParam("query", query)
                .queryParam("start", startIndex)
                .queryParam("count", count);
        
        // Parámetros opcionales
        if (sortField != null && sortDirection != null) {
            builder.queryParam("sort", sortField + " " + sortDirection);
        }
        
        if (dateRange != null) {
            builder.queryParam("date", dateRange);
        }
        
        if (view != null) {
            builder.queryParam("view", view);
        }
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);
        
        return response.getBody();
    }
}