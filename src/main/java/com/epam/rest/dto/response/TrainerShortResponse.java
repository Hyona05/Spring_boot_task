package com.epam.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trainer summary information")
public record TrainerShortResponse(
        @Schema(description = "Trainer username", example = "jane.smith")
        String username,

        @Schema(description = "Trainer first name", example = "Jane")
        String firstName,

        @Schema(description = "Trainer last name", example = "Smith")
        String lastName,

        @Schema(description = "Trainer specialization", example = "Yoga")
        String specialization
) {}