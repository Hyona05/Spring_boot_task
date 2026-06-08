package com.epam.rest.controller;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.service.TraineeService;
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
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee", description = "Trainee management endpoints")
public class TraineeController {

    private final TraineeService traineeService;

    @Operation(summary = "Register a new trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(traineeService.register(request));
    }

    @Operation(summary = "Get trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @Parameter(description = "Trainee username") @PathVariable String username) {
        return ResponseEntity.ok(traineeService.getProfile(username));
    }

    @Operation(summary = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping
    public ResponseEntity<UpdateTraineeResponse> updateProfile(
            @Valid @RequestBody UpdateTraineeRequest request) {
        return ResponseEntity.ok(traineeService.updateProfile(request));
    }

    @Operation(summary = "Delete trainee profile (hard delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee deleted"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Trainee username") @PathVariable String username) {
        traineeService.deleteProfile(username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get active trainers not assigned to this trainee")
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerShortResponse>> getUnassignedTrainers(
            @PathVariable String username) {
        return ResponseEntity.ok(traineeService.getUnassignedActiveTrainers(username));
    }

    @Operation(summary = "Update trainee's trainer list")
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerShortResponse>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        return ResponseEntity.ok(
                traineeService.updateTrainers(username, request.trainerUsernames()));
    }

    @Operation(summary = "Get trainee trainings with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        return ResponseEntity.ok(
                traineeService.getTrainings(username, from, to, trainerName, trainingType));
    }

    @Operation(summary = "Activate or deactivate a trainee (non-idempotent)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Already in requested state"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PatchMapping("/activate")
    public ResponseEntity<Void> activate(
            @Valid @RequestBody ActivateDeactivateRequest request) {
        traineeService.activate(request);
        return ResponseEntity.ok().build();
    }
}