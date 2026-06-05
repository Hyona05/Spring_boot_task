package com.epam.rest.dto.response;

public record CredentialsResponse(
        String username,
        String password
) {}