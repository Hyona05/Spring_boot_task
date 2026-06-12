package com.epam.rest.repository;

import com.epam.rest.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    long countByUserIsActiveTrue();
    boolean existsByUserUsername(String username);

}