package com.travelplanner.demo.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private static final long REFRESH_TOKEN_TTL = 60 * 60 * 24 * 7; // 7일
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";
    private final StringRedisTemplate stringRedisTemplate;

    // Fallback in-memory storage if Redis is down
    private final ConcurrentHashMap<String, String> memoryStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> memoryStorageExpiry = new ConcurrentHashMap<>();
    private boolean isRedisAvailable = true;

    private boolean checkRedisConnection() {
        if (!isRedisAvailable) {
            return false;
        }
        try {
            stringRedisTemplate.hasKey("connection-test");
            return true;
        } catch (Exception e) {
            log.warn("⚠️ Redis connection failed. Falling back to local In-Memory Storage! Local development mode active. Error: {}", e.getMessage());
            isRedisAvailable = false;
            return false;
        }
    }

    public void saveRefreshToken(String userId, String refreshToken) {
        if (checkRedisConnection()) {
            try {
                stringRedisTemplate.opsForValue()
                        .set(REFRESH_TOKEN_PREFIX + userId, refreshToken, REFRESH_TOKEN_TTL, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                log.warn("Failed to save to Redis, using in-memory: {}", e.getMessage());
            }
        }
        String key = REFRESH_TOKEN_PREFIX + userId;
        memoryStorage.put(key, refreshToken);
        memoryStorageExpiry.put(key, System.currentTimeMillis() + (REFRESH_TOKEN_TTL * 1000));
    }

    public void deleteRefreshToken(String userId) {
        if (checkRedisConnection()) {
            try {
                stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
                return;
            } catch (Exception e) {
                log.warn("Failed to delete from Redis, using in-memory: {}", e.getMessage());
            }
        }
        memoryStorage.remove(REFRESH_TOKEN_PREFIX + userId);
        memoryStorageExpiry.remove(REFRESH_TOKEN_PREFIX + userId);
    }

    public String getRefreshToken(String userId) {
        if (checkRedisConnection()) {
            try {
                return stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
            } catch (Exception e) {
                log.warn("Failed to get from Redis, using in-memory: {}", e.getMessage());
            }
        }
        String key = REFRESH_TOKEN_PREFIX + userId;
        Long expiry = memoryStorageExpiry.get(key);
        if (expiry != null && expiry < System.currentTimeMillis()) {
            memoryStorage.remove(key);
            memoryStorageExpiry.remove(key);
            return null;
        }
        return memoryStorage.get(key);
    }

    public boolean validateRefreshToken(String userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void blacklistToken(String accessToken, long remainingTimeMs) {
        addToBlacklist(accessToken, remainingTimeMs);
    }

    public boolean isTokenBlacklisted(String accessToken) {
        return isBlacklisted(accessToken);
    }

    // Access Token 블랙리스트 (로그아웃 시 즉시 무효화)
    public void addToBlacklist(String accessToken, long ttlMillis) {
        if (checkRedisConnection()) {
            try {
                stringRedisTemplate.opsForValue()
                        .set(BLACKLIST_PREFIX + accessToken, "1", ttlMillis, TimeUnit.MILLISECONDS);
                return;
            } catch (Exception e) {
                log.warn("Failed to add blacklist to Redis, using in-memory: {}", e.getMessage());
            }
        }
        String key = BLACKLIST_PREFIX + accessToken;
        memoryStorage.put(key, "1");
        memoryStorageExpiry.put(key, System.currentTimeMillis() + ttlMillis);
    }

    public boolean isBlacklisted(String accessToken) {
        if (checkRedisConnection()) {
            try {
                return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
            } catch (Exception e) {
                log.warn("Failed to check blacklist in Redis, using in-memory: {}", e.getMessage());
            }
        }
        String key = BLACKLIST_PREFIX + accessToken;
        Long expiry = memoryStorageExpiry.get(key);
        if (expiry != null && expiry < System.currentTimeMillis()) {
            memoryStorage.remove(key);
            memoryStorageExpiry.remove(key);
            return false;
        }
        return memoryStorage.containsKey(key);
    }
}
