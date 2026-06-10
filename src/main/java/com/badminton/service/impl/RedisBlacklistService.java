package com.badminton.service.impl;

import com.badminton.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.blacklist.storage", havingValue = "redis")
public class RedisBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklistToken(String token, long remainingExpirationMs) {
        if (remainingExpirationMs <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "true",
                remainingExpirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isBlacklisted(String token) {
        Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(exists);
    }
}
