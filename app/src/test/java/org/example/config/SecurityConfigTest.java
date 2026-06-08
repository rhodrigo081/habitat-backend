package org.example.config;

import org.example.repositories.UserRepository;
import org.example.services.AssociateService;
import org.example.services.AuthService;
import org.example.services.ConciliationService;
import org.example.services.ConsultationService;
import org.example.services.DocumentService;
import org.example.services.FileAttachmentService;
import org.example.services.ProcessService;
import org.example.services.TokenService;
import org.example.services.UserDetailsServiceImpl;
import org.example.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AssociateService associateService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ConciliationService conciliationService;

    @MockitoBean
    private ConsultationService consultationService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private FileAttachmentService fileAttachmentService;

    @MockitoBean
    private ProcessService processService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Deve permitir o acesso público à rota de login (POST /auth/login)")
    void shouldAllowPublicAccessToLogin() throws Exception {
        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"teste@teste.com\", \"password\":\"123456\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Deve negar acesso (403) para rotas protegidas sem autenticação")
    void shouldDenyAccessToProtectedRoutesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/associados").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMINISTRATOR tem permissão total para gerenciar usuários")
    @WithMockUser(roles = "ADMINISTRATOR")
    void adminShouldManageUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is2xxSuccessful()); 
                
        mockMvc.perform(delete("/users/1").with(csrf()))
                .andExpect(status().is2xxSuccessful()); 
    }

    @Test
    @DisplayName("INTERN não deve ter acesso de escrita em usuários (retorna 403)")
    @WithMockUser(roles = "INTERN")
    void internShouldNotManageUsers() throws Exception {
        mockMvc.perform(post("/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden()); 
                
        mockMvc.perform(delete("/users/1").with(csrf()))
                .andExpect(status().isForbidden()); 
    }

    @Test
    @DisplayName("INTERN e ADMINISTRATOR podem criar entidades como associados")
    @WithMockUser(roles = "INTERN")
    void internShouldCreateAssociates() throws Exception {
        mockMvc.perform(post("/associados").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus(), "O acesso não deveria ser bloqueado pela segurança (403)")); 
    }

    @Test
    @DisplayName("COORDINATOR não pode criar associados, mas pode ler (GET)")
    @WithMockUser(roles = "COORDINATOR")
    void coordinatorPermissionsOnAssociates() throws Exception {
        mockMvc.perform(post("/associados").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/associados"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus(), "O acesso não deveria ser bloqueado pela segurança (403)")); 
    }

    @Test
    @DisplayName("Qualquer cargo logado (ex: COORDINATOR) pode acessar rotas de documents")
    @WithMockUser(roles = "COORDINATOR")
    void coordinatorCanAccessDocuments() throws Exception {
        mockMvc.perform(get("/documents/generate"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus(), "O acesso não deveria ser bloqueado pela segurança (403)")); 
    }
}