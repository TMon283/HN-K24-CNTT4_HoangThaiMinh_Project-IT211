package com.badminton.service;

public interface TokenBlacklistService {

    void blacklistToken(String token, long remainingExpirationMs);

    boolean isBlacklisted(String token);
}
