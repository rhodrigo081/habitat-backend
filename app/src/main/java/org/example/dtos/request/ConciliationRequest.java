package org.example.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.enums.CitationStatus;

import java.time.LocalDateTime;

public record ConciliationRequest(@NotBlank(message = "Nome da parte contrária é obrigatório") String oppositePartyName,

                                  String oppositePartyContact,

                                  LocalDateTime audienceDateTime,

                                  String summary,

                                  @NotNull(message = "Status de citação é obrigatório") CitationStatus citationStatus,

                                  @NotNull(message = "ID do associado é obrigatório") Long associateId) {
}
