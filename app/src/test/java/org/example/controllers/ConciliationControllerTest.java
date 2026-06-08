package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JwtAuthenticationFilter;
import org.example.dtos.request.ConciliationRequest;
import org.example.dtos.response.ConciliationResponse;
import org.example.enums.CitationStatus;
import org.example.enums.UserRole;
import org.example.models.User;
import org.example.services.ConciliationService;
import org.example.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConciliationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConciliationService conciliationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private ConciliationRequest request;
    private ConciliationResponse responseDto;
    private User authenticatedUser;
    private UsernamePasswordAuthenticationToken principal;

    @BeforeEach
    void setUp() {
        LocalDateTime conciliationDate = LocalDateTime.now().plusDays(10);
        
        request = new ConciliationRequest(
                "João Silva", "81988887777", conciliationDate,
                "Resumo", CitationStatus.PENDING, 1L
        );

        responseDto = new ConciliationResponse(
                1L, "João Silva", "81988887777", conciliationDate,
                "Resumo", CitationStatus.PENDING, 1L, "Maria Souza",
                2L, "Estagiario", LocalDateTime.now()
        );

        authenticatedUser = User.builder().id(2L).email("estagiario@email.com").role(UserRole.INTERN).build();
        principal = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
    }

    @Test
    @DisplayName("POST /conciliations - Deve retornar 201 Created")
    void registerShouldReturn201() throws Exception {
        when(conciliationService.register(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/conciliations")
                .with(authentication(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /conciliations - Deve retornar 200 OK")
    void getAllShouldReturn200() throws Exception {
        when(conciliationService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(responseDto)));

        mockMvc.perform(get("/conciliations")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /conciliations/{id} - Deve retornar 200 OK")
    void getByIdShouldReturn200() throws Exception {
        when(conciliationService.findById(any(), any())).thenReturn(responseDto);

        mockMvc.perform(get("/conciliations/1")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /conciliations/associate/{associateId} - Deve retornar 200 OK")
    void getByAssociateShouldReturn200() throws Exception {
        when(conciliationService.findByAssociate(any())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/conciliations/associate/1")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /conciliations/next-audiences - Deve retornar 200 OK")
    void getNextAudiencesShouldReturn200() throws Exception {
        when(conciliationService.findNextAudiences(any(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/conciliations/next-audiences")
                .param("dias", "7")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("PUT /conciliations/{id} - Deve retornar 200 OK")
    void updateShouldReturn200() throws Exception {
        when(conciliationService.update(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(put("/conciliations/1")
                .with(authentication(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PATCH /conciliations/{id}/citation-status - Deve retornar 200 OK")
    void updateCitationStatusShouldReturn200() throws Exception {
        when(conciliationService.updateStatus(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/conciliations/1/citation-status")
                .param("status", "CITED")
                .with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /conciliations/{id} - Deve retornar 204 No Content")
    void deleteShouldReturn204() throws Exception {
        doNothing().when(conciliationService).delete(any(), any());

        mockMvc.perform(delete("/conciliations/1")
                .with(authentication(principal)))
                .andExpect(status().isNoContent());
    }
}