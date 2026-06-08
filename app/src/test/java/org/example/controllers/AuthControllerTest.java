package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JwtAuthenticationFilter;
import org.example.dtos.request.LoginRequest;
import org.example.dtos.response.LoginResponse;
import org.example.dtos.response.UserResponse;
import org.example.enums.UserRole;
import org.example.services.AuthService;
import org.example.services.UserDetailsServiceImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("admin@email.com", "senha123");

        UserResponse userResponse = new UserResponse(
                1L, "Admin Silva", "admin@email.com", UserRole.COORDINATOR, 
                "N/A", true, LocalDateTime.now()
        );

        loginResponse = new LoginResponse("mocked-jwt-token-aqui", userResponse);
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 200 OK e o token de autenticação")
    void loginShouldReturn200AndToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token-aqui"))
                .andExpect(jsonPath("$.user.email").value("admin@email.com"))
                .andExpect(jsonPath("$.user.role").value("COORDINATOR"));
    }
}