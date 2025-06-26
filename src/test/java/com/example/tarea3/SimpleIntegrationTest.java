package com.example.tarea3;

import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Models.Rol;
import com.example.tarea3.Models.Favorito;
import com.example.tarea3.Repositories.UsuarioRepository;
import com.example.tarea3.Repositories.RolRepository;
import com.example.tarea3.Repositories.FavoritoRepository;
import com.example.tarea3.Services.ScopusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SimpleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ScopusService scopusService;

    private Rol rolUser;
    private Rol rolAdmin;

    // Configurar H2 en memoria dinámicamente
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("scopus.api.key", () -> "test-api-key");
        registry.add("logging.level.org.springframework.security", () -> "ERROR");
        registry.add("logging.level.org.hibernate", () -> "ERROR");
    }

    @BeforeEach
    void setUp() {
        // Mock de Scopus response
        String mockScopusResponse = """
            {
                "search-results": {
                    "opensearch:totalResults": "2",
                    "entry": [
                        {
                            "dc:identifier": "SCOPUS_ID:123456",
                            "dc:title": "Machine Learning in Healthcare",
                            "dc:creator": ["John Doe", "Jane Smith"],
                            "prism:publicationName": "Journal of AI",
                            "prism:coverDate": "2024-01-15",
                            "prism:doi": "10.1000/test123",
                            "citedby-count": "25",
                            "dc:description": "A comprehensive study on machine learning."
                        }
                    ]
                }
            }
            """;
        
        when(scopusService.searchArticles(anyString(), anyInt(), anyInt()))
                .thenReturn(mockScopusResponse);

        // Crear o reutilizar roles
        rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre("ROLE_USER");
                    return rolRepository.save(rol);
                });

        rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre("ROLE_ADMIN");
                    return rolRepository.save(rol);
                });
    }

    @Test
    void testRegistroDeUsuario() throws Exception {
        mockMvc.perform(post("/usuarios/registrar")
                .with(csrf())
                .param("nombre", "newuser")
                .param("email", "newuser@example.com")
                .param("passwordHash", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verificar que el usuario fue creado
        Optional<Usuario> usuarioCreado = usuarioRepository.findByNombre("newuser");
        assertTrue(usuarioCreado.isPresent());
        assertEquals("newuser@example.com", usuarioCreado.get().getEmail());
    }

    @Test
    void testRegistroConNombreDuplicado() throws Exception {
        // Crear usuario primero
        crearUsuario("existing", "existing@test.com", rolUser);

        // Intentar registrar con mismo nombre
        mockMvc.perform(post("/usuarios/registrar")
                .with(csrf())
                .param("nombre", "existing") // Mismo nombre
                .param("email", "another@test.com")
                .param("passwordHash", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/registro/formulario"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void testBusquedaDeArticulos() throws Exception {
        crearUsuario("testuser", "testuser@test.com", rolUser);

        mockMvc.perform(get("/search/results")
                .param("query", "machine learning")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("search_results"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attribute("query", "machine learning"));

        verify(scopusService, atLeastOnce()).searchArticles(contains("machine learning"), eq(0), eq(10));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void testGestionDeFavoritos() throws Exception {
        Usuario usuario = crearUsuario("testuser", "testuser@test.com", rolUser);

        // Verificar que empieza sin favoritos
        assertEquals(0, favoritoRepository.countByUsuarioId(usuario.getId()));

        // Agregar favorito
        mockMvc.perform(post("/favoritos/toggle")
                .with(csrf())
                .param("articleId", "SCOPUS_ID:TEST001")
                .param("title", "Test Article")
                .param("authors", "Test Author")
                .param("publicationName", "Test Journal")
                .param("publicationDate", "2024-01-15")
                .param("citedByCount", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favoritos"));

        // Verificar favorito guardado
        assertEquals(1, favoritoRepository.countByUsuarioId(usuario.getId()));

        // Ver favoritos
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isOk())
                .andExpect(view().name("favoritos"))
                .andExpect(model().attributeExists("favoritos"))
                .andExpect(model().attribute("totalFavoritos", 1L));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void testRecomendacionesSinFavoritos() throws Exception {
        crearUsuario("testuser", "testuser@test.com", rolUser);

        mockMvc.perform(get("/recomendaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendaciones"))
                .andExpect(model().attribute("sinFavoritos", true))
                .andExpect(model().attribute("nombreUsuario", "testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void testRecomendacionesConFavoritos() throws Exception {
        Usuario usuario = crearUsuario("testuser", "testuser@test.com", rolUser);
        crearFavorito(usuario);

        mockMvc.perform(get("/recomendaciones")
                .param("limite", "15"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendaciones"))
                .andExpect(model().attributeExists("recomendaciones"))
                .andExpect(model().attributeExists("estadisticas"))
                .andExpect(model().attribute("limite", 15))
                .andExpect(model().attribute("sinFavoritos", false));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testAdministracionUsuarios() throws Exception {
        crearUsuario("admin", "admin@test.com", rolAdmin);
        Usuario testUser = crearUsuario("testuser", "testuser@test.com", rolUser);

        // Listar usuarios
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios"))
                .andExpect(model().attributeExists("usuarios"));

        // Editar usuario
        mockMvc.perform(get("/admin/usuarios/editar/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("editar_usuario"))
                .andExpect(model().attributeExists("usuario"));

        // Actualizar usuario
        mockMvc.perform(post("/admin/usuarios/editar/" + testUser.getId())
                .with(csrf())
                .param("nombre", "testuser_updated")
                .param("email", "updated@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        // Verificar actualización
        Usuario usuarioActualizado = usuarioRepository.findById(testUser.getId()).get();
        assertEquals("testuser_updated", usuarioActualizado.getNombre());
        assertEquals("updated@test.com", usuarioActualizado.getEmail());
    }

    @Test
    void testAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/recomendaciones"))
                .andExpect(status().isUnauthorized());
    }

    

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testEliminacionUsuario() throws Exception {
        crearUsuario("admin", "admin@test.com", rolAdmin);
        Usuario testUser = crearUsuario("deleteuser", "delete@test.com", rolUser);
        Long userId = testUser.getId();

        // Eliminar usuario
        mockMvc.perform(get("/admin/usuarios/eliminar/" + userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        // Verificar eliminación
        assertFalse(usuarioRepository.findById(userId).isPresent());
    }

    // Métodos auxiliares
    private Usuario crearUsuario(String nombre, String email, Rol rol) {
        Optional<Usuario> existing = usuarioRepository.findByNombre(nombre);
        if (existing.isPresent()) {
            return existing.get();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode("password123"));
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        usuario.setRoles(roles);
        return usuarioRepository.save(usuario);
    }

    private void crearFavorito(Usuario usuario) {
        Favorito favorito = new Favorito();
        favorito.setUsuarioId(usuario.getId());
        favorito.setArticleId("SCOPUS_ID:FAV001");
        favorito.setTitle("Recommendation Test Article");
        favorito.setAuthors("Test Author");
        favorito.setPublicationName("Test Journal");
        favorito.setAbstractText("Machine learning artificial intelligence test recommendation");
        favorito.setCitedByCount(15);
        favoritoRepository.save(favorito);
    }
}