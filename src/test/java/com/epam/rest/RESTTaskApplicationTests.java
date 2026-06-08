package com.epam.rest;

import com.epam.rest.controller.AuthController;
import com.epam.rest.controller.TraineeController;
import com.epam.rest.controller.TrainerController;
import com.epam.rest.controller.TrainingController;
import com.epam.rest.entity.Trainee;
import com.epam.rest.entity.Trainer;
import com.epam.rest.entity.User;
import com.epam.rest.repository.*;
import com.epam.rest.service.AuthService;
import com.epam.rest.service.TraineeService;
import com.epam.rest.service.TrainerService;
import com.epam.rest.service.TrainingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Spring Boot Application Context Tests")
class RESTTaskApplicationTests {

    @Autowired TraineeController  traineeController;
    @Autowired TrainerController  trainerController;
    @Autowired AuthController     authController;
    @Autowired TrainingController trainingController;

    @Autowired TraineeService  traineeService;
    @Autowired TrainerService  trainerService;
    @Autowired AuthService     authService;
    @Autowired TrainingService trainingService;

    @Autowired TraineeRepository      traineeRepository;
    @Autowired TrainerRepository      trainerRepository;
    @Autowired TrainingRepository     trainingRepository;
    @Autowired TrainingTypeRepository trainingTypeRepository;
    @Autowired UserRepository         userRepository;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("All controllers should be loaded")
    void allControllersShouldBeLoaded() {
        assertThat(traineeController).isNotNull();
        assertThat(trainerController).isNotNull();
        assertThat(authController).isNotNull();
        assertThat(trainingController).isNotNull();
    }

    @Test
    @DisplayName("All services should be loaded")
    void allServicesShouldBeLoaded() {
        assertThat(traineeService).isNotNull();
        assertThat(trainerService).isNotNull();
        assertThat(authService).isNotNull();
        assertThat(trainingService).isNotNull();
    }

    @Test
    @DisplayName("All repositories should be loaded")
    void allRepositoriesShouldBeLoaded() {
        assertThat(traineeRepository).isNotNull();
        assertThat(trainerRepository).isNotNull();
        assertThat(trainingRepository).isNotNull();
        assertThat(trainingTypeRepository).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("Task 5: One-to-One relation between User and Trainee/Trainer")
    void oneToOneRelation_userAndTrainee() {
        User user = User.builder()
                .firstName("John").lastName("Doe")
                .username("John.Doe.ctx").password("encoded")
                .isActive(true).build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .address("Tashkent")
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();

        Trainee saved = traineeRepository.save(trainee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isNotNull();
        assertThat(saved.getUser().getId()).isNotNull();
        assertThat(saved.getUser().getUsername()).isEqualTo("John.Doe.ctx");
        assertThat(userRepository.findByUsername("John.Doe.ctx")).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("Task 5: One-to-One relation between User and Trainer")
    void oneToOneRelation_userAndTrainer() {
        User user = User.builder()
                .firstName("Alice").lastName("Smith")
                .username("Alice.Smith.ctx").password("encoded")
                .isActive(true).build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization("Yoga")
                .trainees(new HashSet<>())
                .build();

        Trainer saved = trainerRepository.save(trainer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isNotNull();
        assertThat(saved.getUser().getUsername()).isEqualTo("Alice.Smith.ctx");
        assertThat(userRepository.findByUsername("Alice.Smith.ctx")).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("Task 8: Many-to-Many relation between Trainee and Trainer")
    void manyToManyRelation_traineeAndTrainer() {
        // Create trainer
        User trainerUser = User.builder()
                .firstName("Bob").lastName("Coach")
                .username("Bob.Coach.ctx").password("encoded")
                .isActive(true).build();
        Trainer trainer = trainerRepository.save(
                Trainer.builder()
                        .user(trainerUser)
                        .specialization("Cardio")
                        .trainees(new HashSet<>())
                        .build()
        );

        User traineeUser = User.builder()
                .firstName("Eve").lastName("Student")
                .username("Eve.Student.ctx").password("encoded")
                .isActive(true).build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();
        trainee.getTrainers().add(trainer);

        Trainee saved = traineeRepository.save(trainee);

        assertThat(saved.getTrainers()).hasSize(1);
        assertThat(saved.getTrainers().iterator().next().getUser().getUsername())
                .isEqualTo("Bob.Coach.ctx");
    }

    @Test
    @Transactional
    @DisplayName("Task 8: One trainer can have multiple trainees")
    void manyToManyRelation_oneTrainerManyTrainees() {
        User trainerUser = User.builder()
                .firstName("Coach").lastName("Pro")
                .username("Coach.Pro.ctx").password("encoded")
                .isActive(true).build();
        Trainer trainer = trainerRepository.save(
                Trainer.builder()
                        .user(trainerUser)
                        .specialization("Fitness")
                        .trainees(new HashSet<>())
                        .build()
        );

        Trainee t1 = Trainee.builder()
                .user(User.builder().firstName("A").lastName("One")
                        .username("A.One.ctx").password("enc").isActive(true).build())
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();
        t1.getTrainers().add(trainer);
        traineeRepository.save(t1);

        Trainee t2 = Trainee.builder()
                .user(User.builder().firstName("B").lastName("Two")
                        .username("B.Two.ctx").password("enc").isActive(true).build())
                .trainers(new HashSet<>())
                .trainings(new HashSet<>())
                .build();
        t2.getTrainers().add(trainer);
        traineeRepository.save(t2);

        long count = traineeRepository.findAll().stream()
                .filter(tr -> tr.getTrainers().stream()
                        .anyMatch(tn -> tn.getUser().getUsername().equals("Coach.Pro.ctx")))
                .count();

        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}