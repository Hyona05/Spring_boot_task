package com.epam.rest.service;

import com.epam.rest.dto.response.TrainingTypeResponse;
import com.epam.rest.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public List<TrainingTypeResponse> getAll() {
        return trainingTypeRepository.findAll()
                .stream()
                .map(t -> new TrainingTypeResponse(t.getId(), t.getTrainingTypeName()))
                .toList();
    }
}