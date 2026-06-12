package com.epam.rest.health;

import com.epam.rest.repository.TraineeRepository;
import com.epam.rest.repository.TrainerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationHealthIndicator Tests")
class UserRegistrationHealthIndicatorTest {

    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @InjectMocks UserRegistrationHealthIndicator healthIndicator;

    @Test
    @DisplayName("health: UP and shows counts when repos accessible")
    void health_up_withCounts() {
        given(traineeRepository.count()).willReturn(5L);
        given(trainerRepository.count()).willReturn(3L);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("totalTrainees")).isEqualTo(5L);
        assertThat(health.getDetails().get("totalTrainers")).isEqualTo(3L);
        assertThat(health.getDetails().get("registrationServiceStatus"))
                .isEqualTo("operational");
    }

    @Test
    @DisplayName("health: UP even with zero users (new installation)")
    void health_up_withZeroCounts() {
        given(traineeRepository.count()).willReturn(0L);
        given(trainerRepository.count()).willReturn(0L);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("health: DOWN when repo throws exception")
    void health_down_whenRepoThrows() {
        given(traineeRepository.count()).willThrow(new RuntimeException("Table missing"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("reason").toString())
                .contains("Cannot access user tables");
    }
}