package com.epam.rest.health;

import com.epam.rest.repository.TraineeRepository;
import com.epam.rest.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component("userRegistration")
@RequiredArgsConstructor
public class UserRegistrationHealthIndicator implements HealthIndicator {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public Health health() {
        try {
            long traineeCount = traineeRepository.count();
            long trainerCount = trainerRepository.count();
            log.debug("UserRegistrationHealthIndicator: trainees={}, trainers={}",
                    traineeCount, trainerCount);

            return Health.up()
                    .withDetail("totalTrainees", traineeCount)
                    .withDetail("totalTrainers", trainerCount)
                    .withDetail("registrationServiceStatus", "operational")
                    .build();

        } catch (Exception ex) {
            log.error("UserRegistrationHealthIndicator: check failed", ex);
            return Health.down(ex)
                    .withDetail("reason", "Cannot access user tables: " + ex.getMessage())
                    .build();
        }
    }
}