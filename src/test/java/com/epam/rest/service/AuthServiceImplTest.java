package com.epam.rest.service;

import com.epam.rest.dto.request.ChangeLoginRequest;
import com.epam.rest.entity.User;
import com.epam.rest.exception.AuthException;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.UserRepository;
import com.epam.rest.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @InjectMocks AuthServiceImpl service;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L).username("John.Doe").password("encoded")
                .isActive(true).firstName("John").lastName("Doe").build();
    }

    @Test
    @DisplayName("login: should succeed with valid credentials")
    void login_success() {
        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("rawPass", "encoded")).willReturn(true);

        assertThatCode(() -> service.login("John.Doe", "rawPass"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("login: should throw AuthException when user not found")
    void login_userNotFound() {
        given(userRepository.findByUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("ghost", "pass"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("login: should throw AuthException when password wrong")
    void login_wrongPassword() {
        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> service.login("John.Doe", "wrong"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("login: should throw AuthException when user inactive")
    void login_inactiveUser() {
        activeUser.setIsActive(false);
        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("rawPass", "encoded")).willReturn(true);

        assertThatThrownBy(() -> service.login("John.Doe", "rawPass"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    @DisplayName("changeLogin: should change password successfully")
    void changeLogin_success() {
        var req = new ChangeLoginRequest("John.Doe", "rawPass", "newPass");
        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("rawPass", "encoded")).willReturn(true);
        given(passwordEncoder.encode("newPass")).willReturn("newEncoded");
        given(userRepository.save(any())).willReturn(activeUser);

        assertThatCode(() -> service.changeLogin(req)).doesNotThrowAnyException();
        assertThat(activeUser.getPassword()).isEqualTo("newEncoded");
    }

    @Test
    @DisplayName("changeLogin: should throw AuthException when old password wrong")
    void changeLogin_wrongOldPassword() {
        var req = new ChangeLoginRequest("John.Doe", "wrong", "newPass");
        given(userRepository.findByUsername("John.Doe")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> service.changeLogin(req))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Old password is incorrect");
    }

    @Test
    @DisplayName("changeLogin: should throw NotFoundException when user missing")
    void changeLogin_userNotFound() {
        var req = new ChangeLoginRequest("ghost", "pass", "new");
        given(userRepository.findByUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeLogin(req))
                .isInstanceOf(NotFoundException.class);
    }
}