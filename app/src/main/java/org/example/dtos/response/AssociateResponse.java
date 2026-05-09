package org.example.dtos.response;

import java.time.LocalDateTime;

public record AssociateResponse(Long id,
                                String name,
                                String cpf,
                                String address,
                                String phone,
                                String caseReport,
                                String legalGuidance,
                                String attendanceStatus,
                                String attendanceType,
                                Long coordinatorId,
                                Long internId,
                                String internName,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
}
