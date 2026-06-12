package com.badminton.controller;

import com.badminton.dto.request.LoginRequest;
import com.badminton.dto.request.RegisterRequest;
import com.badminton.dto.response.AuthResponse;
import com.badminton.dto.response.UserResponse;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.service.AuthService;
import com.badminton.service.PasswordService;
import com.badminton.service.UserService;
import com.badminton.support.MockMvcTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcTestSupport.buildMockMvc(authController);
    }

    @Test
    void register_shouldReturn201_whenValidRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@mail.com")
                .password("Password@123")
                .fullName("New User")
                .phone("0901234567")
                .build();

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(UserResponse.builder().email("new@mail.com").fullName("New User").build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new@mail.com"));
    }

    @Test
    void login_shouldReturn200_whenValidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("customer@mail.com")
                .password("Customer@123")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .tokenType("Bearer")
                        .expiresIn(900)
                        .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }
}
