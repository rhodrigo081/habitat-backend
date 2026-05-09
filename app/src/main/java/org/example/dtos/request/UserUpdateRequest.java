package org.example.dtos.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        String name,

        @Email(message = "Email inválido")
        String email,

        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$",
            message = "Senha deve ter pelo menos 6 caracteres, incluindo letra maiúscula, minúscula, número e caractere especial"
        )
        String password,

        Long coordinatorId,

        Boolean status
) {}