package com.epam.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trainee summary information")
public record TraineeShortResponse(
        @Schema(description = "Trainee username", example = "john.doe")
        String username,

        @Schema(description = "Trainee first name", example = "John")
        String firstName,

        @Schema(description = "Trainee last name", example = "Doe")
        String lastName
) {}