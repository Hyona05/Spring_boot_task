package com.epam.rest.service;

import com.epam.rest.dto.request.AddTrainingRequest;
import com.epam.rest.dto.response.TrainingTypeResponse;

import java.util.List;

public interface TrainingService {
    void addTraining(AddTrainingRequest request);
    List<TrainingTypeResponse> getTrainingTypes();
}