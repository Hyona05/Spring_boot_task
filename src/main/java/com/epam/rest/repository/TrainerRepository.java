package com.epam.rest.repository;

import com.epam.rest.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserUsername(String username);
}