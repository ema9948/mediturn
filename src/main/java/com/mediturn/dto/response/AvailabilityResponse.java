package com.mediturn.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID doctorId,
        String doctorName,
        LocalDate date,
        List<TimeSlot> slots
) {
    public record TimeSlot(
            LocalTime startTime,
            LocalTime endTime,
            boolean available
    ) {}
}
