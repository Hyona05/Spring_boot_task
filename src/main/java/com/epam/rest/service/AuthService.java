package com.epam.rest.service;

import com.epam.rest.dto.request.ChangeLoginRequest;

public interface AuthService {
    void login(String username, String password);
    void changeLogin(ChangeLoginRequest request);
}