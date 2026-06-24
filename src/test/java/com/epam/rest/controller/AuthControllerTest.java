package com.epam.rest.controller;

import com.epam.rest.dto.request.ChangeLoginRequest;
import com.epam.rest.dto.request.LoginRequest;
import com.epam.rest.filter.AuthAndLoggingFilter;
import com.epam.rest.security.JwtAuthenticationFilter;
import com.epam.rest.security.JwtService;
import com.epam.rest.security.LoginAttemptService;
import com.epam.rest.security.TokenBlacklistService;
import com.epam.rest.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {AuthAndLoggingFilter.class, JwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean LoginAttemptService loginAttemptService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;
    @MockitoBean AuthenticationProvider authenticationProvider;
    @MockitoBean LogoutHandler logoutHandler;
    @MockitoBean PasswordEncoder passwordEncoder;

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "rawPass";
    private static final String FAKE_TOKEN = "header.payload.signature";

    @Test
    @DisplayName("POST /login → 200 with JWT token when credentials are valid")
    void login_validCredentials_returns200WithToken() throws Exception {
        var userDetails = new User(USERNAME, "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        given(loginAttemptService.isBlocked(USERNAME)).willReturn(false);
        given(authenticationManager.authenticate(any()))
                .willReturn(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));
        given(userDetailsService.loadUserByUsername(USERNAME)).willReturn(userDetails);
        given(jwtService.generateToken(userDetails)).willReturn(FAKE_TOKEN);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(USERNAME, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(FAKE_TOKEN));
    }

    @Test
    @DisplayName("POST /login → 401 on wrong password")
    void login_wrongPassword_returns401() throws Exception {
        given(loginAttemptService.isBlocked(USERNAME)).willReturn(false);
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(USERNAME, "wrong"))))
                .andExpect(status().isUnauthorized());

        then(loginAttemptService).should().loginFailed(USERNAME);
    }

    @Test
    @DisplayName("POST /login → 429 when user is blocked")
    void login_blockedUser_returns429() throws Exception {
        given(loginAttemptService.isBlocked(USERNAME)).willReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(USERNAME, PASSWORD))))
                .andExpect(status().isTooManyRequests());

        then(authenticationManager).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("POST /login → 400 when username is blank")
    void login_blankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("", PASSWORD))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login → 400 when password is blank")
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(USERNAME, ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login → 400 when body is empty JSON")
    void login_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login → loginSucceeded called on success")
    void login_success_callsLoginSucceeded() throws Exception {
        var userDetails = new User(USERNAME, "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        given(loginAttemptService.isBlocked(USERNAME)).willReturn(false);
        given(authenticationManager.authenticate(any()))
                .willReturn(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));
        given(userDetailsService.loadUserByUsername(USERNAME)).willReturn(userDetails);
        given(jwtService.generateToken(userDetails)).willReturn(FAKE_TOKEN);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(USERNAME, PASSWORD))))
                .andExpect(status().isOk());

        then(loginAttemptService).should().loginSucceeded(USERNAME);
    }

    @Test
    @DisplayName("PUT /change-login → 200 on valid request")
    void changeLogin_valid_returns200() throws Exception {
        willDoNothing().given(authService).changeLogin(any());

        mockMvc.perform(put("/api/auth/change-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangeLoginRequest(USERNAME, "oldPass", "newPass"))))
                .andExpect(status().isOk());

        then(authService).should().changeLogin(any());
    }

    @Test
    @DisplayName("PUT /change-login → 400 when username is blank")
    void changeLogin_blankUsername_returns400() throws Exception {
        mockMvc.perform(put("/api/auth/change-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangeLoginRequest("", "old", "new"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /change-login → 400 when body is empty")
    void changeLogin_emptyBody_returns400() throws Exception {
        mockMvc.perform(put("/api/auth/change-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}