package org.example.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.enums.ProcessStatus;

public record ProcessRequest(@NotBlank(message = "Número do processo é obrigatório")
                             String processNumber,

                             @NotBlank(message = "Cidade é obrigatória")
                             String city,

                             @NotBlank(message = "Vara é obrigatória")
                             String court,

                             String description,

                             @NotNull(message = "Fase atual é obrigatória")
                             ProcessStatus status,

                             @NotNull(message = "ID do associado é obrigatório")
                             Long associateId) {
}
