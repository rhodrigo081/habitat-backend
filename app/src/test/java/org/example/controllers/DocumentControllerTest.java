package org.example.controllers;

import org.example.config.JwtAuthenticationFilter;
import org.example.enums.DocumentFormat;
import org.example.enums.DocumentType;
import org.example.enums.UserRole;
import org.example.models.Associate;
import org.example.models.User;
import org.example.services.AssociateService;
import org.example.services.DocumentService;
import org.example.services.TokenService;
import org.example.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private AssociateService associateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenService tokenService;

    private User authenticatedUser;
    private UsernamePasswordAuthenticationToken principal;
    private byte[] documentContent;
    private Associate associate;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .id(2L)
                .email("estagiario@email.com")
                .role(UserRole.INTERN)
                .build();

        principal = new UsernamePasswordAuthenticationToken(
                authenticatedUser, null, authenticatedUser.getAuthorities()
        );

        documentContent = "Conteudo simulado do documento".getBytes();

        associate = Associate.builder()
                .id(1L)
                .name("Maria Souza")
                .build();
    }

    @Test
    @DisplayName("POST /documents/generate - Deve retornar 200 OK e o ficheiro")
    void generateDocumentShouldReturn200AndFile() throws Exception {

        String requestJson = """
                {
                    "associateId": 1,
                    "type": "POWER_OF_ATTORNEY",
                    "format": "PDF"
                }
                """;

        when(associateService.findAssociateById(anyLong())).thenReturn(associate);

        when(documentService.generate(any(), any())).thenReturn(documentContent);

        when(documentService.getContentType(any(DocumentFormat.class))).thenReturn("application/pdf");
        when(documentService.getFilename(
                any(DocumentType.class),
                any(DocumentFormat.class),
                any(String.class)
        )).thenReturn("procuracao_maria_souza.pdf");

        mockMvc.perform(post("/documents/generate")
                        .with(authentication(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(content().bytes(documentContent));
    }
}
