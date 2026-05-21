package com.mediturn.repository;

import com.mediturn.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<Patient> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
