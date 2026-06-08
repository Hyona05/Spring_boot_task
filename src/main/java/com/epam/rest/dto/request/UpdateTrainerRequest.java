package com.epam.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update trainer profile")
public record UpdateTrainerRequest(
        @NotBlank(message = "Username is required")
        @Schema(description = "Username of the trainer", example = "jane.smith")
        String username,

        @NotBlank(message = "First name is required")
        @Schema(description = "First name", example = "Jane")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Schema(description = "Last name", example = "Smith")
        String lastName,

//        @NotBlank(message = "Specialization is required")
//        @Schema(description = "Training specialization", example = "Yoga")
//        String specialization,

        @Schema(description = "Active status", example = "true")
        Boolean isActive
) {}