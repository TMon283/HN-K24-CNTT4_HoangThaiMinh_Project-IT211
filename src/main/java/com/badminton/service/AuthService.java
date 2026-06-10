package com.badminton.service;

import com.badminton.dto.request.LoginRequest;
import com.badminton.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String accessToken);
}
