package com.badminton.service;

import com.badminton.dto.request.RegisterRequest;
import com.badminton.dto.request.UserCreateRequest;
import com.badminton.dto.request.UserUpdateRequest;
import com.badminton.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse getUserById(Long id);

    Page<UserResponse> searchUsers(String keyword, Pageable pageable);
}
