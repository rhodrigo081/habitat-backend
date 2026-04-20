package org.example.dtos.response;

import org.example.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(Long id,
                           String nome,
                           String email,
                           UserRole role,
                           Long coordinatorId,
                           String coordinatorNome,
                           Boolean status,
                           LocalDateTime createdAt) {
}
