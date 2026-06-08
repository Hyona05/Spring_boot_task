package com.epam.rest.controller;

import com.epam.rest.dto.request.ChangeLoginRequest;
import com.epam.rest.exception.AuthException;
import com.epam.rest.filter.AuthAndLoggingFilter;
import com.epam.rest.repository.UserRepository;
import com.epam.rest.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {AuthAndLoggingFilter.class}))
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    UserRepository userRepository;

    @Test
    @DisplayName("GET /api/auth/login → 200")
    void login_returns200() throws Exception {
        willDoNothing().given(authService).login("John.Doe", "rawPass");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Doe")
                        .param("password", "rawPass"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/auth/login → 401 on bad credentials")
    void login_badCredentials_401() throws Exception {
        willThrow(new AuthException("Invalid credentials"))
                .given(authService).login("John.Doe", "wrong");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Doe")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/auth/change-login → 200")
    void changeLogin_returns200() throws Exception {
        var req = new ChangeLoginRequest("John.Doe", "old", "new");
        willDoNothing().given(authService).changeLogin(any());

        mockMvc.perform(put("/api/auth/change-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
