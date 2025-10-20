package vn.hoidanit.authservice.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * Lưu refresh token vào Redis
     * KEY: refresh_token:{email}
     * VALUE: {refresh_token}
     * TTL: 7 days
     */
    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofDays(7));
        log.info("Saved refresh token for user: {}", email);
    }

    /**
     * Lấy refresh token từ Redis
     */
    public Optional<String> getRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    /**
     * XÓA refresh token cũ khi refresh (QUAN TRỌNG!)
     */
    public void deleteRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
        log.info("Deleted refresh token for user: {}", email);
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String email, String refreshToken) {
        Optional<String> storedToken = getRefreshToken(email);
        return storedToken.isPresent() && storedToken.get().equals(refreshToken);
    }
}