package org.example.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultationResponse(Long id,
                                   String summary,
                                   LocalDate date,
                                   Long internId,
                                   String internName,
                                   Long associateId,
                                   String associateName,
                                   LocalDateTime createdAt){
}
