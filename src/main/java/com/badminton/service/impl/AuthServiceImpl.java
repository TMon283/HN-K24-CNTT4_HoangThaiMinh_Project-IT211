package com.badminton.service.impl;

import com.badminton.dto.request.LoginRequest;
import com.badminton.dto.response.AuthResponse;
import com.badminton.dto.response.UserResponse;
import com.badminton.entity.RefreshToken;
import com.badminton.entity.User;
import com.badminton.exception.ValidationException;
import com.badminton.mapper.UserMapper;
import com.badminton.repository.RefreshTokenRepository;
import com.badminton.repository.UserRepository;
import com.badminton.security.JwtTokenProvider;
import com.badminton.security.UserPrincipal;
import com.badminton.service.AuthService;
import com.badminton.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed for email={}: user not found after authentication", request.getEmail());
                    return new ValidationException("Invalid credentials");
                });

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        saveRefreshToken(user, refreshToken);
        UserResponse userResponse = userMapper.toResponse(user);

        log.info("Login successful for email={}, userId={}", user.getEmail(), user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getRemainingExpirationMs(accessToken) / 1000)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing access token");

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token failed: invalid token");
                    return new ValidationException("Invalid refresh token");
                });

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token failed for userId={}: expired or revoked", storedToken.getUser().getId());
            throw new ValidationException("Refresh token expired or revoked");
        }

        User user = storedToken.getUser();
        UserPrincipal principal = new UserPrincipal(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);
        UserResponse userResponse = userMapper.toResponse(user);

        log.info("Access token refreshed for userId={}", user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getRemainingExpirationMs(newAccessToken) / 1000)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        long remainingMs = jwtTokenProvider.getRemainingExpirationMs(accessToken);
        tokenBlacklistService.blacklistToken(accessToken, remainingMs);
        log.info("User logged out, access token blacklisted");
    }

    private void saveRefreshToken(User user, String token) {
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}
