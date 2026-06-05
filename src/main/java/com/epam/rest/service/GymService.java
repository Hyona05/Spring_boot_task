package com.epam.rest.service;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymService {

    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final CredentialGenerator credentialGenerator;
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public CredentialsResponse createTrainer(CreateTrainerRequest request) {
        String username = credentialGenerator.generateUsername(request.firstName(), request.lastName());
        String rawPassword = credentialGenerator.generatePassword();

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(request.specialization())
                .build();

        trainerRepository.save(trainer);
        log.info("Trainer created: {}", username);

        return new CredentialsResponse(username, rawPassword);
    }

    @Transactional
    public CredentialsResponse createTrainee(CreateTraineeRequest request) {
        String username = credentialGenerator.generateUsername(request.firstName(), request.lastName());
        String rawPassword = credentialGenerator.generatePassword();

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        traineeRepository.save(trainee);
        log.info("Trainee created: {}", username);

        return new CredentialsResponse(username, rawPassword);
    }

    public boolean authenticate(String username, String password) {
        authService.authenticate(username, password);
        return true;
    }


    @Transactional(readOnly = true)
    public TrainerResponse getTrainerByUsername(String authUsername, String password, String trainerUsername) {
        authService.authenticate(authUsername, password);

        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        return toTrainerResponse(trainer);
    }

    @Transactional(readOnly = true)
    public TraineeResponse getTraineeByUsername(String authUsername, String password, String traineeUsername) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        return toTraineeResponse(trainee);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        authService.authenticate(request.username(), request.oldPassword());

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        log.info("Password changed for user: {}", request.username());
    }

    @Transactional
    public TrainerResponse updateTrainer(UpdateTrainerRequest request) {
        authService.authenticate(request.username(), request.password());

        Trainer trainer = trainerRepository.findByUserUsername(request.username())
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        trainer.getUser().setFirstName(request.firstName());
        trainer.getUser().setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());

        if (request.isActive() != null) {
            trainer.getUser().setIsActive(request.isActive());
        }

        log.info("Trainer updated: {}", request.username());
        return toTrainerResponse(trainer);
    }

    @Transactional
    public TraineeResponse updateTrainee(UpdateTraineeRequest request) {
        authService.authenticate(request.username(), request.password());

        Trainee trainee = traineeRepository.findByUserUsername(request.username())
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        trainee.getUser().setFirstName(request.firstName());
        trainee.getUser().setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        if (request.isActive() != null) {
            trainee.getUser().setIsActive(request.isActive());
        }

        log.info("Trainee updated: {}", request.username());
        return toTraineeResponse(trainee);
    }

    @Transactional
    public void activateTrainee(String authUsername, String password, String traineeUsername) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        if (Boolean.TRUE.equals(trainee.getUser().getIsActive())) {
            throw new IllegalStateException("Trainee already active");
        }

        trainee.getUser().setIsActive(true);
        log.info("Trainee activated: {}", traineeUsername);
    }

    @Transactional
    public void deactivateTrainee(String authUsername, String password, String traineeUsername) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        if (Boolean.FALSE.equals(trainee.getUser().getIsActive())) {
            throw new IllegalStateException("Trainee already inactive");
        }

        trainee.getUser().setIsActive(false);
        log.info("Trainee deactivated: {}", traineeUsername);
    }

    @Transactional
    public void activateTrainer(String authUsername, String password, String trainerUsername) {
        authService.authenticate(authUsername, password);

        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        if (Boolean.TRUE.equals(trainer.getUser().getIsActive())) {
            throw new IllegalStateException("Trainer already active");
        }

        trainer.getUser().setIsActive(true);
        log.info("Trainer activated: {}", trainerUsername);
    }

    @Transactional
    public void deactivateTrainer(String authUsername, String password, String trainerUsername) {
        authService.authenticate(authUsername, password);

        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        if (Boolean.FALSE.equals(trainer.getUser().getIsActive())) {
            throw new IllegalStateException("Trainer already inactive");
        }

        trainer.getUser().setIsActive(false);
        log.info("Trainer deactivated: {}", trainerUsername);
    }

    @Transactional
    public void deleteTrainee(String authUsername, String password, String traineeUsername) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        trainee.getTrainers().forEach(trainer -> trainer.getTrainees().remove(trainee));
        trainee.getTrainers().clear();

        List<Training> trainings = trainingRepository.findByTraineeUserUsername(traineeUsername);
        trainingRepository.deleteAll(trainings);
        trainingRepository.flush();

        traineeRepository.delete(trainee);
        log.info("Trainee hard deleted: {}", traineeUsername);
    }

    @Transactional
    public void addTraining(String authUsername, String password, AddTrainingRequest request) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(request.traineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        Trainer trainer = trainerRepository.findByUserUsername(request.trainerUsername())
                .orElseThrow(() -> new NotFoundException("Trainer not found"));

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(request.trainingTypeName())
                .orElseThrow(() -> new NotFoundException("Training type not found"));

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.trainingName())
                .trainingType(trainingType)
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();

        trainingRepository.save(training);
        log.info("Training added: {}", request.trainingName());
    }

    @Transactional(readOnly = true)
    public List<TrainingResponse> getTraineeTrainings(
            String authUsername,
            String password,
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType
    ) {
        authService.authenticate(authUsername, password);

        return trainingRepository.findTraineeTrainingsByCriteria(
                        traineeUsername,
                        fromDate,
                        toDate,
                        trainerName,
                        trainingType
                )
                .stream()
                .map(this::toTrainingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainerTrainings(
            String authUsername,
            String password,
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        authService.authenticate(authUsername, password);

        return trainingRepository.findTrainerTrainingsByCriteria(
                        trainerUsername,
                        fromDate,
                        toDate,
                        traineeName
                )
                .stream()
                .map(this::toTrainingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerResponse> getNotAssignedTrainers(String authUsername, String password, String traineeUsername) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        return trainerRepository.findAll().stream()
                .filter(trainer -> !trainee.getTrainers().contains(trainer))
                .map(this::toTrainerResponse)
                .toList();
    }

    @Transactional
    public TraineeResponse updateTraineeTrainers(String authUsername, String password,
                                                 UpdateTraineeTrainersRequest request) {
        authService.authenticate(authUsername, password);

        Trainee trainee = traineeRepository.findByUserUsername(request.traineeUsername())
                .orElseThrow(() -> new NotFoundException("Trainee not found"));

        List<Trainer> trainers = request.trainerUsernames().stream()
                .map(u -> trainerRepository.findByUserUsername(u)
                        .orElseThrow(() -> new NotFoundException("Trainer not found: " + u)))
                .toList();

        trainee.setTrainers(new HashSet<>(trainers));
        log.info("Trainee trainers list updated: {}", request.traineeUsername());
        return toTraineeResponse(trainee);
    }

    private TrainerResponse toTrainerResponse(Trainer trainer) {
        List<TraineeShortResponse> trainees = trainer.getTrainees().stream()
                .map(t -> new TraineeShortResponse(
                        t.getUser().getUsername(),
                        t.getUser().getFirstName(),
                        t.getUser().getLastName()))
                .toList();

        return new TrainerResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization(),
                trainer.getUser().getIsActive(),
                trainees);
    }

    private TraineeResponse toTraineeResponse(Trainee trainee) {
        List<TrainerShortResponse> trainers = trainee.getTrainers().stream()
                .map(t -> new TrainerShortResponse(
                        t.getUser().getUsername(),
                        t.getUser().getFirstName(),
                        t.getUser().getLastName(),
                        t.getSpecialization()))
                .toList();

        return new TraineeResponse(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().getIsActive(),
                trainers);
    }

    private TrainingResponse toTrainingResponse(Training training) {
        return new TrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                training.getTrainer().getUser().getUsername(),
                training.getTrainee().getUser().getUsername(),
                training.getTrainingType().getTrainingTypeName()
        );
    }
}