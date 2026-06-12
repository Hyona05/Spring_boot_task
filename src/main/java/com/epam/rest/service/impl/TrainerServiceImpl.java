package com.epam.rest.service.impl;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.metrics.GymMetrics;
import com.epam.rest.repository.*;
import com.epam.rest.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository      trainerRepository;
    private final TraineeRepository      traineeRepository;
    private final TrainingRepository     trainingRepository;
    private final UserRepository         userRepository;
    private final UsernamePasswordGenerator generator;
    private final BCryptPasswordEncoder  passwordEncoder;
    private final GymMetrics             gymMetrics;

    @Override
    @Transactional
    public RegistrationResponse register(TrainerRegistrationRequest req) {
        log.debug("Registering trainer: {} {}", req.firstName(), req.lastName());

        String username    = generator.generateUsername(req.firstName(), req.lastName());
        String rawPassword = generator.generatePassword();

        if (traineeRepository.existsByUserUsername(username)) {
            log.warn("Registration rejected: user '{}' is already registered as a trainee", username);
            throw new IllegalStateException(
                    "User '" + username + "' is already registered as a trainee. " +
                            "A user cannot be both a trainer and a trainee.");
        }

        User user = User.builder()
                .firstName(req.firstName()).lastName(req.lastName())
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .build();

        Trainer trainer = Trainer.builder()
                .specialization(req.specialization())
                .user(user)
                .build();

        trainerRepository.save(trainer);

        gymMetrics.incrementTrainerRegistration();
        long activeCount = trainerRepository.countByUserIsActiveTrue();
        gymMetrics.setActiveTrainers((int) activeCount);

        log.info("Trainer registered with username: {}", username);
        return new RegistrationResponse(username, rawPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerProfileResponse getProfile(String username) {
        return mapToProfile(findTrainer(username));
    }

    @Override
    @Transactional
    public UpdateTrainerResponse updateProfile(UpdateTrainerRequest req) {
        Trainer trainer = findTrainer(req.username());
        User user = trainer.getUser();
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setIsActive(req.isActive());
        userRepository.save(user);
        log.info("Trainer profile updated: {}", req.username());

        List<TraineeShortResponse> traineeList = trainer.getTrainees().stream()
                .map(this::toTraineeShort).toList();

        return new UpdateTrainerResponse(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                trainer.getSpecialization(), user.getIsActive(), traineeList
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username, LocalDate from,
                                               LocalDate to, String traineeName) {
        return trainingRepository
                .findTrainerTrainingsByCriteria(username, from, to, traineeName)
                .stream()
                .map(t -> new TrainingResponse(
                        t.getTrainingName(), t.getTrainingDate(),
                        t.getTrainingType().getTrainingTypeName(),
                        t.getTrainingDuration(),
                        t.getTrainee().getUser().getUsername()))
                .toList();
    }

    @Override
    @Transactional
    public void activate(ActivateDeactivateRequest req) {
        Trainer trainer = findTrainer(req.username());
        User user = trainer.getUser();
        if (user.getIsActive().equals(req.isActive())) {
            throw new IllegalStateException(
                    "Trainer is already " + (req.isActive() ? "active" : "inactive"));
        }
        user.setIsActive(req.isActive());
        userRepository.save(user);

        long activeCount = trainerRepository.countByUserIsActiveTrue();
        gymMetrics.setActiveTrainers((int) activeCount);

        log.info("Trainer {} set isActive={}", req.username(), req.isActive());
    }

    private Trainer findTrainer(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    private TrainerProfileResponse mapToProfile(Trainer t) {
        return new TrainerProfileResponse(
                t.getUser().getFirstName(), t.getUser().getLastName(),
                t.getSpecialization(), t.getUser().getIsActive(),
                t.getTrainees().stream().map(this::toTraineeShort).toList()
        );
    }

    private TraineeShortResponse toTraineeShort(Trainee tr) {
        return new TraineeShortResponse(
                tr.getUser().getUsername(), tr.getUser().getFirstName(),
                tr.getUser().getLastName()
        );
    }
}
