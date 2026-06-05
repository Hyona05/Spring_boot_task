package com.epam.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank String trainingName,
        @NotBlank String trainingTypeName,
        @NotNull LocalDate trainingDate,
        @NotNull Integer trainingDuration
) {}