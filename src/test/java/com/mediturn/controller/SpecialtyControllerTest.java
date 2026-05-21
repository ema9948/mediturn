package com.mediturn.controller;

import com.mediturn.BaseIntegrationTest;
import com.mediturn.domain.Organization;
import com.mediturn.dto.request.SpecialtyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Specialty Integration Tests")
class SpecialtyControllerTest extends BaseIntegrationTest {

    private Organization org;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        org = createOrganization("Hospital Test");
        baseUrl = "/api/organizations/" + org.getId() + "/specialties";
    }

    @Test
    @DisplayName("GET /specialties → 200 lista vacía al inicio")
    void findAll_empty() throws Exception {
        mockMvc.perform(asAdmin(get(baseUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /specialties → 200 lista con especialidades creadas")
    void findAll_withData() throws Exception {
        createSpecialty(org, "Cardiología", 30);
        createSpecialty(org, "Pediatría", 20);

        mockMvc.perform(asAdmin(get(baseUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Cardiología", "Pediatría")));
    }

    @Test
    @DisplayName("POST /specialties → 201 crea especialidad correctamente")
    void create_success() throws Exception {
        var request = new SpecialtyRequest("Neurología", 45);

        mockMvc.perform(asAdmin(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Neurología")))
                .andExpect(jsonPath("$.durationMinutes", is(45)))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /specialties → 422 si el nombre ya existe en la org")
    void create_duplicateName() throws Exception {
        createSpecialty(org, "Cardiología", 30);

        var request = new SpecialtyRequest("Cardiología", 60);

        mockMvc.perform(asAdmin(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /specialties → 400 si duración es menor a 10")
    void create_invalidDuration() throws Exception {
        var request = new SpecialtyRequest("Cardiología", 5);

        mockMvc.perform(asAdmin(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /specialties/{id} → 200 actualiza correctamente")
    void update_success() throws Exception {
        var specialty = createSpecialty(org, "Cardiología", 30);
        var request = new SpecialtyRequest("Cardiología Clínica", 45);

        mockMvc.perform(asAdmin(put(baseUrl + "/" + specialty.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Cardiología Clínica")))
                .andExpect(jsonPath("$.durationMinutes", is(45)));
    }

    @Test
    @DisplayName("DELETE /specialties/{id} → 204 soft delete")
    void delete_success() throws Exception {
        var specialty = createSpecialty(org, "Cardiología", 30);

        mockMvc.perform(asAdmin(delete(baseUrl + "/" + specialty.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(asAdmin(get(baseUrl)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /specialties/{id} → 404 si no existe")
    void findById_notFound() throws Exception {
        mockMvc.perform(asAdmin(get(baseUrl + "/" + java.util.UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }
}
