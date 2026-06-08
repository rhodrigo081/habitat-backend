package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JwtAuthenticationFilter;
import org.example.dtos.request.AssociateRequest;
import org.example.dtos.response.AssociateResponse;
import org.example.enums.UserRole;
import org.example.models.User;
import org.example.services.AssociateService;
import org.example.services.TokenService;
import org.example.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssociateController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssociateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssociateService associateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenService tokenService;

    private AssociateRequest request;
    private AssociateResponse responseDto;
    private User authenticatedUser;
    private UsernamePasswordAuthenticationToken principal;

    @BeforeEach
    void setUp() {

        request = new AssociateRequest(
                "Maria Souza",
                "52998224725",
                "Rua A",
                "(81) 99999-9999",
                "Relato",
                "Orientação",
                "triagem",
                "judicial",
                1L,
                null
        );

        responseDto = new AssociateResponse(
                1L,
                "Maria Souza",
                "52998224725",
                "Rua A",
                "(81) 99999-9999",
                "Relato",
                "Orientação",
                "triagem",
                "judicial",
                1L,
                2L,
                "Estagiario",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        authenticatedUser = User.builder()
                .id(2L)
                .email("admin@email.com")
                .role(UserRole.ADMINISTRATOR)
                .build();

        principal = new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                authenticatedUser.getAuthorities()
        );
    }

    @Test
    @DisplayName("POST /associates - Deve retornar 201 Created")
    void registerShouldReturn201() throws Exception {

        when(associateService.register(any(), any()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/associates")
                        .with(authentication(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Maria Souza")));
    }

    @Test
    @DisplayName("GET /associates - Deve retornar 200 OK com página de associados")
    void getAllShouldReturn200() throws Exception {

        when(associateService.findAll(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDto)));

        mockMvc.perform(get("/associates")
                        .param("search", "")
                        .with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /associates/{id} - Deve retornar 200 OK com o associado")
    void findByIdShouldReturn200() throws Exception {

        when(associateService.findById(eq(1L), any()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/associates/1")
                        .with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /associates/intern/{internId} - Deve retornar 200 OK com lista")
    void listByInternShouldReturn200() throws Exception {

        when(associateService.findByIntern(eq(2L), any()))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/associates/intern/2")
                        .with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /associates/{id} - Deve retornar 200 OK")
    void updateShouldReturn200() throws Exception {

        when(associateService.update(eq(1L), any(), any()))
                .thenReturn(responseDto);

        mockMvc.perform(put("/associates/1")
                        .with(authentication(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /associates/{id} - Deve retornar 204 No Content")
    void deleteShouldReturn204() throws Exception {

        doNothing().when(associateService)
                .delete(eq(1L), any());

        mockMvc.perform(delete("/associates/1")
                        .with(authentication(principal)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /associates/{id}/history - Deve retornar 200 OK")
    void getHistoryShouldReturn200() throws Exception {

        List<Map<String, Object>> history = List.of(
                Map.of(
                        "id", 1,
                        "action", "Caso criado",
                        "userName", "Estagiario",
                        "createdAt", LocalDateTime.now().toString()
                )
        );

        when(associateService.getHistory(eq(1L), any()))
                .thenReturn(history);

        mockMvc.perform(get("/associates/1/history")
                        .with(authentication(principal)))
                .andExpect(status().isOk());
    }
}