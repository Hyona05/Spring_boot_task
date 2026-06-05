package com.epam.rest.config;

import com.epam.rest.entity.TrainingType;
import com.epam.rest.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    public void run(String... args) {
        createIfNotExists("Fitness");
        createIfNotExists("Yoga");
        createIfNotExists("Cardio");
        createIfNotExists("Strength");
    }

    private void createIfNotExists(String name) {
        trainingTypeRepository.findByTrainingTypeName(name)
                .orElseGet(() -> trainingTypeRepository.save(
                        TrainingType.builder()
                                .trainingTypeName(name)
                                .build()
                ));
    }
}