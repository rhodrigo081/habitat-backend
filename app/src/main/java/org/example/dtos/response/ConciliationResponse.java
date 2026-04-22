package org.example.dtos.response;

import org.example.enums.CitationStatus;

import java.time.LocalDateTime;

public record ConciliationResponse (Long id,
                                    String oppositePartyName,
                                    String oppositePartyContact,
                                    LocalDateTime audienceDateTime,
                                    String summary,
                                    CitationStatus citationStatus,
                                    Long associateId,
                                    String associateName,
                                    Long internId,
                                    String internName,
                                    LocalDateTime createdAt){
}
