package com.epam.rest.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GymMetrics Unit Tests")
class GymMetricsTest {

    private MeterRegistry meterRegistry;
    private GymMetrics gymMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymMetrics = new GymMetrics(meterRegistry);
        gymMetrics.registerMetrics();   // @PostConstruct manually called
    }

    @Test
    @DisplayName("incrementTraineeRegistration: counter increases by 1 each call")
    void incrementTraineeRegistration_incrementsCounter() {
        gymMetrics.incrementTraineeRegistration();
        gymMetrics.incrementTraineeRegistration();

        Counter counter = meterRegistry.find("gym_trainee_registrations_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("incrementTrainerRegistration: counter increases by 1 each call")
    void incrementTrainerRegistration_incrementsCounter() {
        gymMetrics.incrementTrainerRegistration();

        Counter counter = meterRegistry.find("gym_trainer_registrations_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("setActiveTrainees: gauge reflects latest value")
    void setActiveTrainees_updatesGauge() {
        gymMetrics.setActiveTrainees(10);

        double value = meterRegistry.find("gym_active_trainees")
                .gauge().value();
        assertThat(value).isEqualTo(10.0);

        gymMetrics.setActiveTrainees(7);
        value = meterRegistry.find("gym_active_trainees").gauge().value();
        assertThat(value).isEqualTo(7.0);
    }

    @Test
    @DisplayName("setActiveTrainers: gauge reflects latest value")
    void setActiveTrainers_updatesGauge() {
        gymMetrics.setActiveTrainers(5);

        double value = meterRegistry.find("gym_active_trainers")
                .gauge().value();
        assertThat(value).isEqualTo(5.0);
    }

    @Test
    @DisplayName("All 4 metrics are registered after @PostConstruct")
    void allMetricsRegistered() {
        assertThat(meterRegistry.find("gym_trainee_registrations_total").counter()).isNotNull();
        assertThat(meterRegistry.find("gym_trainer_registrations_total").counter()).isNotNull();
        assertThat(meterRegistry.find("gym_active_trainees").gauge()).isNotNull();
        assertThat(meterRegistry.find("gym_active_trainers").gauge()).isNotNull();
    }

    @Test
    @DisplayName("Trainee counter is independent from trainer counter")
    void countersAreIndependent() {
        gymMetrics.incrementTraineeRegistration();
        gymMetrics.incrementTraineeRegistration();
        gymMetrics.incrementTrainerRegistration();

        assertThat(meterRegistry.find("gym_trainee_registrations_total")
                .counter().count()).isEqualTo(2.0);
        assertThat(meterRegistry.find("gym_trainer_registrations_total")
                .counter().count()).isEqualTo(1.0);
    }
}