package vn.hoidanit.authservice.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 7;

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofDays(REFRESH_TOKEN_TTL_DAYS));
        log.debug("Saved refresh token for user: {}", email);
    }

    public Optional<String> getRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    public void deleteRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Deleted refresh token for user: {}", email);
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        Optional<String> storedToken = getRefreshToken(email);
        return storedToken.isPresent() && storedToken.get().equals(refreshToken);
    }

    public void blacklistAccessToken(String accessToken, String email, long remainingTimeInSeconds) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, email, remainingTimeInSeconds, TimeUnit.SECONDS);
        log.debug("Blacklisted access token for user: {} with TTL: {}s", email, remainingTimeInSeconds);
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void removeFromBlacklist(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        redisTemplate.delete(key);
        log.debug("Removed token from blacklist");
    }

    public String getBlacklistedTokenOwner(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return redisTemplate.opsForValue().get(key);
    }
}