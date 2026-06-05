package com.epam.rest.filter;

import com.epam.rest.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthAndLoggingFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        String uri = request.getRequestURI();
        String method = request.getMethod();

        log.info("[TXN:{}] --> {} {}", transactionId, method, uri);

        boolean isPublic = uri.contains("/api/trainers/register")
                || uri.contains("/api/trainees/register")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/h2-console");

        if (!isPublic) {
            String username = request.getHeader("username");
            String password = request.getHeader("password");

            if (username == null || password == null) {
                log.warn("[TXN:{}] Missing credentials for {} {}", transactionId, method, uri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Missing credentials\"}");
                MDC.clear();
                return;
            }

            boolean valid = userRepository.findByUsername(username)
                    .map(u -> u.getIsActive() && passwordEncoder.matches(password, u.getPassword()))
                    .orElse(false);

            if (!valid) {
                log.warn("[TXN:{}] Invalid credentials for user: {}", transactionId, username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid credentials\"}");
                MDC.clear();
                return;
            }
        }

        chain.doFilter(request, response);

        log.info("[TXN:{}] <-- {} {} | Status: {}", transactionId, method, uri, response.getStatus());
        MDC.clear();
    }
}