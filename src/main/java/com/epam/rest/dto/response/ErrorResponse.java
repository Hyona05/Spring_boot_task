package com.epam.rest.dto.response;

public record ErrorResponse(
        int status,
        String message,
        String transactionId
) {
}
