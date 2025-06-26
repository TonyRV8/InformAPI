package com.example.tarea3;

import com.example.tarea3.Controllers.RecomendacionController;
import com.example.tarea3.Models.Article;
import com.example.tarea3.Services.RecomendacionService;
import com.example.tarea3.Services.FavoritoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecomendacionController.class)
class RecomendacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecomendacionService recomendacionService;

    @MockBean
    private FavoritoService favoritoService;

    private List<Article> mockRecomendaciones;
    private Map<String, Object> mockEstadisticas;

    @BeforeEach
    void setUp() {
        // Configurar recomendaciones de prueba
        Article recomendacion1 = new Article();
        recomendacion1.setId("SCOPUS_ID:REC001");
        recomendacion1.setTitle("Advanced Machine Learning Techniques");
        recomendacion1.setAuthors(Arrays.asList("Alice Wilson", "Bob Brown"));
        recomendacion1.setPublicationName("AI Journal");
        recomendacion1.setPublicationDate("2024-03-10");
        recomendacion1.setDoi("10.1000/rec001");
        recomendacion1.setCitedByCount(35);

        Article recomendacion2 = new Article();
        recomendacion2.setId("SCOPUS_ID:REC002");
        recomendacion2.setTitle("Neural Networks in Practice");
        recomendacion2.setAuthors(Arrays.asList("Charlie Davis"));
        recomendacion2.setPublicationName("Neural Computing");
        recomendacion2.setPublicationDate("2024-03-15");
        recomendacion2.setDoi("10.1000/rec002");
        recomendacion2.setCitedByCount(28);

        mockRecomendaciones = Arrays.asList(recomendacion1, recomendacion2);

        // Configurar estadísticas de prueba
        mockEstadisticas = new HashMap<>();
        mockEstadisticas.put("totalFavoritos", 3);
        mockEstadisticas.put("palabrasClaveExtraidas", 15);
        mockEstadisticas.put("palabrasTop", Arrays.asList("machine", "learning", "neural", "networks"));
        
        Map<String, Integer> frecuencias = new HashMap<>();
        frecuencias.put("machine", 5);
        frecuencias.put("learning", 5);
        frecuencias.put("neural", 3);
        frecuencias.put("networks", 3);
        mockEstadisticas.put("frecuencias", frecuencias);
    }

    

    @Test
    @WithMockUser(username = "testuser")
    void testMostrarRecomendacionesSinFavoritos() throws Exception {
        when(favoritoService.contarFavoritos("testuser")).thenReturn(0L);

        mockMvc.perform(get("/recomendaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendaciones"))
                .andExpect(model().attribute("sinFavoritos", true))
                .andExpect(model().attribute("nombreUsuario", "testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testMostrarRecomendacionesConError() throws Exception {
        when(favoritoService.contarFavoritos("testuser")).thenReturn(3L);
        when(recomendacionService.generarRecomendaciones("testuser", 15))
                .thenThrow(new RuntimeException("Error en la API de Scopus"));

        mockMvc.perform(get("/recomendaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRegenerarRecomendaciones() throws Exception {
        mockMvc.perform(post("/recomendaciones/regenerar")
                .with(csrf())
                .param("limite", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recomendaciones?limite=15&refresh=true"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRegenerarRecomendacionesConLimitePorDefecto() throws Exception {
        mockMvc.perform(post("/recomendaciones/regenerar")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recomendaciones?limite=15&refresh=true"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testObtenerMasRecomendaciones() throws Exception {
        // Simular que hay más recomendaciones disponibles
        List<Article> masRecomendaciones = Arrays.asList(
                mockRecomendaciones.get(0), mockRecomendaciones.get(1),
                new Article() {{ setId("SCOPUS_ID:REC003"); setTitle("Extra Recommendation"); }}
        );
        
        when(recomendacionService.generarRecomendaciones("testuser", 15))
                .thenReturn(masRecomendaciones);

        mockMvc.perform(get("/recomendaciones/mas")
                .param("limite", "5")
                .param("offset", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.hasMore").exists())
                .andExpect(jsonPath("$.recomendaciones").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testObtenerMasRecomendacionesConError() throws Exception {
        when(recomendacionService.generarRecomendaciones("testuser", 10))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(get("/recomendaciones/mas")
                .param("limite", "10")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testObtenerMasRecomendacionesConParametrosPorDefecto() throws Exception {
        when(recomendacionService.generarRecomendaciones("testuser", 10))
                .thenReturn(mockRecomendaciones);

        mockMvc.perform(get("/recomendaciones/mas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void testRecomendacionesWithoutAuthentication() throws Exception {
        // Corregir: esperar 401 (Unauthorized) en lugar de redirección
        mockMvc.perform(get("/recomendaciones"))
                .andExpect(status().isUnauthorized()); // Cambio principal aquí
    }
}