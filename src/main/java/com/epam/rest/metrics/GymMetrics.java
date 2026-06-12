package com.epam.rest.metrics;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Prometheus metrics for the Gym Management application.
 *
 * Exposed at: GET /actuator/prometheus
 *
 * Metrics:
 *  1. gym_trainee_registrations_total  — Counter: total trainee registrations
 *  2. gym_trainer_registrations_total  — Counter: total trainer registrations
 *  3. gym_active_trainees              — Gauge:   current active trainee count
 *  4. gym_active_trainers              — Gauge:   current active trainer count
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GymMetrics {

    private final MeterRegistry meterRegistry;

    // ── Internal atomic state for gauges ──────────────────────────
    private final AtomicInteger activeTraineesGauge  = new AtomicInteger(0);
    private final AtomicInteger activeTrainersGauge  = new AtomicInteger(0);

    // ── Counters (assigned after registration) ────────────────────
    private Counter traineeRegistrationCounter;
    private Counter trainerRegistrationCounter;

    @PostConstruct
    public void registerMetrics() {
        // Metric 1 — Counter: trainee registrations
        traineeRegistrationCounter = Counter.builder("gym_trainee_registrations_total")
                .description("Total number of trainee registrations")
                .tag("type", "trainee")
                .register(meterRegistry);

        // Metric 2 — Counter: trainer registrations
        trainerRegistrationCounter = Counter.builder("gym_trainer_registrations_total")
                .description("Total number of trainer registrations")
                .tag("type", "trainer")
                .register(meterRegistry);

        // Metric 3 — Gauge: active trainees
        Gauge.builder("gym_active_trainees", activeTraineesGauge, AtomicInteger::get)
                .description("Current number of active trainees")
                .tag("entity", "trainee")
                .register(meterRegistry);

        // Metric 4 — Gauge: active trainers
        Gauge.builder("gym_active_trainers", activeTrainersGauge, AtomicInteger::get)
                .description("Current number of active trainers")
                .tag("entity", "trainer")
                .register(meterRegistry);

        log.info("GymMetrics: custom Prometheus metrics registered");
    }

    // ── Public API called from services ───────────────────────────

    /** Call after a trainee is successfully registered. */
    public void incrementTraineeRegistration() {
        traineeRegistrationCounter.increment();
        log.debug("Metric incremented: gym_trainee_registrations_total");
    }

    /** Call after a trainer is successfully registered. */
    public void incrementTrainerRegistration() {
        trainerRegistrationCounter.increment();
        log.debug("Metric incremented: gym_trainer_registrations_total");
    }

    /** Set to current active trainee count (call after activate/deactivate). */
    public void setActiveTrainees(int count) {
        activeTraineesGauge.set(count);
        log.debug("Metric updated: gym_active_trainees={}", count);
    }

    /** Set to current active trainer count (call after activate/deactivate). */
    public void setActiveTrainers(int count) {
        activeTrainersGauge.set(count);
        log.debug("Metric updated: gym_active_trainers={}", count);
    }
}

