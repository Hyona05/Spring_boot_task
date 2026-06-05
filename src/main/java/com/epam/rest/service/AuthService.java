package com.epam.rest.service;

import com.epam.rest.entity.User;
import com.epam.rest.exception.AuthException;
import com.epam.rest.repository.TraineeRepository;
import com.epam.rest.repository.TrainerRepository;
import com.epam.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;

    public void authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthException("User is inactive");
        }
    }

    public void authenticateTrainer(String username, String password) {
        authenticate(username, password);

        trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new AuthException("Trainer profile not found"));
    }

    public void authenticateTrainee(String username, String password) {
        authenticate(username, password);

        traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new AuthException("Trainee profile not found"));
    }
}