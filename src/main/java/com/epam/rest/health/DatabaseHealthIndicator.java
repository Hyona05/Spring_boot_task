package com.epam.rest.health;

import com.epam.rest.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health Indicator #1
 * Checks that the database is reachable by querying training_types table.
 * Returns UP if query succeeds (even with 0 rows — empty DB is still reachable).
 * Returns DOWN only when an exception is thrown (connection refused, table missing, etc).
 */
@Slf4j
@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    public Health health() {
        try {
            long count = trainingTypeRepository.count();
            log.debug("DatabaseHealthIndicator: training_types count={}", count);

            return Health.up()
                    .withDetail("status", "Database reachable")
                    .withDetail("trainingTypesCount", count)
                    .build();

        } catch (Exception ex) {
            log.error("DatabaseHealthIndicator: DB check failed", ex);
            return Health.down(ex)
                    .withDetail("reason", "Cannot query database: " + ex.getMessage())
                    .build();
        }
    }
}
