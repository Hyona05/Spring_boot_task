package com.epam.rest.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TraineeResponse(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean isActive,
        List<TrainerShortResponse> trainers
) {}