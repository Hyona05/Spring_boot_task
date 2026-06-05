package com.epam.rest.controller;

import com.epam.rest.dto.request.AddTrainingRequest;
import com.epam.rest.dto.request.ChangePasswordRequest;
import com.epam.rest.dto.request.CreateTraineeRequest;
import com.epam.rest.dto.request.CreateTrainerRequest;
import com.epam.rest.dto.request.UpdateTraineeRequest;
import com.epam.rest.dto.request.UpdateTraineeTrainersRequest;
import com.epam.rest.dto.request.UpdateTrainerRequest;
import com.epam.rest.dto.response.*;
import com.epam.rest.service.GymService;
import com.epam.rest.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GymController {

    private final GymService gymService;
    private final TrainingTypeService trainingTypeService;

    // ==================== REGISTRATION ====================

    @Operation(summary = "Register new trainer")
    @Tag(name = "Trainer")
    @PostMapping("/trainers/register")
    public ResponseEntity<CredentialsResponse> createTrainer(
            @Valid @RequestBody CreateTrainerRequest request) {
        return ResponseEntity.ok(gymService.createTrainer(request));
    }

    @Operation(summary = "Register new trainee")
    @Tag(name = "Trainee")
    @PostMapping("/trainees/register")
    public ResponseEntity<CredentialsResponse> createTrainee(
            @Valid @RequestBody CreateTraineeRequest request) {
        return ResponseEntity.ok(gymService.createTrainee(request));
    }

    // ==================== AUTH ====================

    @Operation(summary = "Login")
    @Tag(name = "Auth")
    @GetMapping("/auth/login")
    public ResponseEntity<Void> login(
            @RequestHeader String username,
            @RequestHeader String password) {
        gymService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password")
    @Tag(name = "Auth")
    @PutMapping("/auth/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        gymService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // ==================== TRAINER ====================

    @Operation(summary = "Get trainer profile")
    @Tag(name = "Trainer")
    @GetMapping("/trainers/{username}")
    public ResponseEntity<TrainerResponse> getTrainer(
            @RequestHeader String username,
            @RequestHeader String password,
            @PathVariable("username") String trainerUsername) {
        return ResponseEntity.ok(gymService.getTrainerByUsername(username, password, trainerUsername));
    }

    @Operation(summary = "Update trainer profile")
    @Tag(name = "Trainer")
    @PutMapping("/trainers/{username}")
    public ResponseEntity<TrainerResponse> updateTrainer(
            @RequestHeader String username,
            @RequestHeader String password,
            @Valid @RequestBody UpdateTrainerRequest request) {
        return ResponseEntity.ok(gymService.updateTrainer(request));
    }

    @Operation(summary = "Activate or deactivate trainer")
    @Tag(name = "Trainer")
    @PatchMapping("/trainers/{username}/activate")
    public ResponseEntity<Void> toggleTrainerActive(
            @PathVariable String username,
            @RequestHeader String password,
            @RequestParam Boolean isActive) {
        if (isActive) {
            gymService.activateTrainer(username, password, username);
        } else {
            gymService.deactivateTrainer(username, password, username);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get trainer trainings list")
    @Tag(name = "Trainer")
    @GetMapping("/trainers/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainerTrainings(
            @RequestHeader String username,
            @RequestHeader String password,
            @PathVariable("username") String trainerUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName) {
        return ResponseEntity.ok(gymService.getTrainerTrainings(
                username, password, trainerUsername, fromDate, toDate, traineeName));
    }

    // ==================== TRAINEE ====================

    @Operation(summary = "Get trainee profile")
    @Tag(name = "Trainee")
    @GetMapping("/trainees/{username}")
    public ResponseEntity<TraineeResponse> getTrainee(
            @RequestHeader String username,
            @RequestHeader String password,
            @PathVariable("username") String traineeUsername) {
        return ResponseEntity.ok(gymService.getTraineeByUsername(username, password, traineeUsername));
    }

    @Operation(summary = "Update trainee profile")
    @Tag(name = "Trainee")
    @PutMapping("/trainees/{username}")
    public ResponseEntity<TraineeResponse> updateTrainee(
            @RequestHeader String username,
            @RequestHeader String password,
            @Valid @RequestBody UpdateTraineeRequest request) {
        return ResponseEntity.ok(gymService.updateTrainee(request));
    }

    @Operation(summary = "Delete trainee profile")
    @Tag(name = "Trainee")
    @DeleteMapping("/trainees/{username}")
    public ResponseEntity<Void> deleteTrainee(
            @RequestHeader String username,
            @RequestHeader String password,
            @PathVariable("username") String traineeUsername) {
        gymService.deleteTrainee(username, password, traineeUsername);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate or deactivate trainee")
    @Tag(name = "Trainee")
    @PatchMapping("/trainees/{username}/activate")
    public ResponseEntity<Void> toggleTraineeActive(
            @PathVariable String username,
            @RequestHeader String password,
            @RequestParam Boolean isActive) {
        if (isActive) {
            gymService.activateTrainee(username, password, username);
        } else {
            gymService.deactivateTrainee(username, password, username);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get trainee trainings list")
    @Tag(name = "Trainee")
    @GetMapping("/trainees/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainings(
            @RequestHeader String username,
            @RequestHeader String password,
            @PathVariable("username") String traineeUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        return ResponseEntity.ok(gymService.getTraineeTrainings(
                username, password, traineeUsername, fromDate, toDate, trainerName, trainingType));
    }

    @Operation(summary = "Get unassigned active trainers for trainee")
    @Tag(name = "Trainee")
    @GetMapping("/trainees/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerResponse>> getNotAssignedTrainers(
            @RequestHeader String username,
            @RequestHeader String password,
            @PathVariable("username") String traineeUsername) {
        return ResponseEntity.ok(gymService.getNotAssignedTrainers(username, password, traineeUsername));
    }

    @Operation(summary = "Update trainee's trainer list")
    @Tag(name = "Trainee")
    @PutMapping("/trainees/{username}/trainers")
    public ResponseEntity<TraineeResponse> updateTraineeTrainers(
            @RequestHeader String username,
            @RequestHeader String password,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        return ResponseEntity.ok(gymService.updateTraineeTrainers(username, password, request));
    }

    // ==================== TRAINING ====================

    @Operation(summary = "Add training")
    @Tag(name = "Training")
    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(
            @RequestHeader String username,
            @RequestHeader String password,
            @Valid @RequestBody AddTrainingRequest request) {
        gymService.addTraining(username, password, request);
        return ResponseEntity.ok().build();
    }

    // ==================== TRAINING TYPES ====================

    @Operation(summary = "Get all training types")
    @Tag(name = "Training Types")
    @GetMapping("/training-types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(trainingTypeService.getAll());
    }
}