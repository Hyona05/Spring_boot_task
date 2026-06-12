package com.epam.rest.filter;

import com.epam.rest.entity.User;
import com.epam.rest.exception.AuthException;
import com.epam.rest.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthAndLoggingFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String TRANSACTION_ID = "transactionId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);
        response.setHeader("X-Transaction-Id", transactionId);

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.info("[{}] Incoming {} request to: {}", transactionId, method, requestURI);

        if (isPublicEndpoint(requestURI)) {
            try {
                filterChain.doFilter(request, response);
                log.info("[{}] Response status: {}", transactionId, response.getStatus());
            } finally {
                MDC.clear();
            }
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("[{}] Missing or invalid Authorization header", transactionId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication required\"}");
            MDC.clear();
            return;
        }

        try {
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                throw new AuthException("Invalid credentials format");
            }

            String username = parts[0];
            String rawPassword = parts[1];

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthException("Invalid credentials"));

            // ✅ FIX 2: Parolni BCrypt hash bilan solishtirish — BU ENG MUHIM TUZATISH
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                log.warn("[{}] Invalid password attempt for user '{}'", transactionId, username);
                throw new AuthException("Invalid credentials");
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new AuthException("User is inactive");
            }

            request.setAttribute("authenticatedUser", username);
            log.info("[{}] User '{}' authenticated successfully", transactionId, username);

            filterChain.doFilter(request, response);
            log.info("[{}] Response status: {} for user '{}'",
                    transactionId, response.getStatus(), username);

        } catch (AuthException e) {
            log.error("[{}] Authentication failed: {}", transactionId, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"error\":\"%s\",\"transactionId\":\"%s\"}",
                            e.getMessage(), transactionId)
            );
        } finally {
            MDC.clear();
        }
    }

    private boolean isPublicEndpoint(String uri) {
        return uri.equals("/api/trainees/register") ||
                uri.equals("/api/trainers/register") ||
                uri.equals("/api/auth/login") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/actuator") ||
                uri.startsWith("/h2-console");
    }
}
