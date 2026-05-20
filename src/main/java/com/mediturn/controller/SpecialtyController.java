package com.mediturn.controller;

import com.mediturn.dto.request.SpecialtyRequest;
import com.mediturn.dto.response.SpecialtyResponse;
import com.mediturn.service.SpecialtyService;
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
@RequestMapping("/api/organizations/{organizationId}/specialties")
@RequiredArgsConstructor
@Tag(name = "Specialties", description = "Gestión de especialidades por organización")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    @Operation(summary = "Listar especialidades activas de la organización")
    public ResponseEntity<List<SpecialtyResponse>> findAll(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(specialtyService.findAll(organizationId));
    }

    @GetMapping("/{specialtyId}")
    @Operation(summary = "Obtener especialidad por ID")
    public ResponseEntity<SpecialtyResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID specialtyId) {
        return ResponseEntity.ok(specialtyService.findById(organizationId, specialtyId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Crear especialidad — ADMIN")
    public ResponseEntity<SpecialtyResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody SpecialtyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(specialtyService.create(organizationId, request));
    }

    @PutMapping("/{specialtyId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Actualizar especialidad — ADMIN")
    public ResponseEntity<SpecialtyResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID specialtyId,
            @Valid @RequestBody SpecialtyRequest request) {
        return ResponseEntity.ok(specialtyService.update(organizationId, specialtyId, request));
    }

    @DeleteMapping("/{specialtyId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Eliminar especialidad (soft delete) — ADMIN")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID specialtyId) {
        specialtyService.delete(organizationId, specialtyId);
        return ResponseEntity.noContent().build();
    }
}
