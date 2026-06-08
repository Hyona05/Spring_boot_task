package com.epam.rest.controller;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer", description = "Trainer management endpoints")
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "Register a new trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainerService.register(request));
    }

    @Operation(summary = "Get trainer profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @Parameter(description = "Trainer username") @PathVariable String username) {
        return ResponseEntity.ok(trainerService.getProfile(username));
    }

    @Operation(summary = "Update trainer profile")
    @PutMapping
    public ResponseEntity<UpdateTrainerResponse> updateProfile(
            @Valid @RequestBody UpdateTrainerRequest request) {
        return ResponseEntity.ok(trainerService.updateProfile(request));
    }

    @Operation(summary = "Get trainer trainings with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String traineeName) {
        return ResponseEntity.ok(
                trainerService.getTrainings(username, from, to, traineeName));
    }

    @Operation(summary = "Activate or deactivate a trainer (non-idempotent)")
    @PatchMapping("/activate")
    public ResponseEntity<Void> activate(
            @Valid @RequestBody ActivateDeactivateRequest request) {
        trainerService.activate(request);
        return ResponseEntity.ok().build();
    }
}