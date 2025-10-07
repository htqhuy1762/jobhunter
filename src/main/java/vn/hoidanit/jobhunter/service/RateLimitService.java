package vn.hoidanit.jobhunter.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service xử lý Rate Limiting sử dụng Redis
 * Thuật toán: Fixed Window Counter
 */
@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Kiểm tra xem request có bị rate limit không
     *
     * @param key      Redis key (thường là IP hoặc user ID)
     * @param limit    Số request tối đa
     * @param duration Thời gian window (giây)
     * @return true nếu request được phép, false nếu vượt quá giới hạn
     */
    public boolean allowRequest(String key, int limit, int duration) {
        try {
            String redisKey = key;

            // Lấy số lượng request hiện tại
            String currentCountStr = redisTemplate.opsForValue().get(redisKey);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;

            // Nếu đã vượt quá giới hạn
            if (currentCount >= limit) {
                return false;
            }

            // Tăng counter
            Long newCount = redisTemplate.opsForValue().increment(redisKey);

            // Nếu là request đầu tiên, set TTL
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(duration));
            }

            return true;

        } catch (Exception e) {
            // Nếu Redis lỗi, cho phép request (fail-open strategy)
            return true;
        }
    }

    /**
     * Lấy số request còn lại
     *
     * @param key      Redis key
     * @param limit    Số request tối đa
     * @return Số request còn lại
     */
    public int getRemainingRequests(String key, int limit) {
        try {
            String currentCountStr = redisTemplate.opsForValue().get(key);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            return Math.max(0, limit - currentCount);
        } catch (Exception e) {
            return limit;
        }
    }

    /**
     * Lấy thời gian còn lại cho đến khi reset (giây)
     *
     * @param key Redis key
     * @return Thời gian còn lại (giây), -1 nếu không tồn tại
     */
    public long getTimeUntilReset(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Reset rate limit cho một key
     *
     * @param key Redis key
     */
    public void resetLimit(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
        }
    }
}
