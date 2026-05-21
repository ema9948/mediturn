package com.mediturn.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(

        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        @NotNull(message = "Specialty ID is required")
        UUID specialtyId,

        @NotNull(message = "Datetime is required")
        @Future(message = "Appointment must be in the future")
        LocalDateTime datetime,

        String notes
) {}
