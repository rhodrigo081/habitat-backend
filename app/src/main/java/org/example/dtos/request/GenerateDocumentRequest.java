package org.example.dtos.request;

import jakarta.validation.constraints.NotNull;
import org.example.enums.DocumentFormat;
import org.example.enums.DocumentType;

public record GenerateDocumentRequest(@NotNull(message = "Tipo do documento é obrigatório")
                                      DocumentType type,

                                      @NotNull(message = "Formato do documento é obrigatório")
                                      DocumentFormat format,

                                      @NotNull(message = "ID do associado é obrigatório")
                                      Long associateId,

                                      Long coordinatorId) {
}
