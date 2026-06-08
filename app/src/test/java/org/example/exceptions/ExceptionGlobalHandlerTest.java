package org.example.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionGlobalHandlerTest {

    private ExceptionGlobalHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ExceptionGlobalHandler();
    }

    @Test
    @DisplayName("Deve retornar 404 e ErrorResponse para NotFoundException")
    void handleNotFoundExceptionShouldReturn404() {
        NotFoundException exception = new NotFoundException("Recurso não encontrado");
        
        ResponseEntity<ExceptionGlobalHandler.ErrorResponse> response = exceptionHandler.handleNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recurso não encontrado", response.getBody().message());
        assertEquals(404, response.getBody().error());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Deve retornar 400 e ErrorResponse para InvalidArgumentException")
    void handleInvalidArgumentExceptionShouldReturn400() {
        InvalidArgumentException exception = new InvalidArgumentException("Argumento inválido");
        
        ResponseEntity<ExceptionGlobalHandler.ErrorResponse> response = exceptionHandler.handleInvalidArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().message());
        assertEquals(400, response.getBody().error());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Deve retornar 403 e ErrorResponse para UnauthorizedAccessException")
    void handleUnauthorizedAccessExceptionShouldReturn403() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Acesso negado");
        
        ResponseEntity<ExceptionGlobalHandler.ErrorResponse> response = exceptionHandler.handleUnauthorizedAccessException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Acesso negado", response.getBody().message());
        assertEquals(403, response.getBody().error());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Deve retornar 401 e ErrorResponse para InvalidCredentialsExceptions")
    void handleInvalidCredentialsExceptionShouldReturn401() {
        InvalidCredentialsExceptions exception = new InvalidCredentialsExceptions("Credenciais inválidas");
        
        ResponseEntity<ExceptionGlobalHandler.ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credenciais inválidas", response.getBody().message());
        assertEquals(401, response.getBody().error());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Deve retornar 500 e ErrorResponse para Exception genérica")
    void handleInternalErrorShouldReturn500() {
        Exception exception = new Exception("Erro interno no servidor");
        
        ResponseEntity<ExceptionGlobalHandler.ErrorResponse> response = exceptionHandler.handleInternalError(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro interno no servidor", response.getBody().message());
        assertEquals(500, response.getBody().error());
        assertNotNull(response.getBody().timestamp());
    }
}