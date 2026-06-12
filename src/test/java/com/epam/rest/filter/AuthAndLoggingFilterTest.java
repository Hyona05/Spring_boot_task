package com.epam.rest.filter;

import com.epam.rest.entity.User;
import com.epam.rest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthAndLoggingFilter Unit Tests")
class AuthAndLoggingFilterTest {

    @Mock UserRepository        userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @InjectMocks AuthAndLoggingFilter filter;

    private MockHttpServletRequest  request;
    private MockHttpServletResponse response;
    private MockFilterChain         chain;
    private User                    activeUser;

    @BeforeEach
    void setUp() {
        request  = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain    = new MockFilterChain();

        activeUser = User.builder()
                .id(1L).username("John.Doe")
                .password("$2a$10$encodedHash")
                .isActive(true)
                .build();
    }


    @Test
    @DisplayName("Public endpoint /api/trainees/register — filter passes through without auth")
    void publicEndpoint_traineeRegister_passesThrough() throws Exception {
        request.setRequestURI("/api/trainees/register");
        request.setMethod("POST");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Public endpoint /api/trainers/register — filter passes through without auth")
    void publicEndpoint_trainerRegister_passesThrough() throws Exception {
        request.setRequestURI("/api/trainers/register");
        request.setMethod("POST");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Public endpoint /api/auth/login — filter passes through without auth")
    void publicEndpoint_login_passesThrough() throws Exception {
        request.setRequestURI("/api/auth/login");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Actuator endpoint — filter passes through without auth")
    void publicEndpoint_actuator_passesThrough() throws Exception {
        request.setRequestURI("/actuator/health");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(userRepository, passwordEncoder);
    }


    @Test
    @DisplayName("No Authorization header on protected endpoint → 401")
    void missingAuthHeader_returns401() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Authentication required");
    }

    @Test
    @DisplayName("Non-Basic Authorization header → 401")
    void bearerAuthHeader_returns401() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");
        request.addHeader("Authorization", "Bearer sometoken");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
    }


    @Test
    @DisplayName("Unknown username → 401 with Invalid credentials")
    void unknownUser_returns401() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");
        setBasicAuth(request, "ghost", "pass");

        given(userRepository.findByUsername("ghost")).willReturn(Optional.empty());

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("Wrong password → 401 (BCrypt mismatch)")  // ✅ BU TEST ASOSIY
    void wrongPassword_returns401() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");
        setBasicAuth(request, "John.Doe", "wrongPassword");

        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("wrongPassword", "$2a$10$encodedHash")).willReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid credentials");
        verify(passwordEncoder).matches("wrongPassword", "$2a$10$encodedHash");
    }

    @Test
    @DisplayName("Inactive user → 401 with User is inactive")
    void inactiveUser_returns401() throws Exception {
        User inactiveUser = User.builder()
                .username("inactive.user").password("$2a$10$hash").isActive(false).build();

        request.setRequestURI("/api/trainees/inactive.user");
        setBasicAuth(request, "inactive.user", "correctPass");

        given(userRepository.findByUsername("inactive.user")).willReturn(Optional.of(inactiveUser));
        given(passwordEncoder.matches("correctPass", "$2a$10$hash")).willReturn(true);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("User is inactive");
    }


    @Test
    @DisplayName("Correct credentials → 200 and authenticatedUser attribute set")
    void validCredentials_passesThrough() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");
        setBasicAuth(request, "John.Doe", "correctPass");

        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("correctPass", "$2a$10$encodedHash")).willReturn(true);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(request.getAttribute("authenticatedUser")).isEqualTo("John.Doe");
        verify(passwordEncoder).matches("correctPass", "$2a$10$encodedHash");
    }

    @Test
    @DisplayName("X-Transaction-Id header is always set in response")
    void transactionIdHeader_alwaysPresent() throws Exception {
        request.setRequestURI("/api/auth/login");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Transaction-Id")).isNotNull().isNotEmpty();
    }


    private void setBasicAuth(MockHttpServletRequest req, String username, String password) {
        String credentials = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        req.addHeader("Authorization", "Basic " + credentials);
    }
}
