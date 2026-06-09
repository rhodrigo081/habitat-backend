package org.example.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidCredentialsExceptionsTest {

    @Test
    @DisplayName("Deve inicializar a exceção com a mensagem correta")
    void shouldInitializeWithMessage() {
        
        String errorMessage = "Credenciais inválidas fornecidas";

        InvalidCredentialsExceptions exception = new InvalidCredentialsExceptions(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }
}