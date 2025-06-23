package com.example.tarea3.Services;

import com.example.tarea3.Models.Article;
import com.example.tarea3.Models.Favorito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecomendacionService {
    
    @Autowired
    private FavoritoService favoritoService;
    
    @Autowired
    private ArticleService articleService;
    
    // Palabras a excluir del análisis (stop words en español e inglés)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
    // Español
    "el", "la", "de", "que", "y", "a", "en", "un", "es", "se", "no", "te", "lo", "le", "da", "su", "por", "son",
    "con", "para", "al", "una", "del", "todo", "pero", "más", "hace", "muy", "fue", "han", "vez", "puede", "esto",
    "ya", "sobre", "está", "aquí", "cada", "bien", "donde", "sino", "otros", "ese", "esa", "sus", "les", "como",
    // Inglés
    "the", "of", "and", "a", "to", "in", "is", "you", "that", "it", "he", "was", "for", "on", "are", "as", "with",
    "his", "they", "i", "at", "be", "this", "have", "from", "or", "one", "had", "by", "word", "but", "not", "what",
    "all", "were", "we", "when", "your", "can", "said", "there", "each", "which", "she", "do", "how", "their", "if",
    "will", "up", "other", "about", "out", "many", "then", "them", "these", "so", "some", "her", "would", "make",
    "like", "into", "him", "has", "two", "more", "go", "no", "way", "could", "my", "than", "first", "been", "call",
    "who", "oil", "its", "now", "find", "long", "down", "day", "did", "get", "come", "made", "may", "part", "over",
    // Palabras técnicas comunes
    "using", "based", "analysis", "study", "research", "method", "approach", "results", "data", "model", "system",
    "application", "applications", "new", "novel", "comprehensive", "review", "systematic", "meta", "case", "cases",
    "effect", "effects", "impact", "impacts", "factor", "factors", "evaluation", "assessment", "comparison",
    "mediante", "través", "análisis", "estudio", "investigación", "método", "enfoque", "resultados", "datos",
    "modelo", "sistema", "aplicación", "aplicaciones", "nuevo", "nueva", "revisión", "sistemática", "caso", "casos"
));

    
    /**
     * Genera recomendaciones basadas en los favoritos del usuario
     */
    public List<Article> generarRecomendaciones(String nombreUsuario, int limite) {
        try {
            // 1. Obtener favoritos del usuario
            List<Favorito> favoritos = favoritoService.obtenerFavoritosDeUsuario(nombreUsuario);
            
            if (favoritos.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. Extraer palabras clave de los favoritos
            Map<String, Integer> palabrasFrecuencia = extraerPalabrasClave(favoritos);
            
            // 3. Obtener las palabras más relevantes
            List<String> palabrasTop = obtenerPalabrasTop(palabrasFrecuencia, 8);
            
            if (palabrasTop.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 4. Buscar artículos usando las palabras clave
            List<Article> recomendaciones = buscarRecomendaciones(palabrasTop, limite * 2);
            
            // 5. Filtrar artículos que ya están en favoritos
            Set<String> favoritosIds = favoritos.stream()
                    .map(Favorito::getArticleId)
                    .collect(Collectors.toSet());
            
            List<Article> recomendacionesFiltradas = recomendaciones.stream()
                    .filter(article -> !favoritosIds.contains(article.getId()))
                    .limit(limite)
                    .collect(Collectors.toList());
            
            System.out.println("Recomendaciones generadas: " + recomendacionesFiltradas.size());
            System.out.println("Palabras clave utilizadas: " + palabrasTop);
            
            return recomendacionesFiltradas;
            
        } catch (Exception e) {
            System.err.println("Error al generar recomendaciones: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Extrae palabras clave de los títulos y abstracts de los favoritos
     */
    private Map<String, Integer> extraerPalabrasClave(List<Favorito> favoritos) {
        Map<String, Integer> frecuenciaPalabras = new HashMap<>();
        
        for (Favorito favorito : favoritos) {
            // Procesar título
            if (favorito.getTitle() != null) {
                procesarTexto(favorito.getTitle(), frecuenciaPalabras, 2); // Peso mayor para títulos
            }
            
            // Procesar abstract
            if (favorito.getAbstractText() != null) {
                procesarTexto(favorito.getAbstractText(), frecuenciaPalabras, 1);
            }
            
            // Procesar nombre de publicación (revista)
            if (favorito.getPublicationName() != null) {
                procesarTexto(favorito.getPublicationName(), frecuenciaPalabras, 1);
            }
        }
        
        return frecuenciaPalabras;
    }
    
    /**
     * Procesa un texto y extrae palabras significativas
     */
    private void procesarTexto(String texto, Map<String, Integer> frecuenciaPalabras, int peso) {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        
        // Limpiar texto: minúsculas, remover puntuación, dividir por espacios
        String textoLimpio = texto.toLowerCase()
                .replaceAll("[^a-záéíóúñü\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        
        String[] palabras = textoLimpio.split("\\s+");
        
        for (String palabra : palabras) {
            palabra = palabra.trim();
            
            // Filtrar palabras muy cortas, muy largas, o stop words
            if (palabra.length() >= 3 && palabra.length() <= 20 && !STOP_WORDS.contains(palabra)) {
                frecuenciaPalabras.merge(palabra, peso, Integer::sum);
            }
        }
    }
    
    /**
     * Obtiene las palabras más frecuentes
     */
    private List<String> obtenerPalabrasTop(Map<String, Integer> frecuenciaPalabras, int limite) {
        return frecuenciaPalabras.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limite)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca artículos usando las palabras clave identificadas
     */
    private List<Article> buscarRecomendaciones(List<String> palabrasClave, int limite) {
        List<Article> todasRecomendaciones = new ArrayList<>();
        
        // Estrategia 1: Buscar con combinaciones de palabras clave
        if (palabrasClave.size() >= 2) {
            // Buscar con las 2-3 palabras más importantes combinadas
            for (int i = 0; i < Math.min(3, palabrasClave.size() - 1); i++) {
                for (int j = i + 1; j < Math.min(i + 3, palabrasClave.size()); j++) {
                    String query = "TITLE-ABS-KEY(" + palabrasClave.get(i) + " AND " + palabrasClave.get(j) + ")";
                    try {
                        List<Article> resultados = articleService.searchArticles(query, 1, 10);
                        todasRecomendaciones.addAll(resultados);
                        
                        if (todasRecomendaciones.size() >= limite) {
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("Error en búsqueda combinada: " + e.getMessage());
                    }
                }
                if (todasRecomendaciones.size() >= limite) {
                    break;
                }
            }
        }
        
        // Estrategia 2: Buscar con palabras individuales si no hay suficientes resultados
        if (todasRecomendaciones.size() < limite / 2) {
            for (String palabra : palabrasClave.subList(0, Math.min(5, palabrasClave.size()))) {
                String query = "TITLE-ABS-KEY(" + palabra + ")";
                try {
                    List<Article> resultados = articleService.searchArticles(query, 1, 8);
                    todasRecomendaciones.addAll(resultados);
                    
                    if (todasRecomendaciones.size() >= limite) {
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error en búsqueda individual: " + e.getMessage());
                }
            }
        }
        
        // Eliminar duplicados y ordenar por citaciones
        return todasRecomendaciones.stream()
                .collect(Collectors.toMap(
                    Article::getId,
                    article -> article,
                    (existing, replacement) -> existing.getCitedByCount() > replacement.getCitedByCount() ? existing : replacement
                ))
                .values()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getCitedByCount(), a.getCitedByCount()))
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene información sobre las palabras clave utilizadas para recomendaciones
     */
    public Map<String, Object> obtenerEstadisticasRecomendaciones(String nombreUsuario) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            List<Favorito> favoritos = favoritoService.obtenerFavoritosDeUsuario(nombreUsuario);
            Map<String, Integer> palabrasFrecuencia = extraerPalabrasClave(favoritos);
            List<String> palabrasTop = obtenerPalabrasTop(palabrasFrecuencia, 10);
            
            estadisticas.put("totalFavoritos", favoritos.size());
            estadisticas.put("palabrasClaveExtraidas", palabrasFrecuencia.size());
            estadisticas.put("palabrasTop", palabrasTop);
            estadisticas.put("frecuencias", palabrasFrecuencia);
            
        } catch (Exception e) {
            estadisticas.put("error", e.getMessage());
        }
        
        return estadisticas;
    }
}