package com.epam.rest.dto.response;

import java.util.List;

public record TrainerResponse(
        String firstName,
        String lastName,
        String specialization,
        Boolean isActive,
        List<TraineeShortResponse> trainees
) {}