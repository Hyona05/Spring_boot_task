package com.epam.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ActivateDeactivateRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "IsActive is required")
        Boolean isActive
) {}