package com.mediturn.controller;

import com.mediturn.dto.request.PatientRequest;
import com.mediturn.dto.response.AppointmentResponse;
import com.mediturn.dto.response.PatientResponse;
import com.mediturn.service.PatientService;
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
@RequestMapping("/api/organizations/{organizationId}/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Gestión de pacientes e historial de turnos")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR')")
    @Operation(summary = "Listar pacientes de la organización")
    public ResponseEntity<List<PatientResponse>> findAll(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(patientService.findAll(organizationId));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR', 'ROLE_PATIENT')")
    @Operation(summary = "Detalle de un paciente")
    public ResponseEntity<PatientResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(patientService.findById(organizationId, patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    @Operation(summary = "Registrar paciente en la organización")
    public ResponseEntity<PatientResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.create(organizationId, request));
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    @Operation(summary = "Actualizar datos del paciente")
    public ResponseEntity<PatientResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID patientId,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.update(organizationId, patientId, request));
    }

    @GetMapping("/{patientId}/history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR', 'ROLE_PATIENT')")
    @Operation(summary = "Historial de turnos del paciente")
    public ResponseEntity<List<AppointmentResponse>> getHistory(
            @PathVariable UUID organizationId,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(patientService.getHistory(organizationId, patientId));
    }
}
