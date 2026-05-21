package com.mediturn.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PatientRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        String dni,

        LocalDate birthDate,

        String phone
) {}
