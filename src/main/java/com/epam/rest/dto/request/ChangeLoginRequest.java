package com.epam.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeLoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Old password is required")
        String oldPassword,

        @NotBlank(message = "New password is required")
        String newPassword
) {}
