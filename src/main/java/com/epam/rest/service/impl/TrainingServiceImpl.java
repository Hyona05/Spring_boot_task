package com.epam.rest.service.impl;

import com.epam.rest.dto.request.AddTrainingRequest;
import com.epam.rest.dto.response.TrainingTypeResponse;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import com.epam.rest.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    @Transactional
    public void addTraining(AddTrainingRequest req) {
        Trainee trainee = traineeRepository.findByUserUsername(req.traineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + req.traineeUsername()));

        Trainer trainer = trainerRepository.findByUserUsername(req.trainerUsername())
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + req.trainerUsername()));

        TrainingType type = trainingTypeRepository
                .findByTrainingTypeName(trainer.getSpecialization())
                .orElseThrow(() -> new NotFoundException("Training type not found"));

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(req.trainingName())
                .trainingDate(req.trainingDate())
                .trainingDuration(req.trainingDuration())
                .trainingType(type)
                .build();

        trainingRepository.save(training);
        log.info("Training added: {} on {} ({} min)", req.trainingName(),
                req.trainingDate(), req.trainingDuration());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getTrainingTypes() {
        return trainingTypeRepository.findAll().stream()
                .map(t -> new TrainingTypeResponse(t.getId(), t.getTrainingTypeName()))
                .toList();
    }
}
