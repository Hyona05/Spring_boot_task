package com.epam.rest;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.entity.*;
import com.epam.rest.exception.AuthException;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import com.epam.rest.service.AuthService;
import com.epam.rest.service.CredentialGenerator;
import com.epam.rest.service.GymService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private CredentialGenerator credentialGenerator;
    @Mock private AuthService authService;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks private GymService gymService;

    // ==================== CREATE ====================

    @Test
    void createTrainer_shouldReturnCredentials() {
        when(credentialGenerator.generateUsername("John", "Smith")).thenReturn("John.Smith");
        when(credentialGenerator.generatePassword()).thenReturn("pass123");
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(trainerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CredentialsResponse result = gymService.createTrainer(
                new CreateTrainerRequest("John", "Smith", "Fitness"));

        assertThat(result.username()).isEqualTo("John.Smith");
        assertThat(result.password()).isEqualTo("pass123");
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void createTrainee_shouldReturnCredentials() {
        when(credentialGenerator.generateUsername("Alex", "Brown")).thenReturn("Alex.Brown");
        when(credentialGenerator.generatePassword()).thenReturn("pass456");
        when(passwordEncoder.encode("pass456")).thenReturn("encoded");
        when(traineeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CredentialsResponse result = gymService.createTrainee(
                new CreateTraineeRequest("Alex", "Brown", LocalDate.of(2000, 1, 1), "Tashkent"));

        assertThat(result.username()).isEqualTo("Alex.Brown");
        assertThat(result.password()).isEqualTo("pass456");
        verify(traineeRepository).save(any(Trainee.class));
    }

    // ==================== AUTH ====================

    @Test
    void authenticate_shouldReturnTrue_whenValid() {
        doNothing().when(authService).authenticate("john", "pass");

        boolean result = gymService.authenticate("john", "pass");

        assertThat(result).isTrue();
    }

    @Test
    void authenticate_shouldThrow_whenInvalid() {
        doThrow(new AuthException("Invalid")).when(authService).authenticate("john", "wrong");

        assertThatThrownBy(() -> gymService.authenticate("john", "wrong"))
                .isInstanceOf(AuthException.class);
    }

    // ==================== GET PROFILE ====================

    @Test
    void getTrainerByUsername_shouldReturnTrainer() {
        User user = buildUser("john", true);
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("john", "pass");
        when(trainerRepository.findByUserUsername("john")).thenReturn(Optional.of(trainer));

        TrainerResponse result = gymService.getTrainerByUsername("john", "pass", "john");

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.specialization()).isEqualTo("Fitness");
        assertThat(result.isActive()).isTrue();
        assertThat(result.trainees()).isEmpty();
    }

    @Test
    void getTrainerByUsername_shouldThrow_whenNotFound() {
        doNothing().when(authService).authenticate("john", "pass");
        when(trainerRepository.findByUserUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.getTrainerByUsername("john", "pass", "ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Trainer not found");
    }

    @Test
    void getTraineeByUsername_shouldReturnTrainee() {
        User user = buildUser("alex", true);
        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Tashkent")
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("alex", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));

        TraineeResponse result = gymService.getTraineeByUsername("alex", "pass", "alex");

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.address()).isEqualTo("Tashkent");
        assertThat(result.isActive()).isTrue();
        assertThat(result.trainers()).isEmpty();
    }

    @Test
    void getTraineeByUsername_shouldThrow_whenNotFound() {
        doNothing().when(authService).authenticate("alex", "pass");
        when(traineeRepository.findByUserUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.getTraineeByUsername("alex", "pass", "ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Trainee not found");
    }

    // ==================== UPDATE ====================

    @Test
    void updateTrainer_shouldUpdateFields() {
        User user = buildUser("john", true);
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("john", "pass");
        when(trainerRepository.findByUserUsername("john")).thenReturn(Optional.of(trainer));

        TrainerResponse result = gymService.updateTrainer(
                new UpdateTrainerRequest("john", "pass", "Johnny", "Smith", "Yoga", true));

        assertThat(result.firstName()).isEqualTo("Johnny");
        assertThat(result.specialization()).isEqualTo("Yoga");
    }

    @Test
    void updateTrainee_shouldUpdateFields() {
        User user = buildUser("alex", true);
        Trainee trainee = Trainee.builder()
                .user(user)
                .address("Old")
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("alex", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));

        TraineeResponse result = gymService.updateTrainee(
                new UpdateTraineeRequest("alex", "pass", "Alexander", "Brown",
                        LocalDate.of(2002, 2, 2), "New address", true));

        assertThat(result.firstName()).isEqualTo("Alexander");
        assertThat(result.address()).isEqualTo("New address");
    }

    // ==================== ACTIVATE / DEACTIVATE ====================

    @Test
    void activateTrainee_shouldSetActiveTrue() {
        User user = buildUser("alex", false);
        Trainee trainee = Trainee.builder()
                .user(user)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));

        gymService.activateTrainee("admin", "pass", "alex");

        assertThat(trainee.getUser().getIsActive()).isTrue();
    }

    @Test
    void activateTrainee_shouldThrow_whenAlreadyActive() {
        User user = buildUser("alex", true);
        Trainee trainee = Trainee.builder()
                .user(user)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> gymService.activateTrainee("admin", "pass", "alex"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainee already active");
    }

    @Test
    void deactivateTrainee_shouldSetActiveFalse() {
        User user = buildUser("alex", true);
        Trainee trainee = Trainee.builder()
                .user(user)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));

        gymService.deactivateTrainee("admin", "pass", "alex");

        assertThat(trainee.getUser().getIsActive()).isFalse();
    }

    @Test
    void deactivateTrainee_shouldThrow_whenAlreadyInactive() {
        User user = buildUser("alex", false);
        Trainee trainee = Trainee.builder()
                .user(user)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> gymService.deactivateTrainee("admin", "pass", "alex"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainee already inactive");
    }

    @Test
    void activateTrainer_shouldSetActiveTrue() {
        User user = buildUser("john", false);
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(trainerRepository.findByUserUsername("john")).thenReturn(Optional.of(trainer));

        gymService.activateTrainer("admin", "pass", "john");

        assertThat(trainer.getUser().getIsActive()).isTrue();
    }

    @Test
    void deactivateTrainer_shouldThrow_whenAlreadyInactive() {
        User user = buildUser("john", false);
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(trainerRepository.findByUserUsername("john")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> gymService.deactivateTrainer("admin", "pass", "john"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainer already inactive");
    }

    // ==================== DELETE ====================

    @Test
    void deleteTrainee_shouldDeleteAndCascade() {
        User user = buildUser("alex", true);
        Trainee trainee = Trainee.builder()
                .user(user)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("admin", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));
        when(trainingRepository.findByTraineeUserUsername("alex")).thenReturn(List.of());

        gymService.deleteTrainee("admin", "pass", "alex");

        verify(trainingRepository).deleteAll(List.of());
        verify(traineeRepository).delete(trainee);
    }

    // ==================== TRAINING ====================

    @Test
    void addTraining_shouldSaveTraining() {
        User tUser = buildUser("alex", true);
        User trUser = buildUser("john", true);
        TrainingType type = TrainingType.builder().trainingTypeName("Fitness").build();
        Trainee trainee = Trainee.builder()
                .user(tUser)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();
        Trainer trainer = Trainer.builder()
                .user(trUser)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("john", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername("john")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName("Fitness")).thenReturn(Optional.of(type));
        when(trainingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        gymService.addTraining("john", "pass",
                new AddTrainingRequest("alex", "john", "Morning Yoga", "Fitness",
                        LocalDate.of(2026, 5, 18), 60));

        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void addTraining_shouldThrow_whenTraineeNotFound() {
        doNothing().when(authService).authenticate("john", "pass");
        when(traineeRepository.findByUserUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.addTraining("john", "pass",
                new AddTrainingRequest("ghost", "john", "Yoga", "Fitness",
                        LocalDate.of(2026, 5, 18), 60)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Trainee not found");
    }

    @Test
    void addTraining_shouldThrow_whenTrainerNotFound() {
        User tUser = buildUser("alex", true);
        Trainee trainee = Trainee.builder()
                .user(tUser)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("john", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.addTraining("john", "pass",
                new AddTrainingRequest("alex", "ghost", "Yoga", "Fitness",
                        LocalDate.of(2026, 5, 18), 60)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Trainer not found");
    }

    // ==================== CHANGE PASSWORD ====================

    @Test
    void changePassword_shouldEncodeAndSave() {
        User user = buildUser("john", true);

        doNothing().when(authService).authenticate("john", "oldPass");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        gymService.changePassword(new ChangePasswordRequest("john", "oldPass", "newPass"));

        assertThat(user.getPassword()).isEqualTo("encodedNew");
        verify(passwordEncoder).encode("newPass");
    }

    // ==================== NOT ASSIGNED TRAINERS ====================

    @Test
    void getNotAssignedTrainers_shouldReturnUnassigned() {
        User tUser = buildUser("alex", true);
        User trUser = buildUser("john", true);

        Trainer trainer = Trainer.builder()
                .user(trUser)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        Trainee trainee = Trainee.builder()
                .user(tUser)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("alex", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<TrainerResponse> result = gymService.getNotAssignedTrainers("alex", "pass", "alex");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).specialization()).isEqualTo("Fitness");
    }

    // ==================== UPDATE TRAINERS LIST ====================

    @Test
    void updateTraineeTrainers_shouldUpdateList() {
        User tUser = buildUser("alex", true);
        User trUser = buildUser("john", true);

        Trainee trainee = Trainee.builder()
                .user(tUser)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        Trainer trainer = Trainer.builder()
                .user(trUser)
                .specialization("Fitness")
                .trainees(new HashSet<>())
                .build();

        doNothing().when(authService).authenticate("alex", "pass");
        when(traineeRepository.findByUserUsername("alex")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername("john")).thenReturn(Optional.of(trainer));

        TraineeResponse result = gymService.updateTraineeTrainers("alex", "pass",
                new UpdateTraineeTrainersRequest("alex", List.of("john")));

        assertThat(result.trainers()).hasSize(1);
        assertThat(result.trainers().get(0).username()).isEqualTo("john");
    }

    // ==================== HELPER ====================

    private User buildUser(String username, boolean isActive) {
        return User.builder()
                .firstName("John")
                .lastName("Smith")
                .username(username)
                .password("encoded")
                .isActive(isActive)
                .build();
    }
}