package org.example.dtos.response;

import java.time.LocalDateTime;

public record AssociateResponse(Long id,
                                String nome,
                                String cpf,
                                String address,
                                String phone,
                                String caseReport,
                                String legalGuidance,
                                Long internId,
                                String internNome,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
}
