package org.example.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidArgumentExceptionTest {

    @Test
    @DisplayName("Deve inicializar a exceção com a mensagem correta")
    void shouldInitializeWithMessage() {

        String errorMessage = "O argumento fornecido é inválido";

        InvalidArgumentException exception = new InvalidArgumentException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }
}