package com.epam.rest.controller;

import com.epam.rest.dto.request.AddTrainingRequest;
import com.epam.rest.dto.response.TrainingTypeResponse;
import com.epam.rest.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training", description = "Training management endpoints")
public class TrainingController {

    private final TrainingService trainingService;

    @Operation(summary = "Add a new training session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training added"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request) {
        trainingService.addTraining(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all training types")
    @ApiResponse(responseCode = "200", description = "Training types returned")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(trainingService.getTrainingTypes());
    }
}
