package org.example.services;

import org.example.exceptions.UnauthorizedAccessException;
import org.example.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private User mockedUser;
    private String secret = "mySecretTestKey12345678901234567890";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", secret);
        mockedUser = User.builder().email("test@email.com").build();
    }

    @Test
    @DisplayName("Deve gerar um token válido para o utilizador")
    void generateTokenShouldReturnValidToken() {
        String token = tokenService.generateToken(mockedUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar um token corretamente e retornar o e-mail")
    void validateTokenShouldReturnEmailWhenTokenIsValid() {
        String token = tokenService.generateToken(mockedUser);
        String email = tokenService.validateToken(token);

        assertEquals("test@email.com", email);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedAccessException ao validar token com assinatura inválida ou malformado")
    void validateTokenShouldThrowExceptionWhenTokenIsInvalid() {
        assertThrows(UnauthorizedAccessException.class, () -> {
            tokenService.validateToken("um.token.invalido");
        });
    }
}