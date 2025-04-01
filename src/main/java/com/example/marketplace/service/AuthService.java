package com.example.marketplace.service;

import com.example.marketplace.model.user.User;
import com.example.marketplace.dto.auth.JwtAuthResponse;
import com.example.marketplace.dto.auth.LoginRequest;
import com.example.marketplace.dto.auth.SignupRequest;

public interface AuthService {

    JwtAuthResponse login(LoginRequest loginRequest);

    JwtAuthResponse signup(SignupRequest signupRequest);

    User getCurrentUser();

    boolean isEmailAvailable(String email);
}