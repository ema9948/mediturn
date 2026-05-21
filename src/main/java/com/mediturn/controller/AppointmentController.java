package com.mediturn.controller;

import com.mediturn.dto.request.AppointmentRequest;
import com.mediturn.dto.request.RescheduleRequest;
import com.mediturn.dto.response.AppointmentResponse;
import com.mediturn.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{organizationId}/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Reserva y gestión del ciclo de vida de turnos")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Historial de turnos de un paciente")
    public ResponseEntity<List<AppointmentResponse>> findByPatient(
            @PathVariable UUID organizationId,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.findByPatient(organizationId, patientId));
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_RECEPTIONIST', 'ROLE_ADMIN')")
    @Operation(summary = "Agenda del médico en un rango de fechas")
    public ResponseEntity<List<AppointmentResponse>> getAgenda(
            @PathVariable UUID organizationId,
            @RequestParam UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(appointmentService.getAgenda(organizationId, doctorId, from, to));
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Detalle de un turno")
    public ResponseEntity<AppointmentResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.findById(organizationId, appointmentId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_RECEPTIONIST', 'ROLE_ADMIN')")
    @Operation(summary = "Reservar turno")
    public ResponseEntity<AppointmentResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.create(organizationId, request));
    }

    @PatchMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_RECEPTIONIST', 'ROLE_ADMIN')")
    @Operation(summary = "Confirmar turno — DOCTOR o RECEPCIONISTA")
    public ResponseEntity<AppointmentResponse> confirm(
            @PathVariable UUID organizationId,
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.confirm(organizationId, appointmentId));
    }

    @PatchMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancelar turno")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable UUID organizationId,
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.cancel(organizationId, appointmentId));
    }

    @PatchMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_RECEPTIONIST', 'ROLE_ADMIN')")
    @Operation(summary = "Reprogramar turno")
    public ResponseEntity<AppointmentResponse> reschedule(
            @PathVariable UUID organizationId,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.reschedule(organizationId, appointmentId, request));
    }
}
