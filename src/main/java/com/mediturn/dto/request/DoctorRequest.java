package com.mediturn.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DoctorRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        String licenseNumber,

        List<UUID> specialtyIds
) {}
