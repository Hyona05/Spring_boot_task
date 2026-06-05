package com.epam.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String username,
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {}