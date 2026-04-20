package org.example.dtos.response;

import org.example.enums.CitationStatus;

import java.time.LocalDateTime;

public record ConciliationResponse (Long id,
                                    String paroppositePartyNameteContariaNome,
                                    String oppositePartyContact,
                                    LocalDateTime audienceDateTime,
                                    String summary,
                                    CitationStatus citationStatus,
                                    Long associateId,
                                    String associateNome,
                                    Long internId,
                                    String internNome,
                                    LocalDateTime createdAt){
}
