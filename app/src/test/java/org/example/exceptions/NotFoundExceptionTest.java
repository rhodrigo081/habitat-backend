package org.example.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    @DisplayName("Deve inicializar a exceção com a mensagem correta")
    void shouldInitializeWithMessage() {

        String errorMessage = "Recurso não encontrado";

        NotFoundException exception = new NotFoundException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }
}