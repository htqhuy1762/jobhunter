package vn.hoidanit.jobhunter.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${hoidanit.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    // Prefix cho key trong Redis để dễ quản lý
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

    /**
     * Lưu refresh token vào Redis với TTL (Time To Live)
     * Key format: refresh_token:email
     * Value: refresh token string
     */
    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.SECONDS);
    }

    /**
     * Lấy refresh token từ Redis theo email
     */
    public String getRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Xóa refresh token khi user logout
     */
    public void deleteRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * Kiểm tra refresh token có hợp lệ không
     * So sánh token từ request với token lưu trong Redis
     */
    public boolean validateRefreshToken(String email, String refreshToken) {
        String storedToken = getRefreshToken(email);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    /**
     * Cập nhật thời gian expire cho token (nếu cần)
     */
    public void extendRefreshTokenExpiration(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.expire(key, refreshTokenExpiration, TimeUnit.SECONDS);
    }

    /**
     * ===== ACCESS TOKEN BLACKLIST =====
     * Lưu access token vào blacklist khi user logout
     * Token sẽ bị từ chối khi gọi API
     */

    /**
     * Thêm access token vào blacklist
     * Key format: blacklist_token:token_string
     * Value: email (để tracking)
     * TTL: thời gian còn lại của access token
     */
    public void blacklistAccessToken(String accessToken, String email, long remainingTimeInSeconds) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        // Chỉ lưu token vào blacklist trong thời gian token còn hiệu lực
        // Sau khi token expire tự nhiên, Redis sẽ tự động xóa
        redisTemplate.opsForValue().set(key, email, remainingTimeInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Kiểm tra access token có trong blacklist không
     * Trả về true nếu token bị blacklist (không cho phép sử dụng)
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Xóa token khỏi blacklist (thường không cần dùng vì Redis tự động xóa khi expire)
     */
    public void removeFromBlacklist(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        redisTemplate.delete(key);
    }

    /**
     * Lấy thông tin user từ blacklisted token (để tracking/audit)
     */
    public String getBlacklistedTokenOwner(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return redisTemplate.opsForValue().get(key);
    }
}
