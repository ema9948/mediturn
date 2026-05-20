package com.mediturn.repository;

import com.mediturn.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findByOrganizationIdAndActiveTrue(UUID organizationId);

    Optional<Doctor> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Doctor> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    // Médicos que atienden una especialidad específica en una organización
    @Query("""
        SELECT d FROM Doctor d
        JOIN d.specialties s
        WHERE d.organization.id = :organizationId
          AND s.id = :specialtyId
          AND d.active = true
    """)
    List<Doctor> findByOrganizationIdAndSpecialtyId(
            @Param("organizationId") UUID organizationId,
            @Param("specialtyId") UUID specialtyId
    );
}
