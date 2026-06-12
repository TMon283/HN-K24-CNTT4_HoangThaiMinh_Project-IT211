package com.badminton.service.impl;

import com.badminton.constant.RoleType;
import com.badminton.dto.request.RegisterRequest;
import com.badminton.dto.request.UserCreateRequest;
import com.badminton.dto.request.UserUpdateRequest;
import com.badminton.dto.response.UserResponse;
import com.badminton.entity.Role;
import com.badminton.entity.User;
import com.badminton.exception.ConflictException;
import com.badminton.mapper.UserMapper;
import com.badminton.repository.RoleRepository;
import com.badminton.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_shouldCreateCustomer_whenEmailUnique() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@mail.com")
                .password("Password@123")
                .fullName("New User")
                .phone("0901234567")
                .build();

        Role customerRole = Role.builder().id(1L).name(RoleType.ROLE_CUSTOMER).build();
        User savedUser = User.builder().id(1L).email("new@mail.com").fullName("New User").build();
        UserResponse response = UserResponse.builder().email("new@mail.com").build();

        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = userService.register(request);

        assertThat(result.getEmail()).isEqualTo("new@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowConflict_whenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("exists@mail.com")
                .password("Password@123")
                .fullName("Existing User")
                .build();

        when(userRepository.existsByEmail("exists@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_shouldCreateUserWithRoles() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("manager@mail.com")
                .password("Manager@123")
                .fullName("Manager User")
                .roles(Set.of(RoleType.ROLE_MANAGER))
                .build();

        Role managerRole = Role.builder().id(2L).name(RoleType.ROLE_MANAGER).build();
        User savedUser = User.builder().id(2L).email("manager@mail.com").build();
        UserResponse response = UserResponse.builder().email("manager@mail.com").build();

        when(userRepository.existsByEmail("manager@mail.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_MANAGER)).thenReturn(Optional.of(managerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("manager@mail.com");
    }

    @Test
    void updateUser_shouldUpdateFullName() {
        User existingUser = User.builder()
                .id(1L)
                .email("user@mail.com")
                .fullName("Old Name")
                .enabled(true)
                .roles(Set.of())
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullName("New Name")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(UserResponse.builder().fullName("New Name").build());

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result.getFullName()).isEqualTo("New Name");
        assertThat(existingUser.getFullName()).isEqualTo("New Name");
    }
}
