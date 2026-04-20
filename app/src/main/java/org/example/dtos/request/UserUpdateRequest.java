package org.example.dtos.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String name,

        @Email(message = "Email inválido")
        String email,

        @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
        String password,

        Long coordinatorId,

        Boolean status
) {}