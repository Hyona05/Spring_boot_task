package com.epam.rest.service;

import com.epam.rest.dto.request.ActivateDeactivateRequest;
import com.epam.rest.dto.request.TrainerRegistrationRequest;
import com.epam.rest.dto.request.UpdateTrainerRequest;
import com.epam.rest.dto.response.RegistrationResponse;
import com.epam.rest.dto.response.TrainerProfileResponse;
import com.epam.rest.dto.response.TrainingResponse;
import com.epam.rest.dto.response.UpdateTrainerResponse;

import java.time.LocalDate;
import java.util.List;

public interface TrainerService {
    RegistrationResponse register(TrainerRegistrationRequest request);
    TrainerProfileResponse getProfile(String username);
    UpdateTrainerResponse updateProfile(UpdateTrainerRequest request);
    List<TrainingResponse> getTrainings(String username, LocalDate from, LocalDate to, String traineeName);
    void activate(ActivateDeactivateRequest request);
}