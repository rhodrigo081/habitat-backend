package org.example.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConsultationRequest(
        @NotBlank(message = "Resumo é obrigatório")
        String summary,

        @NotNull(message = "Data é obrigatória")
        LocalDate date,

        @NotNull(message = "ID do associado é obrigatório")
        Long associateId
) {
}
