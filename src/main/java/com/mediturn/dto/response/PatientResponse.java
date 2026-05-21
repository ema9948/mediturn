package com.mediturn.dto.response;

import com.mediturn.domain.Patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String fullName,
        String email,
        String dni,
        LocalDate birthDate,
        String phone
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getUser().getFullName(),
                patient.getUser().getEmail(),
                patient.getDni(),
                patient.getBirthDate(),
                patient.getPhone()
        );
    }
}
