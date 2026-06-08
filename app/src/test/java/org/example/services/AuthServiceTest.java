package org.example.services;

import org.example.dtos.request.LoginRequest;
import org.example.dtos.response.LoginResponse;
import org.example.enums.UserRole;
import org.example.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User mockedUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@email.com", "Senha123!");

        mockedUser = User.builder()
                .id(1L)
                .name("Usuário Teste")
                .email("test@email.com")
                .role(UserRole.COORDINATOR)
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar o token e os dados do usuário")
    void loginShouldReturnTokenAndUserResponseWhenCredentialsAreValid() {
   
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockedUser);
        when(tokenService.generateToken(mockedUser)).thenReturn("mocked-jwt-token");

  
        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertNotNull(response.user());
        assertEquals("test@email.com", response.user().email());
        assertEquals("N/A", response.user().coordinatorName());
        
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).generateToken(mockedUser);
    }
}