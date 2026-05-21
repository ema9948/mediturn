package com.mediturn.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleRequest(

        @NotNull(message = "New datetime is required")
        @Future(message = "Appointment must be in the future")
        LocalDateTime newDatetime
) {}
