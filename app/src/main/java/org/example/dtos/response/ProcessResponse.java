package org.example.dtos.response;

import org.example.enums.ProcessStatus;

import java.time.LocalDateTime;

public record ProcessResponse(Long id,
                              String processNumber,
                              String city,
                              String court,
                              String description,
                              ProcessStatus currentStatus,
                              Long associateId,
                              String associateName,
                              Long internId,
                              String internName,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
}
