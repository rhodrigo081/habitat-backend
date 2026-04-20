package org.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionGlobalHandler extends RuntimeException {
    public record ErrorResponse(String message, int error, String timestamp) {
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse error = new ErrorResponse(e.getMessage(), status.value(), LocalDateTime.now().toString());

        return new ResponseEntity<ErrorResponse>(error, status);
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgumentException(InvalidArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse error = new ErrorResponse(e.getMessage(), status.value(), LocalDateTime.now().toString());

        return new ResponseEntity<ErrorResponse>(error, status);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        ErrorResponse error = new ErrorResponse(e.getMessage(), status.value(), LocalDateTime.now().toString());

        return new ResponseEntity<ErrorResponse>(error, status);
    }

    @ExceptionHandler(InvalidCredentialsExceptions.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsExceptions e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse error = new ErrorResponse(e.getMessage(), status.value(), LocalDateTime.now().toString());

        return new ResponseEntity<ErrorResponse>(error, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalError(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse error = new ErrorResponse(e.getMessage(), status.value(), LocalDateTime.now().toString());

        return new ResponseEntity<ErrorResponse>(error, status);
    }

}
