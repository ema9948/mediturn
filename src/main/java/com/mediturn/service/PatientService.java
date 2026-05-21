package com.mediturn.service;

import com.mediturn.domain.Organization;
import com.mediturn.domain.Patient;
import com.mediturn.domain.User;
import com.mediturn.dto.request.PatientRequest;
import com.mediturn.dto.response.AppointmentResponse;
import com.mediturn.dto.response.PatientResponse;
import com.mediturn.exception.BusinessException;
import com.mediturn.exception.ResourceNotFoundException;
import com.mediturn.repository.AppointmentRepository;
import com.mediturn.repository.OrganizationRepository;
import com.mediturn.repository.PatientRepository;
import com.mediturn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final AppointmentRepository appointmentRepository;

    public List<PatientResponse> findAll(UUID organizationId) {
        // Verificar que la org existe
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        return patientRepository.findAll().stream()
                .filter(p -> p.getOrganization().getId().equals(organizationId))
                .map(PatientResponse::from)
                .toList();
    }

    public PatientResponse findById(UUID organizationId, UUID patientId) {
        return patientRepository
                .findByIdAndOrganizationId(patientId, organizationId)
                .map(PatientResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
    }

    @Transactional
    public PatientResponse create(UUID organizationId, PatientRequest request) {
        if (patientRepository.existsByUserIdAndOrganizationId(request.userId(), organizationId)) {
            throw new BusinessException("User is already registered as a patient in this organization");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        Patient patient = Patient.builder()
                .user(user)
                .organization(organization)
                .dni(request.dni())
                .birthDate(request.birthDate())
                .phone(request.phone())
                .build();

        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse update(UUID organizationId, UUID patientId, PatientRequest request) {
        Patient patient = patientRepository
                .findByIdAndOrganizationId(patientId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        patient.setDni(request.dni());
        patient.setBirthDate(request.birthDate());
        patient.setPhone(request.phone());

        return PatientResponse.from(patientRepository.save(patient));
    }

    public List<AppointmentResponse> getHistory(UUID organizationId, UUID patientId) {
        patientRepository.findByIdAndOrganizationId(patientId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        return appointmentRepository
                .findByPatientIdOrderByDatetimeDesc(patientId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }
}
