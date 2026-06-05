package com.epam.rest.dto.response;

import java.time.LocalDate;

public record TrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        Integer trainingDuration,
        String trainerName,
        String traineeName,
        String trainingType
) {}