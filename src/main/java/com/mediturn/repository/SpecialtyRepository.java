package com.mediturn.repository;

import com.mediturn.domain.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    List<Specialty> findByOrganizationIdAndActiveTrue(UUID organizationId);

    Optional<Specialty> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
}