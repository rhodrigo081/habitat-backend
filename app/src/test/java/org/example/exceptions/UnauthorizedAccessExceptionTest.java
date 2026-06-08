package org.example.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    @DisplayName("Deve inicializar a exceção com a mensagem correta")
    void shouldInitializeWithMessage() {

        String errorMessage = "Acesso não autorizado";

        UnauthorizedAccessException exception = new UnauthorizedAccessException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }
}