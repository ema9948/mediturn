package com.mediturn.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpecialtyRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Duration is required")
        @Min(value = 10, message = "Duration must be at least 10 minutes")
        Integer durationMinutes
) {}