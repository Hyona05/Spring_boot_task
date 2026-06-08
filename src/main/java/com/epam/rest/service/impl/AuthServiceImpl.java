package com.epam.rest.service.impl;

import com.epam.rest.dto.request.ChangeLoginRequest;
import com.epam.rest.entity.User;
import com.epam.rest.exception.AuthException;
import com.epam.rest.exception.NotFoundException;
import com.epam.rest.repository.UserRepository;
import com.epam.rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public void login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthException("User is inactive");
        }
        log.info("User logged in: {}", username);
    }

    @Override
    @Transactional
    public void changeLogin(ChangeLoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new NotFoundException("User not found: " + req.username()));
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new AuthException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", req.username());
    }
}