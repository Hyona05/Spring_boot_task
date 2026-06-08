package com.epam.rest.service;

import com.epam.rest.dto.request.AddTrainingRequest;
import com.epam.rest.dto.response.TrainingTypeResponse;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import com.epam.rest.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl Unit Tests")
class TrainingServiceImplTest {

    @Mock TrainingRepository trainingRepository;
    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @Mock TrainingTypeRepository trainingTypeRepository;

    @InjectMocks TrainingServiceImpl service;

    @Test
    @DisplayName("addTraining: should create training successfully")
    void addTraining_success() {
        User traineeUser = User.builder().username("trainee1").isActive(true).build();
        Trainee trainee = Trainee.builder().id(1L).user(traineeUser)
                .trainers(new HashSet<>()).trainings(new HashSet<>()).build();

        User trainerUser = User.builder().username("trainer1").isActive(true).build();
        Trainer trainer = Trainer.builder().id(2L).user(trainerUser)
                .specialization("Yoga").trainees(new HashSet<>()).build();

        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();

        var req = new AddTrainingRequest("trainee1", "trainer1",
                "Morning Session", LocalDate.now(), 60);

        given(traineeRepository.findByUserUsername("trainee1"))
                .willReturn(Optional.of(trainee));
        given(trainerRepository.findByUserUsername("trainer1"))
                .willReturn(Optional.of(trainer));
        given(trainingTypeRepository.findByTrainingTypeName("Yoga"))
                .willReturn(Optional.of(type));
        given(trainingRepository.save(any())).willReturn(new Training());

        assertThatCode(() -> service.addTraining(req)).doesNotThrowAnyException();
        then(trainingRepository).should().save(any(Training.class));
    }

    @Test
    @DisplayName("addTraining: should throw when trainee not found")
    void addTraining_traineeNotFound() {
        var req = new AddTrainingRequest("ghost", "trainer1",
                "Session", LocalDate.now(), 45);
        given(traineeRepository.findByUserUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addTraining(req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("addTraining: should throw when trainer not found")
    void addTraining_trainerNotFound() {
        User traineeUser = User.builder().username("trainee1").build();
        Trainee trainee = Trainee.builder().user(traineeUser)
                .trainers(new HashSet<>()).trainings(new HashSet<>()).build();
        var req = new AddTrainingRequest("trainee1", "ghost",
                "Session", LocalDate.now(), 45);
        given(traineeRepository.findByUserUsername("trainee1"))
                .willReturn(Optional.of(trainee));
        given(trainerRepository.findByUserUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addTraining(req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getTrainingTypes: should return all training types")
    void getTrainingTypes_success() {
        given(trainingTypeRepository.findAll()).willReturn(List.of(
                TrainingType.builder().id(1L).trainingTypeName("Yoga").build(),
                TrainingType.builder().id(2L).trainingTypeName("Cardio").build()
        ));

        List<TrainingTypeResponse> result = service.getTrainingTypes();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TrainingTypeResponse::trainingTypeName)
                .containsExactlyInAnyOrder("Yoga", "Cardio");
    }
}