package com.badminton.service.impl;

import com.badminton.dto.request.ChangePasswordRequest;
import com.badminton.dto.request.ForgotPasswordRequest;
import com.badminton.dto.request.ResetPasswordRequest;
import com.badminton.dto.response.ForgotPasswordResponse;
import com.badminton.entity.PasswordResetToken;
import com.badminton.entity.User;
import com.badminton.exception.ResourceNotFoundException;
import com.badminton.exception.ValidationException;
import com.badminton.repository.PasswordResetTokenRepository;
import com.badminton.repository.UserRepository;
import com.badminton.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    passwordResetTokenRepository.deleteByUserId(user.getId());

                    String token = UUID.randomUUID().toString();
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .user(user)
                            .token(token)
                            .expiryDate(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS))
                            .used(false)
                            .build();

                    passwordResetTokenRepository.save(resetToken);

                    return ForgotPasswordResponse.builder()
                            .message("Password reset token generated successfully")
                            .resetToken(token)
                            .build();
                })
                .orElse(ForgotPasswordResponse.builder()
                        .message("If the email exists, a reset token has been generated")
                        .build());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ValidationException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new ValidationException("Reset token has already been used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
