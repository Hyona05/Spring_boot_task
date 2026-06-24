package com.epam.rest.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthAndLoggingFilter Unit Tests")
class AuthAndLoggingFilterTest {

    @InjectMocks AuthAndLoggingFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @Test
    @DisplayName("GET /api/trainees/John.Doe — filter passes through (auth is Spring Security's job)")
    void protectedEndpoint_passesThrough() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("POST /api/trainees/register — filter passes through")
    void publicEndpoint_register_passesThrough() throws Exception {
        request.setRequestURI("/api/trainees/register");
        request.setMethod("POST");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("POST /api/auth/login — filter passes through")
    void publicEndpoint_login_passesThrough() throws Exception {
        request.setRequestURI("/api/auth/login");
        request.setMethod("POST");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("X-Transaction-Id header is always set in response")
    void transactionIdHeader_alwaysPresent() throws Exception {
        request.setRequestURI("/api/trainees/John.Doe");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Transaction-Id"))
                .isNotNull()
                .isNotBlank();
    }

    @Test
    @DisplayName("X-Transaction-Id is a valid UUID format")
    void transactionIdHeader_isUuid() throws Exception {
        request.setRequestURI("/api/auth/login");

        filter.doFilterInternal(request, response, chain);

        String txId = response.getHeader("X-Transaction-Id");
        assertThat(txId).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        );
    }

    @Test
    @DisplayName("Each request gets unique X-Transaction-Id")
    void transactionIdHeader_uniquePerRequest() throws Exception {
        request.setRequestURI("/api/auth/login");
        filter.doFilterInternal(request, response, chain);
        String firstId = response.getHeader("X-Transaction-Id");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        request2.setRequestURI("/api/auth/login");
        filter.doFilterInternal(request2, response2, new MockFilterChain());
        String secondId = response2.getHeader("X-Transaction-Id");

        assertThat(firstId).isNotEqualTo(secondId);
    }
}
