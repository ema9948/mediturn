package com.mediturn.service;

import com.mediturn.domain.*;
import com.mediturn.domain.enums.AppointmentStatus;
import com.mediturn.dto.request.AppointmentRequest;
import com.mediturn.dto.request.RescheduleRequest;
import com.mediturn.dto.response.AppointmentResponse;
import com.mediturn.exception.BusinessException;
import com.mediturn.exception.ResourceNotFoundException;
import com.mediturn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final OrganizationRepository organizationRepository;

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<AppointmentResponse> findByPatient(UUID organizationId, UUID patientId) {
        patientRepository.findByIdAndOrganizationId(patientId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        return appointmentRepository
                .findByPatientIdOrderByDatetimeDesc(patientId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public List<AppointmentResponse> getAgenda(UUID organizationId, UUID doctorId,
                                                LocalDateTime from, LocalDateTime to) {
        return appointmentRepository
                .findAgenda(doctorId, organizationId, from, to)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    public AppointmentResponse findById(UUID organizationId, UUID appointmentId) {
        return appointmentRepository
                .findByIdAndOrganizationId(appointmentId, organizationId)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    }

    // ── Reserva ───────────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "availability", key = "#request.doctorId() + '_' + #request.datetime().toLocalDate()")
    public AppointmentResponse create(UUID organizationId, AppointmentRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        Patient patient = patientRepository
                .findByIdAndOrganizationId(request.patientId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));

        Doctor doctor = doctorRepository
                .findByIdAndOrganizationId(request.doctorId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));

        Specialty specialty = specialtyRepository
                .findByIdAndOrganizationId(request.specialtyId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", request.specialtyId()));

        // Validar que el médico atiende esa especialidad
        boolean doctorHasSpecialty = doctor.getSpecialties().stream()
                .anyMatch(s -> s.getId().equals(request.specialtyId()));
        if (!doctorHasSpecialty) {
            throw new BusinessException("Doctor does not attend this specialty");
        }

        // Validar que el slot no esté ocupado
        if (appointmentRepository.existsConflict(request.doctorId(), request.datetime())) {
            throw new BusinessException("This time slot is already taken");
        }

        // Validar que el datetime cae dentro del horario del médico
        validateWithinSchedule(doctor, request.datetime());

        Appointment appointment = Appointment.builder()
                .organization(organization)
                .patient(patient)
                .doctor(doctor)
                .specialty(specialty)
                .datetime(request.datetime())
                .status(AppointmentStatus.PENDING)
                .notes(request.notes())
                .build();

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    // ── Cambios de estado ─────────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse confirm(UUID organizationId, UUID appointmentId) {
        Appointment appointment = getAppointment(organizationId, appointmentId);
        validateTransition(appointment, AppointmentStatus.CONFIRMED);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    @CacheEvict(value = "availability", key = "#result.doctor().id() + '_' + #result.datetime().toLocalDate()")
    public AppointmentResponse cancel(UUID organizationId, UUID appointmentId) {
        Appointment appointment = getAppointment(organizationId, appointmentId);
        validateTransition(appointment, AppointmentStatus.CANCELLED);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    @CacheEvict(value = "availability", allEntries = true) // invalidamos ambas fechas
    public AppointmentResponse reschedule(UUID organizationId, UUID appointmentId,
                                          RescheduleRequest request) {
        Appointment appointment = getAppointment(organizationId, appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
                appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot reschedule a " + appointment.getStatus() + " appointment");
        }

        if (appointmentRepository.existsConflict(appointment.getDoctor().getId(), request.newDatetime())) {
            throw new BusinessException("The new time slot is already taken");
        }

        validateWithinSchedule(appointment.getDoctor(), request.newDatetime());

        appointment.setDatetime(request.newDatetime());
        appointment.setStatus(AppointmentStatus.PENDING); // vuelve a PENDING al reprogramar

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Appointment getAppointment(UUID organizationId, UUID appointmentId) {
        return appointmentRepository
                .findByIdAndOrganizationId(appointmentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    }

    private void validateTransition(Appointment appointment, AppointmentStatus target) {
        AppointmentStatus current = appointment.getStatus();

        boolean invalid = switch (target) {
            case CONFIRMED  -> current != AppointmentStatus.PENDING;
            case CANCELLED  -> current == AppointmentStatus.CANCELLED
                             || current == AppointmentStatus.COMPLETED;
            case COMPLETED  -> current != AppointmentStatus.CONFIRMED;
            default         -> true;
        };

        if (invalid) {
            throw new BusinessException(
                    "Cannot transition from " + current + " to " + target
            );
        }
    }

    private void validateWithinSchedule(Doctor doctor, LocalDateTime datetime) {
        int dayOfWeek = datetime.getDayOfWeek().getValue();
        var time = datetime.toLocalTime();

        boolean withinSchedule = doctor.getSchedules().stream()
                .anyMatch(s -> s.getDayOfWeek() == dayOfWeek
                        && !time.isBefore(s.getStartTime())
                        && time.isBefore(s.getEndTime()));

        if (!withinSchedule) {
            throw new BusinessException("Requested time is outside doctor's schedule");
        }
    }
}
