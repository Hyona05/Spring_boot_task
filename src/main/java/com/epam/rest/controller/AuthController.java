package com.epam.rest.controller;

import com.epam.rest.dto.request.ChangeLoginRequest;
import com.epam.rest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User login with username and password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestParam String password) {
        authService.login(username, password);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change user password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "401", description = "Old password incorrect"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/change-login")
    public ResponseEntity<Void> changeLogin(
            @Valid @RequestBody ChangeLoginRequest request) {
        authService.changeLogin(request);
        return ResponseEntity.ok().build();
    }
}