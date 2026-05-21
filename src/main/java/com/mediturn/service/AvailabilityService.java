package com.mediturn.service;

import com.mediturn.domain.Appointment;
import com.mediturn.domain.DoctorSchedule;
import com.mediturn.domain.enums.AppointmentStatus;
import com.mediturn.dto.response.AvailabilityResponse;
import com.mediturn.dto.response.DoctorResponse;
import com.mediturn.exception.BusinessException;
import com.mediturn.exception.ResourceNotFoundException;
import com.mediturn.repository.AppointmentRepository;
import com.mediturn.repository.DoctorRepository;
import com.mediturn.repository.DoctorScheduleRepository;
import com.mediturn.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final SpecialtyRepository specialtyRepository;

    private static final List<AppointmentStatus> EXCLUDED_STATUSES =
            List.of(AppointmentStatus.CANCELLED);

    /**
     * Retorna los slots disponibles de un médico para una fecha.
     * El resultado se cachea en Redis por 10 minutos.
     * La clave incluye doctorId + fecha para invalidar con precisión.
     */
    @Cacheable(value = "availability", key = "#doctorId + '_' + #date")
    public AvailabilityResponse getDoctorAvailability(UUID organizationId, UUID doctorId, LocalDate date) {
        var doctor = doctorRepository.findByIdAndOrganizationId(doctorId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot query availability for past dates");
        }

        // ISO day of week: 1=Lunes ... 7=Domingo
        int dayOfWeek = date.getDayOfWeek().getValue();

        // Horario del médico para ese día de la semana
        List<DoctorSchedule> daySchedules = scheduleRepository
                .findByDoctorId(doctorId)
                .stream()
                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                .toList();

        if (daySchedules.isEmpty()) {
            return new AvailabilityResponse(doctorId, doctor.getUser().getFullName(), date, List.of());
        }

        // Turnos ya reservados ese día
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = date.atTime(LocalTime.MAX);

        List<Appointment> existing = appointmentRepository.findByDoctorAndDateRange(
                doctorId, dayStart, dayEnd, EXCLUDED_STATUSES
        );

        // Horarios ocupados como Set para búsqueda O(1)
        Set<LocalTime> occupiedSlots = existing.stream()
                .map(a -> a.getDatetime().toLocalTime())
                .collect(Collectors.toSet());

        // Obtener duración de la especialidad del médico (usamos la primera por ahora)
        int durationMinutes = doctor.getSpecialties().isEmpty()
                ? 30
                : doctor.getSpecialties().get(0).getDurationMinutes();

        // Generar todos los slots del día según el horario y marcar disponibilidad
        List<AvailabilityResponse.TimeSlot> slots = new ArrayList<>();

        for (DoctorSchedule schedule : daySchedules) {
            LocalTime cursor = schedule.getStartTime();

            while (cursor.plusMinutes(durationMinutes).compareTo(schedule.getEndTime()) <= 0) {
                LocalTime slotEnd = cursor.plusMinutes(durationMinutes);
                boolean available = !occupiedSlots.contains(cursor);

                slots.add(new AvailabilityResponse.TimeSlot(cursor, slotEnd, available));
                cursor = slotEnd;
            }
        }

        return new AvailabilityResponse(doctorId, doctor.getUser().getFullName(), date, slots);
    }

    /**
     * Retorna los médicos disponibles para una especialidad en una fecha dada.
     */
    public List<AvailabilityResponse> getAvailabilityBySpecialty(
            UUID organizationId, UUID specialtyId, LocalDate date) {

        specialtyRepository.findByIdAndOrganizationId(specialtyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", specialtyId));

        return doctorRepository
                .findByOrganizationIdAndSpecialtyId(organizationId, specialtyId)
                .stream()
                .map(doctor -> getDoctorAvailability(organizationId, doctor.getId(), date))
                .filter(a -> !a.slots().isEmpty())  // solo médicos con horario ese día
                .toList();
    }
}
