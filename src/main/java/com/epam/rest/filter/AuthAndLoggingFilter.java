package com.epam.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class AuthAndLoggingFilter extends OncePerRequestFilter {

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

        try {
            filterChain.doFilter(request, response);
            log.info("[{}] Response status: {}", transactionId, response.getStatus());
        } finally {
            MDC.clear();
        }
    }
}
