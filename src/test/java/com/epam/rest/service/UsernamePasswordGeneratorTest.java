package com.epam.rest.service;

import com.epam.rest.entity.User;
import com.epam.rest.repository.UserRepository;
import com.epam.rest.service.impl.UsernamePasswordGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsernamePasswordGenerator Unit Tests")
class UsernamePasswordGeneratorTest {

    @Mock UserRepository userRepository;
    @InjectMocks UsernamePasswordGenerator generator;

    @Test
    @DisplayName("generateUsername: base form when no collision")
    void generateUsername_noCollision() {
        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.empty());

        String username = generator.generateUsername("John", "Doe");

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    @DisplayName("generateUsername: appends count when collision exists")
    void generateUsername_withCollision() {
        given(userRepository.findByUsername("John.Doe"))
                .willReturn(Optional.of(new User()));
        given(userRepository.countByUsernameStartingWith("John.Doe")).willReturn(1L);

        String username = generator.generateUsername("John", "Doe");

        assertThat(username).isEqualTo("John.Doe2");
    }

    @Test
    @DisplayName("generatePassword: returns 10-char alphanumeric string")
    void generatePassword_correctLength() {
        String pass = generator.generatePassword();

        assertThat(pass).hasSize(10);
        assertThat(pass).matches("[A-Za-z0-9]+");
    }

    @Test
    @DisplayName("generatePassword: generates unique values")
    void generatePassword_unique() {
        String p1 = generator.generatePassword();
        String p2 = generator.generatePassword();
        assertThat(p1).isNotNull();
        assertThat(p2).isNotNull();
    }
}
