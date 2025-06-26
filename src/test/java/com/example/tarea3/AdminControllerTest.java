package com.example.tarea3;

import com.example.tarea3.Controllers.AdminController;
import com.example.tarea3.Models.Usuario;
import com.example.tarea3.Models.Rol;
import com.example.tarea3.Services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioAdmin;
    private Usuario usuarioRegular;
    private List<Usuario> usuarios;

    @BeforeEach
    void setUp() {
        // Configurar roles
        Rol rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ROLE_ADMIN");

        Rol rolUser = new Rol();
        rolUser.setId(2L);
        rolUser.setNombre("ROLE_USER");

        // Usuario administrador
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("admin");
        usuarioAdmin.setEmail("admin@example.com");
        usuarioAdmin.setPassword("$2a$10$encodedPassword");
        Set<Rol> rolesAdmin = new HashSet<>();
        rolesAdmin.add(rolAdmin);
        usuarioAdmin.setRoles(rolesAdmin);

        // Usuario regular
        usuarioRegular = new Usuario();
        usuarioRegular.setId(2L);
        usuarioRegular.setNombre("usuario");
        usuarioRegular.setEmail("usuario@example.com");
        usuarioRegular.setPassword("$2a$10$encodedPassword");
        Set<Rol> rolesUser = new HashSet<>();
        rolesUser.add(rolUser);
        usuarioRegular.setRoles(rolesUser);

        usuarios = Arrays.asList(usuarioAdmin, usuarioRegular);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testListarUsuarios() throws Exception {
        when(usuarioService.obtenerTodos()).thenReturn(usuarios);

        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(model().attribute("usuarios", usuarios));

        verify(usuarioService).obtenerTodos();
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testListarUsuariosSinPermisoAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).obtenerTodos();
    }

    @Test
    void testListarUsuariosSinAutenticacion() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).obtenerTodos();
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testEditarUsuarioExistente() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioAdmin);

        mockMvc.perform(get("/admin/usuarios/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editar_usuario"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("usuario", usuarioAdmin));

        verify(usuarioService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testEditarUsuarioNoExistente() throws Exception {
        when(usuarioService.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/admin/usuarios/editar/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).obtenerPorId(999L);
        verify(usuarioService, never()).guardar(any());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testActualizarUsuarioSinPassword() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioAdmin);

        mockMvc.perform(post("/admin/usuarios/editar/1")
                .with(csrf())
                .param("nombre", "admin_actualizado")
                .param("email", "admin_nuevo@example.com")
                .param("password", "")) // Password vacío
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).obtenerPorId(1L);
        verify(usuarioService).guardar(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString()); // No debe encodear password vacío
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testActualizarUsuarioConPassword() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioAdmin);
        when(passwordEncoder.encode("nuevaPassword")).thenReturn("$2a$10$newEncodedPassword");

        mockMvc.perform(post("/admin/usuarios/editar/1")
                .with(csrf())
                .param("nombre", "admin_actualizado")
                .param("email", "admin_nuevo@example.com")
                .param("password", "nuevaPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).obtenerPorId(1L);
        verify(usuarioService).guardar(any(Usuario.class));
        verify(passwordEncoder).encode("nuevaPassword");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testActualizarUsuarioNoExistente() throws Exception {
        when(usuarioService.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(post("/admin/usuarios/editar/999")
                .with(csrf())
                .param("nombre", "usuario_inexistente")
                .param("email", "inexistente@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).obtenerPorId(999L);
        verify(usuarioService, never()).guardar(any());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testEliminarUsuario() throws Exception {
        mockMvc.perform(get("/admin/usuarios/eliminar/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).eliminar(2L);
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testEditarUsuarioSinPermisoAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios/editar/1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testActualizarUsuarioSinPermisoAdmin() throws Exception {
        mockMvc.perform(post("/admin/usuarios/editar/1")
                .with(csrf())
                .param("nombre", "hacker")
                .param("email", "hacker@example.com"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).obtenerPorId(any());
        verify(usuarioService, never()).guardar(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void testEliminarUsuarioSinPermisoAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios/eliminar/1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).eliminar(any());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testActualizarUsuarioSinCsrfToken() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioAdmin);

        mockMvc.perform(post("/admin/usuarios/editar/1")
                // Sin .with(csrf())
                .param("nombre", "admin_actualizado")
                .param("email", "admin_nuevo@example.com"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).obtenerPorId(any());
        verify(usuarioService, never()).guardar(any());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testActualizarUsuarioConParametrosCompletos() throws Exception {
        when(usuarioService.obtenerPorId(2L)).thenReturn(usuarioRegular);
        when(passwordEncoder.encode("passwordCompleja123!")).thenReturn("$2a$10$complexEncodedPassword");

        mockMvc.perform(post("/admin/usuarios/editar/2")
                .with(csrf())
                .param("nombre", "usuario_modificado")
                .param("email", "usuario_modificado@example.com")
                .param("password", "passwordCompleja123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).obtenerPorId(2L);
        verify(usuarioService).guardar(argThat(usuario -> 
            "usuario_modificado".equals(usuario.getNombre()) &&
            "usuario_modificado@example.com".equals(usuario.getEmail())
        ));
        verify(passwordEncoder).encode("passwordCompleja123!");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testEliminarMultiplesUsuarios() throws Exception {
        // Simular eliminación de varios usuarios
        mockMvc.perform(get("/admin/usuarios/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        mockMvc.perform(get("/admin/usuarios/eliminar/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(usuarioService).eliminar(1L);
        verify(usuarioService).eliminar(2L);
    }
}