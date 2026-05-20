package com.mediturn.service;

import com.mediturn.domain.*;
import com.mediturn.dto.request.DoctorRequest;
import com.mediturn.dto.request.ScheduleRequest;
import com.mediturn.dto.response.DoctorResponse;
import com.mediturn.exception.BusinessException;
import com.mediturn.exception.ResourceNotFoundException;
import com.mediturn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final SpecialtyRepository specialtyRepository;

    public List<DoctorResponse> findAll(UUID organizationId) {
        return doctorRepository
                .findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(DoctorResponse::from)
                .toList();
    }

    public DoctorResponse findById(UUID organizationId, UUID doctorId) {
        return doctorRepository
                .findByIdAndOrganizationId(doctorId, organizationId)
                .map(DoctorResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
    }

    public List<DoctorResponse> findBySpecialty(UUID organizationId, UUID specialtyId) {
        return doctorRepository
                .findByOrganizationIdAndSpecialtyId(organizationId, specialtyId)
                .stream()
                .map(DoctorResponse::from)
                .toList();
    }

    @Transactional
    public DoctorResponse create(UUID organizationId, DoctorRequest request) {
        if (doctorRepository.existsByUserIdAndOrganizationId(request.userId(), organizationId)) {
            throw new BusinessException("User is already a doctor in this organization");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        List<Specialty> specialties = resolveSpecialties(request.specialtyIds(), organizationId);

        Doctor doctor = Doctor.builder()
                .user(user)
                .organization(organization)
                .licenseNumber(request.licenseNumber())
                .specialties(specialties)
                .active(true)
                .build();

        return DoctorResponse.from(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorResponse update(UUID organizationId, UUID doctorId, DoctorRequest request) {
        Doctor doctor = doctorRepository
                .findByIdAndOrganizationId(doctorId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        List<Specialty> specialties = resolveSpecialties(request.specialtyIds(), organizationId);

        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setSpecialties(specialties);

        return DoctorResponse.from(doctorRepository.save(doctor));
    }

    @Transactional
    public void delete(UUID organizationId, UUID doctorId) {
        Doctor doctor = doctorRepository
                .findByIdAndOrganizationId(doctorId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        doctor.setActive(false);
        doctorRepository.save(doctor);
    }

    // ── Horarios ──────────────────────────────────────────────────────────────

    public List<DoctorResponse.ScheduleSlotResponse> getSchedule(UUID organizationId, UUID doctorId) {
        doctorRepository.findByIdAndOrganizationId(doctorId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        return scheduleRepository.findByDoctorId(doctorId)
                .stream()
                .map(DoctorResponse.ScheduleSlotResponse::from)
                .toList();
    }

    @Transactional
    public List<DoctorResponse.ScheduleSlotResponse> updateSchedule(
            UUID organizationId, UUID doctorId, ScheduleRequest request) {

        Doctor doctor = doctorRepository
                .findByIdAndOrganizationId(doctorId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        validateScheduleSlots(request);

        // Reemplazamos todos los horarios existentes — más simple y sin conflictos
        scheduleRepository.deleteByDoctorId(doctorId);

        List<DoctorSchedule> schedules = request.slots().stream()
                .map(slot -> DoctorSchedule.builder()
                        .doctor(doctor)
                        .dayOfWeek(slot.dayOfWeek())
                        .startTime(slot.startTime())
                        .endTime(slot.endTime())
                        .build())
                .toList();

        return scheduleRepository.saveAll(schedules)
                .stream()
                .map(DoctorResponse.ScheduleSlotResponse::from)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<Specialty> resolveSpecialties(List<UUID> specialtyIds, UUID organizationId) {
        if (specialtyIds == null || specialtyIds.isEmpty()) return List.of();

        List<Specialty> specialties = specialtyIds.stream()
                .map(id -> specialtyRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Specialty", id)))
                .toList();

        return specialties;
    }

    private void validateScheduleSlots(ScheduleRequest request) {
        for (ScheduleRequest.SlotRequest slot : request.slots()) {
            if (!slot.startTime().isBefore(slot.endTime())) {
                throw new BusinessException(
                        "Start time must be before end time for day " + slot.dayOfWeek()
                );
            }
        }
    }
}
