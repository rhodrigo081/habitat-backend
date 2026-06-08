package org.example.dtos.response;

import org.example.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        String coordinatorName,
        Boolean status,
        LocalDateTime createdAt) {
}