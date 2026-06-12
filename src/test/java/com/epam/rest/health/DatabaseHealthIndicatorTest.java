package com.epam.rest.health;

import com.epam.rest.repository.TrainingTypeRepository;
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
@DisplayName("DatabaseHealthIndicator Tests")
class DatabaseHealthIndicatorTest {

    @Mock TrainingTypeRepository trainingTypeRepository;
    @InjectMocks DatabaseHealthIndicator healthIndicator;

    @Test
    @DisplayName("health: UP when training types exist")
    void health_up_whenTrainingTypesExist() {
        given(trainingTypeRepository.count()).willReturn(4L);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("trainingTypesCount");
        assertThat(health.getDetails().get("trainingTypesCount")).isEqualTo(4L);
    }

    @Test
    @DisplayName("health: UP even when no training types (DB reachable, just empty)")
    void health_up_whenNoTrainingTypes() {
        given(trainingTypeRepository.count()).willReturn(0L);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("trainingTypesCount")).isEqualTo(0L);
    }

    @Test
    @DisplayName("health: DOWN when DB throws exception")
    void health_down_whenDbThrows() {
        given(trainingTypeRepository.count())
                .willThrow(new RuntimeException("Connection refused"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("reason").toString())
                .contains("Cannot query database");
    }
}
