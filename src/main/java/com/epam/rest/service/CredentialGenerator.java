package com.epam.rest.service;

import com.epam.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CredentialGenerator {

    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName) {
        String base = firstName + "." + lastName;
        String username = base;
        int count = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = base + count;
            count++;
        }

        return username;
    }

    public String generatePassword() {
        return String.valueOf(1000000000 + random.nextInt(900000000));
    }
}