package com.badminton.controller;

import com.badminton.constant.RoleType;
import com.badminton.dto.request.UserCreateRequest;
import com.badminton.dto.response.UserResponse;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.service.UserService;
import com.badminton.support.MockMvcTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcTestSupport.buildMockMvc(adminController);
    }

    @Test
    void createUser_shouldReturn201_whenValidRequest() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("manager@mail.com")
                .password("Manager@123")
                .fullName("Manager User")
                .roles(Set.of(RoleType.ROLE_MANAGER))
                .build();

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(UserResponse.builder().id(2L).email("manager@mail.com").build());

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("manager@mail.com"));
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() throws Exception {
        when(userService.getUserById(1L))
                .thenReturn(UserResponse.builder().id(1L).email("admin@badminton.com").build());

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void deleteUser_shouldReturn200_whenUserExists() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).deleteUser(2L);
    }
}
