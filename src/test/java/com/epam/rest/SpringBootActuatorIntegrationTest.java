package com.epam.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Actuator Endpoints Integration Tests")
class SpringBootActuatorIntegrationTest {

    @Autowired MockMvc mockMvc;


    @Test
    @DisplayName("GET /actuator/health → 200 with status UP")
    void actuatorHealth_returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(oneOf("UP", "DOWN")));
    }

    @Test
    @DisplayName("GET /actuator/health shows custom 'database' component")
    void actuatorHealth_showsDatabaseComponent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.database").exists())
                .andExpect(jsonPath("$.components.database.status").exists());
    }

    @Test
    @DisplayName("GET /actuator/health shows custom 'userRegistration' component")
    void actuatorHealth_showsUserRegistrationComponent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.userRegistration").exists())
                .andExpect(jsonPath("$.components.userRegistration.status").value("UP"));
    }


    @Test
    @DisplayName("GET /actuator/prometheus → 200 with prometheus text")
    void actuatorPrometheus_returns200() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    @DisplayName("GET /actuator/prometheus contains gym_trainee_registrations_total metric")
    void actuatorPrometheus_containsTraineeMetric() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("gym_trainee_registrations_total")));
    }

    @Test
    @DisplayName("GET /actuator/prometheus contains gym_active_trainees gauge")
    void actuatorPrometheus_containsActiveTraineesGauge() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("gym_active_trainees")));
    }
}
