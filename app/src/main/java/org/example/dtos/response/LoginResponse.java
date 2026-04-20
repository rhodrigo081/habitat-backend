package org.example.dtos.response;

import org.example.enums.UserRole;

public record LoginResponse(
        String token,
        UserResponse user
) {
}
