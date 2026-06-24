package com.epam.rest.controller;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.filter.AuthAndLoggingFilter;
import com.epam.rest.security.JwtAuthenticationFilter;
import com.epam.rest.security.JwtService;
import com.epam.rest.security.TokenBlacklistService;
import com.epam.rest.service.TraineeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TraineeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {AuthAndLoggingFilter.class, JwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TraineeController MockMvc Tests")
class TraineeControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TraineeService traineeService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;
    @MockitoBean PasswordEncoder passwordEncoder;
    @MockitoBean AuthenticationProvider authenticationProvider;
    @MockitoBean LogoutHandler logoutHandler;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/trainees/register → 201")
    void register_returns201() throws Exception {
        var req = new TraineeRegistrationRequest("John", "Doe",
                LocalDate.of(1990, 1, 1), "Tashkent");
        given(traineeService.register(any()))
                .willReturn(new RegistrationResponse("John.Doe", "pass123"));

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Doe"))
                .andExpect(jsonPath("$.password").value("pass123"));
    }

    @Test
    @DisplayName("POST /api/trainees/register with missing firstName → 400")
    void register_missingFirstName_returns400() throws Exception {
        var req = new TraineeRegistrationRequest("", "Doe", null, null);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/trainees/{username} → 200 with profile")
    void getProfile_returns200() throws Exception {
        given(traineeService.getProfile("John.Doe"))
                .willReturn(new TraineeProfileResponse(
                        "John", "Doe", LocalDate.of(1990, 1, 1),
                        "Tashkent", true, List.of()));

        mockMvc.perform(get("/api/trainees/John.Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("DELETE /api/trainees/{username} → 200")
    void deleteProfile_returns200() throws Exception {
        willDoNothing().given(traineeService).deleteProfile("John.Doe");

        mockMvc.perform(delete("/api/trainees/John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/trainees/activate → 200")
    void activate_returns200() throws Exception {
        var req = new ActivateDeactivateRequest("John.Doe", false);
        willDoNothing().given(traineeService).activate(any());

        mockMvc.perform(patch("/api/trainees/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/trainees/{username}/trainings → 200")
    void getTrainings_returns200() throws Exception {
        given(traineeService.getTrainings(eq("John.Doe"), any(), any(), any(), any()))
                .willReturn(List.of(new TrainingResponse(
                        "Morning Yoga", LocalDate.now(), "Yoga", 60, "trainer1")));

        mockMvc.perform(get("/api/trainees/John.Doe/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"));
    }
}