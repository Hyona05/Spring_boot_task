package com.epam.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTrainerRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String specialization,
        Boolean isActive
) {}