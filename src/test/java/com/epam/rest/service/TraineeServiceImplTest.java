package com.epam.rest.service;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import com.epam.rest.service.impl.TraineeServiceImpl;
import com.epam.rest.service.impl.UsernamePasswordGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl Unit Tests")
class TraineeServiceImplTest {

    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @Mock TrainingRepository trainingRepository;
    @Mock UserRepository userRepository;
    @Mock UsernamePasswordGenerator generator;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @InjectMocks TraineeServiceImpl service;

    private Trainee sampleTrainee;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L).firstName("John").lastName("Doe")
                .username("John.Doe").password("encoded")
                .isActive(true).build();

        sampleTrainee = Trainee.builder()
                .id(1L).user(sampleUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Tashkent")
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();
    }


    @Test
    @DisplayName("register: should create trainee and return credentials")
    void register_success() {
        var req = new TraineeRegistrationRequest("John", "Doe",
                LocalDate.of(1990, 1, 1), "Tashkent");
        given(generator.generateUsername("John", "Doe")).willReturn("John.Doe");
        given(generator.generatePassword()).willReturn("rawPass123");
        given(passwordEncoder.encode("rawPass123")).willReturn("encoded");
        given(traineeRepository.save(any())).willReturn(sampleTrainee);

        RegistrationResponse resp = service.register(req);

        assertThat(resp.username()).isEqualTo("John.Doe");
        assertThat(resp.password()).isEqualTo("rawPass123");
        then(traineeRepository).should().save(any(Trainee.class));
    }


    @Test
    @DisplayName("getProfile: should return profile when trainee exists")
    void getProfile_found() {
        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));

        TraineeProfileResponse profile = service.getProfile("John.Doe");

        assertThat(profile.firstName()).isEqualTo("John");
        assertThat(profile.lastName()).isEqualTo("Doe");
        assertThat(profile.isActive()).isTrue();
    }

    @Test
    @DisplayName("getProfile: should throw NotFoundException when trainee missing")
    void getProfile_notFound() {
        given(traineeRepository.findByUserUsername("missing"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");
    }


    @Test
    @DisplayName("updateProfile: should update and return updated data")
    void updateProfile_success() {
        var req = new UpdateTraineeRequest("John.Doe", "Jane", "Smith",
                null, "Samarkand", false);
        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));
        given(traineeRepository.save(any())).willReturn(sampleTrainee);

        UpdateTraineeResponse resp = service.updateProfile(req);

        assertThat(resp.firstName()).isEqualTo("Jane");
        assertThat(resp.lastName()).isEqualTo("Smith");
        assertThat(resp.isActive()).isFalse();
    }


    @Test
    @DisplayName("deleteProfile: should delete trainee and cascade trainings")
    void deleteProfile_success() {
        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));
        willDoNothing().given(trainingRepository).deleteAllByTraineeUserUsername("John.Doe");
        willDoNothing().given(traineeRepository).delete(sampleTrainee);

        assertThatCode(() -> service.deleteProfile("John.Doe"))
                .doesNotThrowAnyException();

        then(trainingRepository).should().deleteAllByTraineeUserUsername("John.Doe");
        then(traineeRepository).should().delete(sampleTrainee);
    }


    @Test
    @DisplayName("activate: should change isActive status")
    void activate_success() {
        var req = new ActivateDeactivateRequest("John.Doe", false);
        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));
        given(userRepository.save(any())).willReturn(sampleUser);

        assertThatCode(() -> service.activate(req)).doesNotThrowAnyException();
        assertThat(sampleUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("activate: should throw when already in requested state")
    void activate_alreadyActive_throws() {
        var req = new ActivateDeactivateRequest("John.Doe", true); // already true
        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));

        assertThatThrownBy(() -> service.activate(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }


    @Test
    @DisplayName("getUnassignedActiveTrainers: should return trainers not in trainee's list")
    void getUnassignedActiveTrainers_success() {
        User trainerUser = User.builder().id(2L).username("trainer1")
                .firstName("T").lastName("R").isActive(true).build();
        Trainer trainer = Trainer.builder().id(2L).user(trainerUser)
                .specialization("Yoga").trainees(new HashSet<>()).build();

        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));
        given(trainerRepository.findAll()).willReturn(List.of(trainer));

        List<TrainerShortResponse> result =
                service.getUnassignedActiveTrainers("John.Doe");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("trainer1");
    }


    @Test
    @DisplayName("updateTrainers: should replace trainer set")
    void updateTrainers_success() {
        User trainerUser = User.builder().id(2L).username("trainer1")
                .firstName("T").lastName("R").isActive(true).build();
        Trainer trainer = Trainer.builder().id(2L).user(trainerUser)
                .specialization("Yoga").trainees(new HashSet<>()).build();

        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));
        given(trainerRepository.findByUserUsername("trainer1"))
                .willReturn(Optional.of(trainer));
        given(traineeRepository.save(any())).willReturn(sampleTrainee);

        List<TrainerShortResponse> result =
                service.updateTrainers("John.Doe", List.of("trainer1"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).specialization()).isEqualTo("Yoga");
    }

    @Test
    @DisplayName("updateTrainers: should throw when trainer not found")
    void updateTrainers_trainerNotFound() {
        given(traineeRepository.findByUserUsername("John.Doe"))
                .willReturn(Optional.of(sampleTrainee));
        given(trainerRepository.findByUserUsername("ghost"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTrainers("John.Doe", List.of("ghost")))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    @DisplayName("getTrainings: should return filtered list")
    void getTrainings_success() {
        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();
        User trainerUser = User.builder().username("trainer1").build();
        Trainer trainer = Trainer.builder().user(trainerUser).specialization("Yoga").build();
        Training training = Training.builder()
                .trainingName("Morning Yoga").trainingDate(LocalDate.now())
                .trainingDuration(60).trainingType(type).trainer(trainer)
                .trainee(sampleTrainee).build();

        given(trainingRepository.findTraineeTrainingsByCriteria(
                "John.Doe", null, null, null, null))
                .willReturn(List.of(training));

        List<TrainingResponse> result =
                service.getTrainings("John.Doe", null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainingName()).isEqualTo("Morning Yoga");
        assertThat(result.get(0).partnerName()).isEqualTo("trainer1");
    }
}