package com.mediturn.controller;

import com.mediturn.dto.request.DoctorRequest;
import com.mediturn.dto.request.ScheduleRequest;
import com.mediturn.dto.response.DoctorResponse;
import com.mediturn.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{organizationId}/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Gestión de médicos, especialidades y horarios")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Listar médicos activos de la organización")
    public ResponseEntity<List<DoctorResponse>> findAll(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(doctorService.findAll(organizationId));
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Obtener médico por ID")
    public ResponseEntity<DoctorResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.findById(organizationId, doctorId));
    }

    @GetMapping("/by-specialty/{specialtyId}")
    @Operation(summary = "Listar médicos por especialidad")
    public ResponseEntity<List<DoctorResponse>> findBySpecialty(
            @PathVariable UUID organizationId,
            @PathVariable UUID specialtyId) {
        return ResponseEntity.ok(doctorService.findBySpecialty(organizationId, specialtyId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Registrar médico en la organización — ADMIN")
    public ResponseEntity<DoctorResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.create(organizationId, request));
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Actualizar datos del médico — ADMIN")
    public ResponseEntity<DoctorResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(doctorService.update(organizationId, doctorId, request));
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Desactivar médico (soft delete) — ADMIN")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID doctorId) {
        doctorService.delete(organizationId, doctorId);
        return ResponseEntity.noContent().build();
    }

    // ── Horarios ──────────────────────────────────────────────────────────────

    @GetMapping("/{doctorId}/schedule")
    @Operation(summary = "Obtener horarios semanales del médico")
    public ResponseEntity<List<DoctorResponse.ScheduleSlotResponse>> getSchedule(
            @PathVariable UUID organizationId,
            @PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getSchedule(organizationId, doctorId));
    }

    @PutMapping("/{doctorId}/schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_DOCTOR')")
    @Operation(summary = "Actualizar horarios semanales — ADMIN o DOCTOR")
    public ResponseEntity<List<DoctorResponse.ScheduleSlotResponse>> updateSchedule(
            @PathVariable UUID organizationId,
            @PathVariable UUID doctorId,
            @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(doctorService.updateSchedule(organizationId, doctorId, request));
    }
}
