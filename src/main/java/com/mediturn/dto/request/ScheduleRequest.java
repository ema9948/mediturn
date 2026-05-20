package com.mediturn.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record ScheduleRequest(

        @NotEmpty(message = "At least one schedule slot is required")
        @Valid
        List<SlotRequest> slots
) {
    public record SlotRequest(

            @NotNull
            @Min(value = 1, message = "Day must be between 1 (Monday) and 7 (Sunday)")
            @Max(value = 7, message = "Day must be between 1 (Monday) and 7 (Sunday)")
            Integer dayOfWeek,

            @NotNull(message = "Start time is required")
            LocalTime startTime,

            @NotNull(message = "End time is required")
            LocalTime endTime
    ) {}
}
