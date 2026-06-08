package org.example.dtos.response;
public record LoginResponse(
        String token,
        UserResponse user
) {
}
