package com.mediturn.dto.response;

import com.mediturn.domain.Specialty;

import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        String name,
        int durationMinutes,
        boolean active
) {
    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(
                specialty.getId(),
                specialty.getName(),
                specialty.getDurationMinutes(),
                specialty.isActive()
        );
    }
}