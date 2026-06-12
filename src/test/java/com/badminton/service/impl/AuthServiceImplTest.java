package com.badminton.service.impl;

import com.badminton.dto.request.LoginRequest;
import com.badminton.dto.response.AuthResponse;
import com.badminton.dto.response.UserResponse;
import com.badminton.entity.User;
import com.badminton.mapper.UserMapper;
import com.badminton.repository.RefreshTokenRepository;
import com.badminton.repository.UserRepository;
import com.badminton.security.JwtTokenProvider;
import com.badminton.security.UserPrincipal;
import com.badminton.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        LoginRequest request = LoginRequest.builder()
                .email("customer@mail.com")
                .password("Customer@123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("customer@mail.com")
                .password("encoded")
                .fullName("Customer")
                .enabled(true)
                .roles(Set.of())
                .build();

        UserPrincipal principal = new UserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail("customer@mail.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(principal)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(principal)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRemainingExpirationMs("access-token")).thenReturn(900000L);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().email("customer@mail.com").build());

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void logout_shouldBlacklistToken() {
        when(jwtTokenProvider.getRemainingExpirationMs("token")).thenReturn(60000L);

        authService.logout("token");

        verify(tokenBlacklistService).blacklistToken("token", 60000L);
    }
}
