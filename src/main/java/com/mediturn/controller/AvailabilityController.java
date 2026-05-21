package com.mediturn.controller;

import com.mediturn.dto.response.AvailabilityResponse;
import com.mediturn.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{organizationId}/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Consulta de disponibilidad de médicos y especialidades")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/doctors/{doctorId}")
    @Operation(summary = "Slots disponibles de un médico para una fecha")
    public ResponseEntity<AvailabilityResponse> getDoctorAvailability(
            @PathVariable UUID organizationId,
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                availabilityService.getDoctorAvailability(organizationId, doctorId, date)
        );
    }

    @GetMapping("/specialties/{specialtyId}")
    @Operation(summary = "Médicos disponibles para una especialidad en una fecha")
    public ResponseEntity<List<AvailabilityResponse>> getBySpecialty(
            @PathVariable UUID organizationId,
            @PathVariable UUID specialtyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                availabilityService.getAvailabilityBySpecialty(organizationId, specialtyId, date)
        );
    }
}
