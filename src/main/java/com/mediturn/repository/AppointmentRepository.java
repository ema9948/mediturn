package com.mediturn.repository;

import com.mediturn.domain.Appointment;
import com.mediturn.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Turnos de un médico en un rango de tiempo — usado para calcular disponibilidad
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.datetime >= :from
          AND a.datetime < :to
          AND a.status NOT IN :excludedStatuses
    """)
    List<Appointment> findByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses
    );

    // Turnos de un paciente
    List<Appointment> findByPatientIdOrderByDatetimeDesc(UUID patientId);

    // Agenda diaria/semanal de un médico
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.organization.id = :organizationId
          AND a.datetime >= :from
          AND a.datetime < :to
        ORDER BY a.datetime ASC
    """)
    List<Appointment> findAgenda(
            @Param("doctorId") UUID doctorId,
            @Param("organizationId") UUID organizationId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Verificar si existe un turno en ese horario exacto (evitar doble reserva)
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.datetime = :datetime
          AND a.status NOT IN ('CANCELLED')
    """)
    boolean existsConflict(
            @Param("doctorId") UUID doctorId,
            @Param("datetime") LocalDateTime datetime
    );

    Optional<Appointment> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
