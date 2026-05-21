package com.mediturn.controller;

import com.mediturn.BaseIntegrationTest;
import com.mediturn.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Availability Integration Tests")
class AvailabilityControllerTest extends BaseIntegrationTest {

    private Organization org;
    private Doctor doctor;
    private Specialty specialty;
    private Patient patient;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        org = createOrganization("Hospital Test");

        User doctorUser = createUser("doctor@test.com", "Dr. García");
        specialty = createSpecialty(org, "Cardiología", 30);
        doctor    = createDoctor(doctorUser, org, specialty);

        User patientUser = createUser("paciente@test.com", "Paciente");
        patient = createPatient(patientUser, org);

        baseUrl = "/api/organizations/" + org.getId() + "/availability";
    }

    @Test
    @DisplayName("GET /availability/doctors/{id} → retorna slots del médico para el lunes")
    void getDoctorAvailability_returnsSlots() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        mockMvc.perform(asAdmin(get(baseUrl + "/doctors/" + doctor.getId())
                        .param("date", nextMonday.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId", is(doctor.getId().toString())))
                .andExpect(jsonPath("$.slots", not(empty())))
                .andExpect(jsonPath("$.slots[0].available", is(true)))
                .andExpect(jsonPath("$.slots[0].startTime", is("09:00:00")));
    }

    @Test
    @DisplayName("GET /availability/doctors/{id} → slot marcado como ocupado si hay turno")
    void getDoctorAvailability_slotTakenWhenAppointmentExists() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        createAppointment(org, patient, doctor, specialty, nextMonday.atTime(9, 0));

        mockMvc.perform(asAdmin(get(baseUrl + "/doctors/" + doctor.getId())
                        .param("date", nextMonday.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[0].startTime", is("09:00:00")))
                .andExpect(jsonPath("$.slots[0].available", is(false)))
                .andExpect(jsonPath("$.slots[1].available", is(true)));
    }

    @Test
    @DisplayName("GET /availability/doctors/{id} → lista vacía para domingo (sin horario)")
    void getDoctorAvailability_emptyOnSunday() throws Exception {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        mockMvc.perform(asAdmin(get(baseUrl + "/doctors/" + doctor.getId())
                        .param("date", nextSunday.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots", hasSize(0)));
    }

    @Test
    @DisplayName("GET /availability/doctors/{id} → 422 para fecha pasada")
    void getDoctorAvailability_pastDate() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        mockMvc.perform(asAdmin(get(baseUrl + "/doctors/" + doctor.getId())
                        .param("date", yesterday.toString())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /availability/specialties/{id} → médicos disponibles para la especialidad")
    void getBySpecialty_returnsDoctors() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        mockMvc.perform(asAdmin(get(baseUrl + "/specialties/" + specialty.getId())
                        .param("date", nextMonday.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorId", is(doctor.getId().toString())));
    }
}
