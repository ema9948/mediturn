package com.mediturn.controller;

import com.mediturn.BaseIntegrationTest;
import com.mediturn.domain.*;
import com.mediturn.domain.enums.AppointmentStatus;
import com.mediturn.dto.request.AppointmentRequest;
import com.mediturn.dto.request.RescheduleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Appointment Integration Tests")
class AppointmentControllerTest extends BaseIntegrationTest {

    private Organization org;
    private Doctor doctor;
    private Patient patient;
    private Specialty specialty;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        org = createOrganization("Hospital Test");

        User doctorUser = createUser("doctor@test.com", "Dr. García");
        specialty = createSpecialty(org, "Cardiología", 30);
        doctor    = createDoctor(doctorUser, org, specialty);

        User patientUser = createUser("paciente@test.com", "Paciente");
        patient = createPatient(patientUser, org);

        baseUrl = "/api/organizations/" + org.getId() + "/appointments";
    }

    @Test
    @DisplayName("POST /appointments → 201 reserva exitosa")
    void create_success() throws Exception {
        var request = new AppointmentRequest(
                patient.getId(), doctor.getId(), specialty.getId(),
                nextMondayAt10(), "Primera consulta"
        );

        mockMvc.perform(asPatient(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.patient.fullName", is("Paciente")))
                .andExpect(jsonPath("$.doctor.fullName", is("Dr. García")))
                .andExpect(jsonPath("$.specialty.name", is("Cardiología")))
                .andExpect(jsonPath("$.notes", is("Primera consulta")));
    }

    @Test
    @DisplayName("POST /appointments → 422 si el slot ya está ocupado")
    void create_conflictingSlot() throws Exception {
        LocalDateTime slot = nextMondayAt10();
        createAppointment(org, patient, doctor, specialty, slot);

        User otherUser = createUser("otro@test.com", "Otro Paciente");
        Patient otherPatient = createPatient(otherUser, org);

        var request = new AppointmentRequest(
                otherPatient.getId(), doctor.getId(), specialty.getId(), slot, null
        );

        mockMvc.perform(asPatient(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    @DisplayName("POST /appointments → 422 fuera del horario del médico")
    void create_outsideSchedule() throws Exception {
        LocalDateTime sunday = nextMondayAt10().minusDays(1); // domingo

        var request = new AppointmentRequest(
                patient.getId(), doctor.getId(), specialty.getId(), sunday, null
        );

        mockMvc.perform(asPatient(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("outside doctor's schedule")));
    }

    @Test
    @DisplayName("PATCH /confirm → PENDING a CONFIRMED correctamente")
    void confirm_success() throws Exception {
        var appointment = createAppointment(org, patient, doctor, specialty, nextMondayAt10());

        mockMvc.perform(asDoctor(patch(baseUrl + "/" + appointment.getId() + "/confirm")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @DisplayName("PATCH /confirm → 422 si ya está cancelado")
    void confirm_alreadyCancelled() throws Exception {
        var appointment = createAppointment(org, patient, doctor, specialty, nextMondayAt10());
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        mockMvc.perform(asDoctor(patch(baseUrl + "/" + appointment.getId() + "/confirm")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Cannot transition")));
    }

    @Test
    @DisplayName("PATCH /cancel → cancela correctamente desde PENDING")
    void cancel_success() throws Exception {
        var appointment = createAppointment(org, patient, doctor, specialty, nextMondayAt10());

        mockMvc.perform(asPatient(patch(baseUrl + "/" + appointment.getId() + "/cancel")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("PATCH /reschedule → reprograma y vuelve a PENDING")
    void reschedule_success() throws Exception {
        var appointment = createAppointment(org, patient, doctor, specialty, nextMondayAt10());
        LocalDateTime newSlot = nextMondayAt10().plusHours(2);

        var request = new RescheduleRequest(newSlot);

        mockMvc.perform(asPatient(patch(baseUrl + "/" + appointment.getId() + "/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /patient/{id} → historial del paciente")
    void getHistory_success() throws Exception {
        createAppointment(org, patient, doctor, specialty, nextMondayAt10());
        createAppointment(org, patient, doctor, specialty, nextMondayAt10().plusHours(1));

        mockMvc.perform(asPatient(get(baseUrl + "/patient/" + patient.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /agenda → agenda del médico para el rango dado")
    void getAgenda_success() throws Exception {
        LocalDateTime slot = nextMondayAt10();
        createAppointment(org, patient, doctor, specialty, slot);

        mockMvc.perform(asAdmin(get(baseUrl + "/agenda")
                        .param("doctorId", doctor.getId().toString())
                        .param("from", slot.minusHours(1).toString())
                        .param("to", slot.plusHours(2).toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /{id} → 404 si el turno no existe")
    void findById_notFound() throws Exception {
        mockMvc.perform(asAdmin(get(baseUrl + "/" + java.util.UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }
}
