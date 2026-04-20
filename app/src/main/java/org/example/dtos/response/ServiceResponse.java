package org.example.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServiceResponse (Long id,
                               String summary,
                               LocalDate date,
                               Long internId,
                               String internNome,
                               Long associateId,
                               String associateNome,
                               LocalDateTime createdAt){
}
