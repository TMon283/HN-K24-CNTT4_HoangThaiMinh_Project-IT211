package com.badminton.service.impl;

import com.badminton.entity.TokenBlacklist;
import com.badminton.repository.TokenBlacklistRepository;
import com.badminton.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.blacklist.storage", havingValue = "database", matchIfMissing = true)
public class DatabaseBlacklistService implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    @Transactional
    public void blacklistToken(String token, long remainingExpirationMs) {
        if (remainingExpirationMs <= 0) {
            return;
        }

        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(remainingExpirationMs * 1_000_000L);

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiryDate(expiryDate)
                .build();

        tokenBlacklistRepository.save(blacklist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }
}
