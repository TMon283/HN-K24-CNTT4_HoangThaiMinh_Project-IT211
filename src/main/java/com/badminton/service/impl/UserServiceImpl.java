package com.badminton.service.impl;

import com.badminton.constant.RoleType;
import com.badminton.dto.request.RegisterRequest;
import com.badminton.dto.request.UserCreateRequest;
import com.badminton.dto.request.UserUpdateRequest;
import com.badminton.dto.response.UserResponse;
import com.badminton.entity.Role;
import com.badminton.entity.User;
import com.badminton.exception.ConflictException;
import com.badminton.exception.ResourceNotFoundException;
import com.badminton.mapper.UserMapper;
import com.badminton.repository.RoleRepository;
import com.badminton.repository.UserRepository;
import com.badminton.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email={}", request.getEmail());
        validateEmailUniqueness(request.getEmail());

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .enabled(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: userId={}, email={}", saved.getId(), saved.getEmail());

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Admin creating user with email={}", request.getEmail());
        validateEmailUniqueness(request.getEmail());

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .enabled(true)
                .roles(roles)
                .build();

        User saved = userRepository.save(user);
        log.info("User created successfully: userId={}, email={}", saved.getId(), saved.getEmail());

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user id={}", id);

        User user = findUserById(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailUniqueness(request.getEmail());
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        User updated = userRepository.save(user);
        log.info("User updated successfully: userId={}, email={}", updated.getId(), updated.getEmail());

        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("User deleted successfully: userId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users with keyword={}", keyword);
        return userRepository.searchUsers(keyword, pageable)
                .map(userMapper::toResponse);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            throw new ConflictException("Email already exists: " + email);
        }
    }

    private Set<Role> resolveRoles(Set<RoleType> roleTypes) {
        return roleTypes.stream()
                .map(roleType -> roleRepository.findByName(roleType)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleType)))
                .collect(Collectors.toSet());
    }
}
