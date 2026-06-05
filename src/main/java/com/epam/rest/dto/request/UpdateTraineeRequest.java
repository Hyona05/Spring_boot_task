package com.epam.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UpdateTraineeRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean isActive
) {}