package com.example.tarea3;

import com.example.tarea3.Controllers.FavoritoController;
import com.example.tarea3.Models.Favorito;
import com.example.tarea3.Services.FavoritoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoritoController.class)
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoritoService favoritoService;

    private List<Favorito> mockFavoritos;

    @BeforeEach
    void setUp() {
        Favorito favorito1 = new Favorito();
        favorito1.setId(1L);
        favorito1.setUsuarioId(1L);
        favorito1.setArticleId("SCOPUS_ID:123456");
        favorito1.setTitle("Machine Learning in Healthcare");
        favorito1.setAuthors("John Doe, Jane Smith");
        favorito1.setPublicationName("Journal of AI");
        favorito1.setPublicationDate("2024-01-15");
        favorito1.setDoi("10.1000/test123");
        favorito1.setCitedByCount(25);
        favorito1.setFechaAgregado(LocalDateTime.now());

        Favorito favorito2 = new Favorito();
        favorito2.setId(2L);
        favorito2.setUsuarioId(1L);
        favorito2.setArticleId("SCOPUS_ID:789012");
        favorito2.setTitle("Deep Learning Applications");
        favorito2.setAuthors("Bob Johnson");
        favorito2.setPublicationName("AI Research");
        favorito2.setPublicationDate("2024-02-20");
        favorito2.setDoi("10.1000/test456");
        favorito2.setCitedByCount(15);
        favorito2.setFechaAgregado(LocalDateTime.now());

        mockFavoritos = Arrays.asList(favorito1, favorito2);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testMostrarFavoritos() throws Exception {
        when(favoritoService.obtenerFavoritosDeUsuario("testuser")).thenReturn(mockFavoritos);
        when(favoritoService.contarFavoritos("testuser")).thenReturn(2L);

        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isOk())
                .andExpect(view().name("favoritos"))
                .andExpect(model().attributeExists("favoritos"))
                .andExpect(model().attribute("totalFavoritos", 2L))
                .andExpect(model().attribute("nombreUsuario", "testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testMostrarFavoritosConError() throws Exception {
        when(favoritoService.obtenerFavoritosDeUsuario("testuser"))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testToggleFavoritoAgregar() throws Exception {
        when(favoritoService.toggleFavorito(eq("testuser"), eq("SCOPUS_ID:123456"), 
                anyString(), anyString(), anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyInt(), anyString())).thenReturn(true);
        when(favoritoService.esFavorito("testuser", "SCOPUS_ID:123456")).thenReturn(true);

        mockMvc.perform(post("/favoritos/toggle")
                .with(csrf())
                .param("articleId", "SCOPUS_ID:123456")
                .param("title", "Test Article")
                .param("authors", "Test Author")
                .param("publicationName", "Test Journal")
                .param("publicationDate", "2024-01-15")
                .param("doi", "10.1000/test")
                .param("url", "https://test.com")
                .param("abstract", "Test abstract")
                .param("citedByCount", "10")
                .param("openAccess", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favoritos"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testToggleFavoritoConRedirectUrl() throws Exception {
        when(favoritoService.toggleFavorito(anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyString(), anyString(), anyString(), 
                anyString(), anyInt(), anyString())).thenReturn(true);

        mockMvc.perform(post("/favoritos/toggle")
                .with(csrf())
                .param("articleId", "SCOPUS_ID:123456")
                .param("title", "Test Article")
                .param("redirectUrl", "/search/results"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search/results"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEliminarFavorito() throws Exception {
        when(favoritoService.eliminarDeFavoritos("testuser", "SCOPUS_ID:123456"))
                .thenReturn(true);

        mockMvc.perform(post("/favoritos/eliminar/SCOPUS_ID:123456")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favoritos"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEliminarFavoritoError() throws Exception {
        when(favoritoService.eliminarDeFavoritos("testuser", "SCOPUS_ID:123456"))
                .thenReturn(false);

        mockMvc.perform(post("/favoritos/eliminar/SCOPUS_ID:123456")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favoritos"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testVerificarFavorito() throws Exception {
        when(favoritoService.esFavorito("testuser", "SCOPUS_ID:123456")).thenReturn(true);

        mockMvc.perform(get("/favoritos/check/SCOPUS_ID:123456"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"esFavorito\": true}"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testVerificarFavoritoNoExiste() throws Exception {
        when(favoritoService.esFavorito("testuser", "SCOPUS_ID:999999")).thenReturn(false);

        mockMvc.perform(get("/favoritos/check/SCOPUS_ID:999999"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"esFavorito\": false}"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testVerificarFavoritoConError() throws Exception {
        when(favoritoService.esFavorito("testuser", "SCOPUS_ID:123456"))
                .thenThrow(new RuntimeException("Error de servicio"));

        mockMvc.perform(get("/favoritos/check/SCOPUS_ID:123456"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"esFavorito\": false")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"error\"")));
    }

    @Test
    void testFavoritosWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().is3xxRedirection());
    }
}