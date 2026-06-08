package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JwtAuthenticationFilter;
import org.example.dtos.request.RegisterRequest;
import org.example.dtos.request.UserUpdateRequest;
import org.example.dtos.response.UserResponse;
import org.example.enums.UserRole;
import org.example.services.UserDetailsServiceImpl;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private RegisterRequest registerRequest;
    private UserResponse responseDto;
    private String updateRequestJson;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Estagiario Teste", "estagiario@email.com", "Senha@123", UserRole.INTERN, 2L
        );

        updateRequestJson = "{\"name\":\"Estagiario Atualizado\",\"email\":\"estagiario@email.com\",\"role\":\"INTERN\",\"coordinatorId\":2}";

          responseDto = new UserResponse(
                1L, "Estagiario Teste", "estagiario@email.com", UserRole.INTERN, 
                "Coordenador Teste", true, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /users - Deve retornar 201 Created ao registar utilizador")
    void registerShouldReturn201() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Estagiario Teste"))
                .andExpect(jsonPath("$.email").value("estagiario@email.com"));
    }

    @Test
    @DisplayName("GET /users - Deve retornar 200 OK com lista de utilizadores")
    void getAllShouldReturn200() throws Exception {
        when(userService.findAll()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Estagiario Teste"));
    }

    @Test
    @DisplayName("GET /users/{id} - Deve retornar 200 OK e o utilizador")
    void getByIdShouldReturn200() throws Exception {
        when(userService.findByIdResponse(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Estagiario Teste"));
    }

    @Test
    @DisplayName("GET /users/role/{role} - Deve retornar 200 OK com lista por cargo")
    void getByRoleShouldReturn200() throws Exception {
        when(userService.findByUserRole(UserRole.INTERN)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users/role/INTERN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].role").value("INTERN"));
    }

    @Test
    @DisplayName("GET /users/coordinator/{coordinatorId}/estagiarios - Deve retornar 200 OK")
    void getInternByCoordinatorShouldReturn200() throws Exception {
        when(userService.findInternByCoordinator(2L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users/coordinator/2/estagiarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("PUT /users/{id} - Deve retornar 200 OK")
    void updateShouldReturn200() throws Exception {
        when(userService.update(eq(1L), any(UserUpdateRequest.class))).thenReturn(responseDto);

        mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Deve retornar 204 No Content")
    void inactiveShouldReturn204() throws Exception {
        doNothing().when(userService).inactive(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}