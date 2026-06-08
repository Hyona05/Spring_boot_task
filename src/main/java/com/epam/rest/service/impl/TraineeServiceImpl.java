package com.epam.rest.service.impl;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import com.epam.rest.service.TraineeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UsernamePasswordGenerator generator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegistrationResponse register(TraineeRegistrationRequest req) {
        log.debug("Registering trainee: {} {}", req.firstName(), req.lastName());

        String username = generator.generateUsername(req.firstName(), req.lastName());
        String rawPassword = generator.generatePassword();

        User user = User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .dateOfBirth(req.dateOfBirth())
                .address(req.address())
                .user(user)
                .build();

        traineeRepository.save(trainee);
        log.info("Trainee registered with username: {}", username);
        return new RegistrationResponse(username, rawPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public TraineeProfileResponse getProfile(String username) {
        Trainee trainee = findTrainee(username);
        return mapToProfile(trainee);
    }

    @Override
    @Transactional
    public UpdateTraineeResponse updateProfile(UpdateTraineeRequest req) {
        Trainee trainee = findTrainee(req.username());

        User user = trainee.getUser();
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setIsActive(req.isActive());

        trainee.setDateOfBirth(req.dateOfBirth());
        trainee.setAddress(req.address());

        traineeRepository.save(trainee);
        log.info("Trainee profile updated: {}", req.username());

        List<TrainerShortResponse> trainerList = trainee.getTrainers().stream()
                .map(this::toTrainerShort)
                .toList();

        return new UpdateTraineeResponse(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                trainee.getDateOfBirth(), trainee.getAddress(), user.getIsActive(),
                trainerList
        );
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        Trainee trainee = findTrainee(username);
        trainingRepository.deleteAllByTraineeUserUsername(username);
        traineeRepository.delete(trainee);
        log.info("Trainee deleted: {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerShortResponse> getUnassignedActiveTrainers(String traineeUsername) {
        Trainee trainee = findTrainee(traineeUsername);
        Set<Long> assignedIds = trainee.getTrainers().stream()
                .map(Trainer::getId)
                .collect(Collectors.toSet());

        return trainerRepository.findAll().stream()
                .filter(t -> !assignedIds.contains(t.getId()))
                .filter(t -> Boolean.TRUE.equals(t.getUser().getIsActive()))
                .map(this::toTrainerShort)
                .toList();
    }

    @Override
    @Transactional
    public List<TrainerShortResponse> updateTrainers(String traineeUsername,
                                                     List<String> trainerUsernames) {
        Trainee trainee = findTrainee(traineeUsername);

        Set<Trainer> newTrainers = trainerUsernames.stream()
                .map(u -> trainerRepository.findByUserUsername(u)
                        .orElseThrow(() -> new NotFoundException("Trainer not found: " + u)))
                .collect(Collectors.toSet());

        trainee.setTrainers(newTrainers);
        traineeRepository.save(trainee);

        return newTrainers.stream().map(this::toTrainerShort).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String username, LocalDate from,
                                               LocalDate to, String trainerName,
                                               String trainingType) {
        return trainingRepository
                .findTraineeTrainingsByCriteria(username, from, to, trainerName, trainingType)
                .stream()
                .map(t -> new TrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType().getTrainingTypeName(),
                        t.getTrainingDuration(),
                        t.getTrainer().getUser().getUsername()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void activate(ActivateDeactivateRequest req) {
        Trainee trainee = findTrainee(req.username());
        User user = trainee.getUser();
        if (user.getIsActive().equals(req.isActive())) {
            throw new IllegalStateException(
                    "Trainee is already " + (req.isActive() ? "active" : "inactive"));
        }
        user.setIsActive(req.isActive());
        userRepository.save(user);
        log.info("Trainee {} set isActive={}", req.username(), req.isActive());
    }

    // ── helpers ────────────────────────────────────────
    private Trainee findTrainee(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    private TraineeProfileResponse mapToProfile(Trainee t) {
        return new TraineeProfileResponse(
                t.getUser().getFirstName(),
                t.getUser().getLastName(),
                t.getDateOfBirth(),
                t.getAddress(),
                t.getUser().getIsActive(),
                t.getTrainers().stream().map(this::toTrainerShort).toList()
        );
    }

    private TrainerShortResponse toTrainerShort(Trainer tr) {
        return new TrainerShortResponse(
                tr.getUser().getUsername(),
                tr.getUser().getFirstName(),
                tr.getUser().getLastName(),
                tr.getSpecialization()
        );
    }
}