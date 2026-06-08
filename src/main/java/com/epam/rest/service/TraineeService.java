package com.epam.rest.service;

import com.epam.rest.dto.request.ActivateDeactivateRequest;
import com.epam.rest.dto.request.TraineeRegistrationRequest;
import com.epam.rest.dto.request.UpdateTraineeRequest;
import com.epam.rest.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface TraineeService {
    RegistrationResponse register(TraineeRegistrationRequest request);
    TraineeProfileResponse getProfile(String username);
    UpdateTraineeResponse updateProfile(UpdateTraineeRequest request);
    void deleteProfile(String username);
    List<TrainerShortResponse> getUnassignedActiveTrainers(String traineeUsername);
    List<TrainerShortResponse> updateTrainers(String traineeUsername, List<String> trainerUsernames);
    List<TrainingResponse> getTrainings(String username, LocalDate from, LocalDate to,
                                        String trainerName, String trainingType);
    void activate(ActivateDeactivateRequest request);
}