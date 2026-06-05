package com.epam.rest.dto.response;

public record TrainerShortResponse(
        String username,
        String firstName,
        String lastName,
        String specialization
) {}