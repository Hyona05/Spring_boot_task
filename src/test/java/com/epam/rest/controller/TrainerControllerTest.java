package com.epam.rest.controller;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.filter.AuthAndLoggingFilter;
import com.epam.rest.repository.UserRepository;
import com.epam.rest.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrainerController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {AuthAndLoggingFilter.class}))
@DisplayName("TrainerController MockMvc Tests")
class TrainerControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    TrainerService trainerService;
    @MockitoBean
    UserRepository userRepository;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/trainers/register → 201")
    void register_returns201() throws Exception {
        var req = new TrainerRegistrationRequest("Alice", "Smith", "Yoga");
        given(trainerService.register(any()))
                .willReturn(new RegistrationResponse("Alice.Smith", "pass456"));

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alice.Smith"));
    }

    @Test
    @DisplayName("POST /api/trainers/register missing specialization → 400")
    void register_missingSpecialization_400() throws Exception {
        var req = new TrainerRegistrationRequest("Alice", "Smith", "");

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/trainers/{username} → 200")
    void getProfile_returns200() throws Exception {
        given(trainerService.getProfile("Alice.Smith"))
                .willReturn(new TrainerProfileResponse(
                        "Alice", "Smith", "Yoga", true, List.of()));

        mockMvc.perform(get("/api/trainers/Alice.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("Yoga"));
    }

    @Test
    @DisplayName("PATCH /api/trainers/activate → 200")
    void activate_returns200() throws Exception {
        var req = new ActivateDeactivateRequest("Alice.Smith", false);
        willDoNothing().given(trainerService).activate(any());

        mockMvc.perform(patch("/api/trainers/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}