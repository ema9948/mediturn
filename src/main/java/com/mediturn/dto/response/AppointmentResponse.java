package com.mediturn.dto.response;

import com.mediturn.domain.Appointment;
import com.mediturn.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        PatientInfo patient,
        DoctorInfo doctor,
        SpecialtyInfo specialty,
        LocalDateTime datetime,
        AppointmentStatus status,
        String notes,
        LocalDateTime createdAt
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                new PatientInfo(
                        a.getPatient().getId(),
                        a.getPatient().getUser().getFullName(),
                        a.getPatient().getUser().getEmail(),
                        a.getPatient().getDni()
                ),
                new DoctorInfo(
                        a.getDoctor().getId(),
                        a.getDoctor().getUser().getFullName()
                ),
                new SpecialtyInfo(
                        a.getSpecialty().getId(),
                        a.getSpecialty().getName()
                ),
                a.getDatetime(),
                a.getStatus(),
                a.getNotes(),
                a.getCreatedAt()
        );
    }

    public record PatientInfo(UUID id, String fullName, String email, String dni) {}
    public record DoctorInfo(UUID id, String fullName) {}
    public record SpecialtyInfo(UUID id, String name) {}
}
