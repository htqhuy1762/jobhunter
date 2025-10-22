package vn.hoidanit.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service để check token có trong blacklist không (dành cho API Gateway - Reactive)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

    /**
     * Kiểm tra access token có trong blacklist không (Reactive version)
     * Trả về true nếu token bị blacklist (không cho phép sử dụng)
     */
    public Mono<Boolean> isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return reactiveRedisTemplate.hasKey(key)
                .doOnNext(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("Token is blacklisted: {}", key.substring(0, Math.min(30, key.length())) + "...");
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error checking blacklist in Redis: {}", e.getMessage());
                    // Nếu Redis lỗi, cho phép request đi qua (fail-open)
                    // Hoặc có thể return Mono.just(false) để block nếu muốn fail-closed
                    return Mono.just(false);
                });
    }
}

