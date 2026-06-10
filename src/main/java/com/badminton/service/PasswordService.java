package com.badminton.service;

import com.badminton.dto.request.ChangePasswordRequest;
import com.badminton.dto.request.ForgotPasswordRequest;
import com.badminton.dto.request.ResetPasswordRequest;
import com.badminton.dto.response.ForgotPasswordResponse;

public interface PasswordService {

    void changePassword(Long userId, ChangePasswordRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
