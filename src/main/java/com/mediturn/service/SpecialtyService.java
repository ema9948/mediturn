package com.mediturn.service;

import com.mediturn.domain.Organization;
import com.mediturn.domain.Specialty;
import com.mediturn.dto.request.SpecialtyRequest;
import com.mediturn.dto.response.SpecialtyResponse;
import com.mediturn.exception.BusinessException;
import com.mediturn.exception.ResourceNotFoundException;
import com.mediturn.repository.OrganizationRepository;
import com.mediturn.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final OrganizationRepository organizationRepository;

    public List<SpecialtyResponse> findAll(UUID organizationId) {
        return specialtyRepository
                .findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(SpecialtyResponse::from)
                .toList();
    }

    public SpecialtyResponse findById(UUID organizationId, UUID specialtyId) {
        return specialtyRepository
                .findByIdAndOrganizationId(specialtyId, organizationId)
                .map(SpecialtyResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", specialtyId));
    }

    @Transactional
    public SpecialtyResponse create(UUID organizationId, SpecialtyRequest request) {
        if (specialtyRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
            throw new BusinessException("Specialty already exists: " + request.name());
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        Specialty specialty = Specialty.builder()
                .organization(organization)
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .active(true)
                .build();

        return SpecialtyResponse.from(specialtyRepository.save(specialty));
    }

    @Transactional
    public SpecialtyResponse update(UUID organizationId, UUID specialtyId, SpecialtyRequest request) {
        Specialty specialty = specialtyRepository
                .findByIdAndOrganizationId(specialtyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", specialtyId));

        // Verificar nombre duplicado solo si cambió
        if (!specialty.getName().equalsIgnoreCase(request.name()) &&
                specialtyRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
            throw new BusinessException("Specialty already exists: " + request.name());
        }

        specialty.setName(request.name());
        specialty.setDurationMinutes(request.durationMinutes());

        return SpecialtyResponse.from(specialtyRepository.save(specialty));
    }

    @Transactional
    public void delete(UUID organizationId, UUID specialtyId) {
        Specialty specialty = specialtyRepository
                .findByIdAndOrganizationId(specialtyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", specialtyId));

        // Soft delete — no borramos de la BD, desactivamos
        specialty.setActive(false);
        specialtyRepository.save(specialty);
    }
}
