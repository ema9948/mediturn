package com.mediturn.dto.response;

import java.util.UUID;

public record AuthResponse(
        String token,
        String type,
        UUID userId,
        String email,
        String fullName
) {
    public AuthResponse(String token, UUID userId, String email, String fullName) {
        this(token, "Bearer", userId, email, fullName);
    }
}
