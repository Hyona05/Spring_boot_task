package com.epam.rest.service;

import com.epam.rest.dto.request.*;
import com.epam.rest.dto.response.*;
import com.epam.rest.entity.*;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.*;
import com.epam.rest.service.impl.TrainerServiceImpl;
import com.epam.rest.service.impl.UsernamePasswordGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl Unit Tests")
class TrainerServiceImplTest {

    @Mock TrainerRepository trainerRepository;
    @Mock TrainingRepository trainingRepository;
    @Mock UserRepository userRepository;
    @Mock UsernamePasswordGenerator generator;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @InjectMocks TrainerServiceImpl service;

    private Trainer sampleTrainer;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L).firstName("Alice").lastName("Smith")
                .username("Alice.Smith").password("encoded")
                .isActive(true).build();

        sampleTrainer = Trainer.builder()
                .id(1L).user(sampleUser)
                .specialization("Yoga")
                .trainees(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("register: should create trainer and return credentials")
    void register_success() {
        var req = new TrainerRegistrationRequest("Alice", "Smith", "Yoga");
        given(generator.generateUsername("Alice", "Smith")).willReturn("Alice.Smith");
        given(generator.generatePassword()).willReturn("pass1234XY");
        given(passwordEncoder.encode("pass1234XY")).willReturn("encoded");
        given(trainerRepository.save(any())).willReturn(sampleTrainer);

        RegistrationResponse resp = service.register(req);

        assertThat(resp.username()).isEqualTo("Alice.Smith");
        assertThat(resp.password()).isEqualTo("pass1234XY");
    }

    @Test
    @DisplayName("getProfile: should return profile when trainer exists")
    void getProfile_found() {
        given(trainerRepository.findByUserUsername("Alice.Smith"))
                .willReturn(Optional.of(sampleTrainer));

        TrainerProfileResponse profile = service.getProfile("Alice.Smith");

        assertThat(profile.firstName()).isEqualTo("Alice");
        assertThat(profile.specialization()).isEqualTo("Yoga");
        assertThat(profile.isActive()).isTrue();
    }

    @Test
    @DisplayName("getProfile: should throw when trainer not found")
    void getProfile_notFound() {
        given(trainerRepository.findByUserUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile("ghost"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("updateProfile: should update fields and return response")
    void updateProfile_success() {
        var req = new UpdateTrainerRequest("Alice.Smith", "Alice", "Jones", false);
        given(trainerRepository.findByUserUsername("Alice.Smith"))
                .willReturn(Optional.of(sampleTrainer));
        given(userRepository.save(any())).willReturn(sampleUser);

        UpdateTrainerResponse resp = service.updateProfile(req);

        assertThat(resp.lastName()).isEqualTo("Jones");
        assertThat(resp.isActive()).isFalse();
        assertThat(resp.specialization()).isEqualTo("Yoga");
    }

    @Test
    @DisplayName("activate: should toggle isActive")
    void activate_success() {
        var req = new ActivateDeactivateRequest("Alice.Smith", false);
        given(trainerRepository.findByUserUsername("Alice.Smith"))
                .willReturn(Optional.of(sampleTrainer));
        given(userRepository.save(any())).willReturn(sampleUser);

        assertThatCode(() -> service.activate(req)).doesNotThrowAnyException();
        assertThat(sampleUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("activate: should throw when already in requested state")
    void activate_alreadyActive_throws() {
        var req = new ActivateDeactivateRequest("Alice.Smith", true);
        given(trainerRepository.findByUserUsername("Alice.Smith"))
                .willReturn(Optional.of(sampleTrainer));

        assertThatThrownBy(() -> service.activate(req))
                .isInstanceOf(IllegalStateException.class);
    }
}