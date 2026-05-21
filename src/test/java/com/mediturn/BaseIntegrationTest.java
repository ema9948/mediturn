package com.mediturn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediturn.domain.*;
import com.mediturn.domain.enums.AppointmentStatus;
import com.mediturn.domain.enums.OrganizationType;
import com.mediturn.repository.*;
import com.mediturn.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/**
 * Clase base para tests de integración.
 * Levanta el contexto completo de Spring con H2 en memoria.
 * Cada test corre en una transacción que se hace rollback al final.
 *
 * En lugar de pasar tokens JWT (que siempre cargan ROLE_USER), usamos
 * SecurityMockMvcRequestPostProcessors.user() para simular roles específicos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JwtTokenProvider jwtTokenProvider;
    @Autowired protected PasswordEncoder passwordEncoder;

    @Autowired protected UserRepository userRepository;
    @Autowired protected OrganizationRepository organizationRepository;
    @Autowired protected SpecialtyRepository specialtyRepository;
    @Autowired protected DoctorRepository doctorRepository;
    @Autowired protected DoctorScheduleRepository scheduleRepository;
    @Autowired protected PatientRepository patientRepository;
    @Autowired protected AppointmentRepository appointmentRepository;

    // ── Helpers para crear datos de prueba ────────────────────────────────────

    protected User createUser(String email, String fullName) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName(fullName)
                .active(true)
                .build());
    }

    protected Organization createOrganization(String name) {
        return organizationRepository.save(Organization.builder()
                .name(name)
                .slug(name.toLowerCase().replace(" ", "-"))
                .type(OrganizationType.HOSPITAL)
                .active(true)
                .build());
    }

    protected Specialty createSpecialty(Organization org, String name, int duration) {
        return specialtyRepository.save(Specialty.builder()
                .organization(org)
                .name(name)
                .durationMinutes(duration)
                .active(true)
                .build());
    }

    protected Doctor createDoctor(User user, Organization org, Specialty specialty) {
        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(user)
                .organization(org)
                .licenseNumber("LIC-001")
                .specialties(java.util.List.of(specialty))
                .active(true)
                .build());

        // Horario: Lunes a Viernes 9:00 - 17:00
        for (int day = 1; day <= 5; day++) {
            scheduleRepository.save(DoctorSchedule.builder()
                    .doctor(doctor)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .build());
        }
        return doctor;
    }

    protected Patient createPatient(User user, Organization org) {
        return patientRepository.save(Patient.builder()
                .user(user)
                .organization(org)
                .dni("12345678")
                .birthDate(LocalDate.of(1990, 1, 1))
                .phone("1122334455")
                .build());
    }

    protected Appointment createAppointment(Organization org, Patient patient,
                                             Doctor doctor, Specialty specialty,
                                             LocalDateTime datetime) {
        return appointmentRepository.save(Appointment.builder()
                .organization(org)
                .patient(patient)
                .doctor(doctor)
                .specialty(specialty)
                .datetime(datetime)
                .status(AppointmentStatus.PENDING)
                .build());
    }

    // ── Helpers de autenticación por rol ─────────────────────────────────────

    /** Simula usuario autenticado con un rol específico — sin JWT real */
    protected MockHttpServletRequestBuilder asRole(MockHttpServletRequestBuilder request, String role) {
        return request.with(SecurityMockMvcRequestPostProcessors.user("test@test.com").roles(role));
    }

    protected MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        return asRole(request, "ADMIN");
    }

    protected MockHttpServletRequestBuilder asDoctor(MockHttpServletRequestBuilder request) {
        return asRole(request, "DOCTOR");
    }

    protected MockHttpServletRequestBuilder asPatient(MockHttpServletRequestBuilder request) {
        return asRole(request, "PATIENT");
    }

    protected MockHttpServletRequestBuilder asReceptionist(MockHttpServletRequestBuilder request) {
        return asRole(request, "RECEPTIONIST");
    }

    // ── Helper para test de usuario sin autenticar ────────────────────────────

    protected MockHttpServletRequestBuilder asAnonymous(MockHttpServletRequestBuilder request) {
        return request.with(SecurityMockMvcRequestPostProcessors.anonymous());
    }

    // ── Próximo lunes a las 10:00 (siempre futuro y en horario) ──────────────
    protected LocalDateTime nextMondayAt10() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        return monday.atTime(10, 0);
    }
}
