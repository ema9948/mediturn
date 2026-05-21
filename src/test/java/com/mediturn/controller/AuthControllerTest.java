package com.mediturn.controller;

import com.mediturn.BaseIntegrationTest;
import com.mediturn.dto.request.LoginRequest;
import com.mediturn.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Auth Integration Tests")
class AuthControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /register → 201 con token")
    void register_success() throws Exception {
        var request = new RegisterRequest("Juan Pérez", "juan@test.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.email", is("juan@test.com")))
                .andExpect(jsonPath("$.fullName", is("Juan Pérez")));
    }

    @Test
    @DisplayName("POST /register → 422 si el email ya existe")
    void register_duplicateEmail() throws Exception {
        createUser("repetido@test.com", "Alguien");

        var request = new RegisterRequest("Otro", "repetido@test.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("POST /register → 400 si faltan campos requeridos")
    void register_validationError() throws Exception {
        var request = new RegisterRequest("", "no-es-email", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    @Test
    @DisplayName("POST /login → 200 con token válido")
    void login_success() throws Exception {
        createUser("login@test.com", "Test User");

        var request = new LoginRequest("login@test.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("login@test.com")));
    }

    @Test
    @DisplayName("POST /login → 401 con contraseña incorrecta")
    void login_wrongPassword() throws Exception {
        createUser("wrong@test.com", "Test User");

        var request = new LoginRequest("wrong@test.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /me → 200 con usuario autenticado")
    void me_authenticated() throws Exception {
        mockMvc.perform(asAdmin(get("/api/auth/me")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /me → 401 sin autenticación")
    void me_unauthenticated() throws Exception {
        mockMvc.perform(asAnonymous(get("/api/auth/me")))
                .andExpect(status().isUnauthorized());
    }
}
