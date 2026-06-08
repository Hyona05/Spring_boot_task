package com.epam.rest.dto.request;


import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record TraineeRegistrationRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        LocalDate dateOfBirth,   // optional

        String address           // optional
) {}