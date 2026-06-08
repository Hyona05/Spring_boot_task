package com.epam.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to update trainee profile")
public record UpdateTraineeRequest(
        @NotBlank(message = "Username is required")
        @Schema(description = "Username (cannot be changed)", example = "john.doe")
        String username,

        @NotBlank(message = "First name is required")
        @Schema(description = "First name", example = "John")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Date of birth", example = "1990-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,

        @Schema(description = "Address", example = "123 Main St")
        String address,

        @NotNull(message = "isActive is required")
        @Schema(description = "Active status", example = "true")
        Boolean isActive
) {}
