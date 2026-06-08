package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JwtAuthenticationFilter;
import org.example.dtos.request.ProcessRequest;
import org.example.dtos.response.ProcessResponse;
import org.example.enums.ProcessStatus;
import org.example.enums.UserRole;
import org.example.models.User;
import org.example.services.ProcessService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessService processService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private ProcessRequest request;
    private ProcessResponse responseDto;
    private User authenticatedUser;
    private UsernamePasswordAuthenticationToken principal;

    @BeforeEach
    void setUp() {
        request = new ProcessRequest(
                "0001112222026", "Recife", "1ª Vara Cível",
                "Descrição do processo teste", ProcessStatus.INITIAL, 1L
        );

        responseDto = new ProcessResponse(
                1L, "0001112222026", "Recife", "1ª Vara Cível",
                "Descrição do processo teste", ProcessStatus.INITIAL, 1L, "Maria Souza",
                2L, "Estagiario", LocalDateTime.now(), LocalDateTime.now()
        );

        authenticatedUser = User.builder()
                .id(2L)
                .email("estagiario@email.com")
                .role(UserRole.INTERN)
                .build();
                
        principal = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
    }

    @Test
    @DisplayName("POST /processes - Deve retornar 201 Created ao registar processo")
    void registerShouldReturn201() throws Exception {
        when(processService.register(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/processes")
                .with(authentication(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.processNumber").value("0001112222026"))
                .andExpect(jsonPath("$.city").value("Recife"))
                .andExpect(jsonPath("$.currentStatus").value("INITIAL"));
    }

    @Test
    @DisplayName("GET /processes - Deve retornar 200 OK com página de processos")
    void getAllShouldReturn200() throws Exception {
        when(processService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(responseDto)));

        mockMvc.perform(get("/processes")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].processNumber").value("0001112222026"));
    }

    @Test
    @DisplayName("GET /processes/{id} - Deve retornar 200 OK e o processo")
    void findByIdShouldReturn200() throws Exception {
        when(processService.findById(any(), any())).thenReturn(responseDto);

        mockMvc.perform(get("/processes/1")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.processNumber").value("0001112222026"));
    }

    @Test
    @DisplayName("GET /processes/associate/{associateId} - Deve retornar 200 OK com lista")
    void getByAssociateShouldReturn200() throws Exception {
        when(processService.findByAssociate(any())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/processes/associate/1")
                .with(authentication(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].processNumber").value("0001112222026"));
    }

    @Test
    @DisplayName("PUT /processes/{id} - Deve retornar 200 OK")
    void updateShouldReturn200() throws Exception {
        when(processService.update(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(put("/processes/1")
                .with(authentication(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.processNumber").value("0001112222026"));
    }

    @Test
    @DisplayName("PATCH /processes/{id}/status - Deve retornar 200 OK")
    void updateStatusShouldReturn200() throws Exception {
        when(processService.updateStatus(any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/processes/1/status")
                .param("status", "INSTRUCTION")
                .with(authentication(principal)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /processes/{id} - Deve retornar 204 No Content")
    void deleteShouldReturn204() throws Exception {
        doNothing().when(processService).delete(any(), any());

        mockMvc.perform(delete("/processes/1")
                .with(authentication(principal)))
                .andExpect(status().isNoContent());
    }
}