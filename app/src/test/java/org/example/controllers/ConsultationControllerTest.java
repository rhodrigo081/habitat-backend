package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JwtAuthenticationFilter;
import org.example.dtos.request.ConsultationRequest;
import org.example.dtos.response.ConsultationResponse;
import org.example.enums.UserRole;
import org.example.models.User;
import org.example.services.ConsultationService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsultationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConsultationService consultationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private ConsultationRequest request;
    private ConsultationResponse responseDto;
    private User authenticatedUser;
    private UsernamePasswordAuthenticationToken principal;

    @BeforeEach
    void setUp() {
        request = new ConsultationRequest(
                "Resumo da consulta", LocalDate.now(), 1L
        );

        responseDto = new ConsultationResponse(
                1L, "Resumo da consulta", LocalDate.now(), 1L, "Maria Souza",
                2L, "Estagiario", LocalDateTime.now()
        );

        authenticatedUser = User.builder().id(2L).email("estagiario@email.com").role(UserRole.INTERN).build();
        principal = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
    }

    @Test
    @DisplayName("POST /consultations - Deve retornar 201 Created")
    void registerShouldReturn201() throws Exception {
        when(consultationService.register(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/consultations")
                .with(authentication(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /consultations - Deve retornar 200 OK")
    void getAllShouldReturn200() throws Exception {
        when(consultationService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(responseDto)));

        mockMvc.perform(get("/consultations")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /consultations/{id} - Deve retornar 200 OK")
    void getByIdShouldReturn200() throws Exception {
        when(consultationService.findById(any(), any())).thenReturn(responseDto);

        mockMvc.perform(get("/consultations/1")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /consultations/associate/{associateId} - Deve retornar 200 OK")
    void getByAssociateShouldReturn200() throws Exception {
        when(consultationService.findByAssociate(any(), any())).thenReturn(new PageImpl<>(List.of(responseDto)));

        mockMvc.perform(get("/consultations/associate/1")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("PUT /consultations/{id} - Deve retornar 200 OK")
    void updateShouldReturn200() throws Exception {
        when(consultationService.update(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(put("/consultations/1")
                .with(authentication(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /consultations/{id} - Deve retornar 204 No Content")
    void deleteShouldReturn204() throws Exception {
        doNothing().when(consultationService).delete(any(), any());

        mockMvc.perform(delete("/consultations/1")
                .with(authentication(principal)))
                .andExpect(status().isNoContent());
    }
}