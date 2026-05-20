package com.mediturn.dto.response;

import com.mediturn.domain.Doctor;
import com.mediturn.domain.DoctorSchedule;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        String fullName,
        String email,
        String licenseNumber,
        boolean active,
        List<SpecialtyResponse> specialties,
        List<ScheduleSlotResponse> schedule
) {
    public static DoctorResponse from(Doctor doctor) {
        List<SpecialtyResponse> specialties = doctor.getSpecialties() == null
                ? List.of()
                : doctor.getSpecialties().stream().map(SpecialtyResponse::from).toList();

        List<ScheduleSlotResponse> schedule = doctor.getSchedules() == null
                ? List.of()
                : doctor.getSchedules().stream().map(ScheduleSlotResponse::from).toList();

        return new DoctorResponse(
                doctor.getId(),
                doctor.getUser().getFullName(),
                doctor.getUser().getEmail(),
                doctor.getLicenseNumber(),
                doctor.isActive(),
                specialties,
                schedule
        );
    }

    public record ScheduleSlotResponse(
            UUID id,
            int dayOfWeek,
            String dayName,
            LocalTime startTime,
            LocalTime endTime
    ) {
        private static final String[] DAYS = {
                "", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
        };

        public static ScheduleSlotResponse from(DoctorSchedule schedule) {
            return new ScheduleSlotResponse(
                    schedule.getId(),
                    schedule.getDayOfWeek(),
                    DAYS[schedule.getDayOfWeek()],
                    schedule.getStartTime(),
                    schedule.getEndTime()
            );
        }
    }
}
