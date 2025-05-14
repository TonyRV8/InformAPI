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
     * @param query      Consulta de búsqueda (ej: "TITLE-ABS-KEY(inteligencia
     *                   artificial)")
     * @param startIndex Índice de inicio para paginación
     * @param count      Número de resultados por página
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

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error en la consulta a Scopus: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, devolver un JSON vacío pero válido para que el parser no falle
            return "{\"search-results\":{\"entry\":[]}}";
        }
    }

    /**
     * Busca artículos con una consulta simple, formateando la query si es necesario
     */
    public String searchArticlesSimple(String query, int startIndex, int count) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            
            // Formatea la consulta correctamente para Scopus (solo si es necesario)
            if (!query.contains("TITLE-ABS-KEY") && !query.contains("AUTHOR-NAME") && 
                !query.contains("SRCTITLE") && !query.contains("DOI")) {
                query = "TITLE-ABS-KEY(" + query + ")";
            }
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("apiKey", apiKey)
                    .queryParam("query", query)
                    .queryParam("start", startIndex)
                    .queryParam("count", count);
            
            System.out.println("URL de consulta a Scopus simple: " + builder.toUriString());
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error en la consulta a Scopus: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, devolver un JSON vacío pero válido para que el parser no falle
            return "{\"search-results\":{\"entry\":[]}}";
        }
    }
}