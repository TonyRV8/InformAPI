package com.example.tarea3;

import com.example.tarea3.Controllers.SearchController;
import com.example.tarea3.Models.Article;
import com.example.tarea3.Services.ArticleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    private List<Article> mockArticles;

    @BeforeEach
    void setUp() {
        // Configurar artículos de prueba
        Article article1 = new Article();
        article1.setId("SCOPUS_ID:123456");
        article1.setTitle("Machine Learning in Healthcare");
        article1.setAuthors(Arrays.asList("John Doe", "Jane Smith"));
        article1.setPublicationName("Journal of AI");
        article1.setPublicationDate("2024-01-15");
        article1.setDoi("10.1000/test123");
        article1.setCitedByCount(25);

        Article article2 = new Article();
        article2.setId("SCOPUS_ID:789012");
        article2.setTitle("Deep Learning Applications");
        article2.setAuthors(Arrays.asList("Bob Johnson"));
        article2.setPublicationName("AI Research");
        article2.setPublicationDate("2024-02-20");
        article2.setDoi("10.1000/test456");
        article2.setCitedByCount(15);

        mockArticles = Arrays.asList(article1, article2);
    }

    @Test
    @WithMockUser
    void testShowSearchPage() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"));
    }

    @Test
    @WithMockUser
    void testSearchArticlesWithQuery() throws Exception {
        // Configurar mock
        when(articleService.searchArticles(eq("machine learning"), eq(1), eq(10)))
                .thenReturn(mockArticles);

        mockMvc.perform(get("/search/results")
                .param("query", "machine learning")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_results"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attribute("query", "machine learning"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 10));
    }

    @Test
    @WithMockUser
    void testSearchArticlesWithoutQuery() throws Exception {
        mockMvc.perform(get("/search/results"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"));
    }

    @Test
    @WithMockUser
    void testSearchArticlesWithEmptyQuery() throws Exception {
        mockMvc.perform(get("/search/results")
                .param("query", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("search"));
    }

    @Test
    @WithMockUser
    void testSearchArticlesWithException() throws Exception {
        // Configurar mock para lanzar excepción
        when(articleService.searchArticles(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("API Error"));

        mockMvc.perform(get("/search/results")
                .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser
    void testSearchArticlesWithDefaultPagination() throws Exception {
        when(articleService.searchArticles(eq("test"), eq(1), eq(10)))
                .thenReturn(mockArticles);

        mockMvc.perform(get("/search/results")
                .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_results"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 10));
    }

    @Test
    void testSearchWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().is3xxRedirection());
    }
}