package com.mediturn.repository;

import com.mediturn.domain.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    List<DoctorSchedule> findByDoctorId(UUID doctorId);

    void deleteByDoctorId(UUID doctorId);
}
