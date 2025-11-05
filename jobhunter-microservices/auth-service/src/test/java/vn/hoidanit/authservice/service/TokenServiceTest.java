package vn.hoidanit.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GROUP 1 - REFRESH TOKEN OPERATIONS:
 * 1. TODO: saveRefreshToken - Lưu token thành công
 * 2. TODO: getRefreshToken - Lấy token khi tồn tại
 * 3. TODO: getRefreshToken - Trả về Optional.empty() khi không tồn tại
 * 4. TODO: deleteRefreshToken - Xóa token thành công
 * 5. TODO: validateRefreshToken - Token hợp lệ (match)
 * 6. TODO: validateRefreshToken - Token không hợp lệ (không match)
 * 7. TODO: validateRefreshToken - Token không tồn tại trong Redis
 *
 * GROUP 2 - BLACKLIST OPERATIONS:
 * 8. TODO: blacklistAccessToken - Thêm token vào blacklist
 * 9. TODO: isAccessTokenBlacklisted - Token có trong blacklist
 * 10. TODO: isAccessTokenBlacklisted - Token không có trong blacklist
 * 11. TODO: removeFromBlacklist - Xóa token khỏi blacklist
 * 12. TODO: getBlacklistedTokenOwner - Lấy thông tin owner của blacklisted token
 *
 * ========================================
 * KIẾN THỨC MỚI CẦN HỌC:
 * ========================================
 *
 * 1. MOCK RedisTemplate:
 *    RedisTemplate có method chain: redisTemplate.opsForValue().get(key)
 *    Bạn cần mock cả RedisTemplate và ValueOperations
 *
 * 2. TEST Optional<T>:
 *    - Method trả về Optional.of(value) khi có data
 *    - Method trả về Optional.empty() khi không có data
 *    - Assert: assertTrue(result.isPresent()) hoặc assertTrue(result.isEmpty())
 *
 * 3. TEST void methods:
 *    - Không có return value
 *    - Chỉ cần verify method được gọi với đúng params
 *    - Sử dụng: verify(mock).method(params)
 *
 * 4. MOCK method chain:
 *    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
 *    when(valueOperations.get(key)).thenReturn(value);
 *
 * 5. VERIFY với Duration và TimeUnit:
 *    verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofDays(7)));
 *    verify(valueOperations).set(eq(key), eq(value), eq(ttl), eq(TimeUnit.SECONDS));
 *
 * ========================================
 * CONSTANTS TRONG TokenService:
 * ========================================
 * - REFRESH_TOKEN_PREFIX = "refresh_token:"
 * - BLACKLIST_TOKEN_PREFIX = "blacklist_token:"
 * - TTL cho refresh token = 7 days
 *
 * KEY FORMAT:
 * - Refresh token: "refresh_token:user@example.com"
 * - Blacklist: "blacklist_token:actual_token_string"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService Unit Tests - Bài Tập Thực Hành")
class TokenServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    private String testEmail;
    private String testRefreshToken;
    private String testAccessToken;

    /**
     * Setup chạy trước mỗi test
     * LƯU Ý: Phải mock redisTemplate.opsForValue() trả về valueOperations
     */
    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testRefreshToken = "refresh_token_123456";
        testAccessToken = "access_token_abcdef";

        // Mock redisTemplate.opsForValue() để trả về valueOperations
        // Sử dụng lenient() để tránh UnnecessaryStubbingException khi không dùng valueOperations
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }


    @Test
    @DisplayName("saveRefreshToken - Khi lưu token - Nên gọi Redis set với đúng params")
    void saveRefreshToken_shouldSaveToRedisWithCorrectParams() {
        // TODO: Implement test case này

        tokenService.saveRefreshToken(testEmail, testRefreshToken);
        verify(valueOperations, times(1)).set(
                eq("refresh_token:" + testEmail),
                eq(testRefreshToken),
                eq(Duration.ofDays(7))
        );

    }


    @Test
    @DisplayName("getRefreshToken - Khi token tồn tại - Nên trả về Optional chứa token")
    void getRefreshToken_whenTokenExists_shouldReturnOptionalWithToken() {
        // Arrange
        when(valueOperations.get("refresh_token:" + testEmail)).thenReturn(testRefreshToken);
        // Act
        Optional<String> result = tokenService.getRefreshToken(testEmail);
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRefreshToken, result.get());
        verify(valueOperations).get("refresh_token:" + testEmail);
    }


    @Test
    @DisplayName("getRefreshToken - Khi token không tồn tại - Nên trả về Optional.empty()")
    void getRefreshToken_whenTokenNotExists_shouldReturnEmptyOptional() {
        // Arrange
        when(valueOperations.get("refresh_token:" + testEmail)).thenReturn(null);
        // Act
        Optional<String> result = tokenService.getRefreshToken(testEmail);
        // Assert
        assertTrue(result.isEmpty());
        verify(valueOperations).get("refresh_token:" + testEmail);
    }


    @Test
    @DisplayName("deleteRefreshToken - Khi xóa token - Nên gọi Redis delete với key đúng")
    void deleteRefreshToken_shouldCallRedisDeleteWithCorrectKey() {
        // Act
        tokenService.deleteRefreshToken(testEmail);
        // Assert
        verify(redisTemplate, times(1)).delete("refresh_token:" + testEmail);
    }


    @Test
    @DisplayName("validateRefreshToken - Khi token match - Nên trả về true")
    void validateRefreshToken_whenTokenMatches_shouldReturnTrue() {
        // Arrange
        when(valueOperations.get("refresh_token:" + testEmail)).thenReturn(testRefreshToken);
        // Act
        boolean result = tokenService.validateRefreshToken(testEmail, testRefreshToken);
        // Assert
        assertTrue(result);
    }


    @Test
    @DisplayName("validateRefreshToken - Khi token không match - Nên trả về false")
    void validateRefreshToken_whenTokenDoesNotMatch_shouldReturnFalse() {
        // Arrange
        when(valueOperations.get("refresh_token:" + testEmail)).thenReturn("different_token");
        // Act
        boolean result = tokenService.validateRefreshToken(testEmail, testRefreshToken);
        // Assert
        assertFalse(result);
    }


    @Test
    @DisplayName("validateRefreshToken - Khi token không tồn tại - Nên trả về false")
    void validateRefreshToken_whenTokenNotExists_shouldReturnFalse() {
        // Arrange
        when(valueOperations.get("refresh_token:" + testEmail)).thenReturn(null);
        // Act
        boolean result = tokenService.validateRefreshToken(testEmail, testRefreshToken);
        // Assert
        assertFalse(result);
    }


    @Test
    @DisplayName("blacklistAccessToken - Khi blacklist token - Nên lưu vào Redis với TTL")
    void blacklistAccessToken_shouldSaveToRedisWithTTL() {
        // Arrange
        long remainingTime = 3600L; // 1 hour
        // Act
        tokenService.blacklistAccessToken(testAccessToken, testEmail, remainingTime);
        // Assert
        verify(valueOperations, times(1)).set(
                eq("blacklist_token:" + testAccessToken),
                eq(testEmail),
                eq(remainingTime),
                eq(TimeUnit.SECONDS)
        );
    }


    @Test
    @DisplayName("isAccessTokenBlacklisted - Khi token trong blacklist - Nên trả về true")
    void isAccessTokenBlacklisted_whenTokenInBlacklist_shouldReturnTrue() {
        // Arrange
        when(redisTemplate.hasKey("blacklist_token:" + testAccessToken)).thenReturn(Boolean.TRUE);
        // Act
        boolean result = tokenService.isAccessTokenBlacklisted(testAccessToken);
        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey("blacklist_token:" + testAccessToken);
    }


    @Test
    @DisplayName("isAccessTokenBlacklisted - Khi token không trong blacklist - Nên trả về false")
    void isAccessTokenBlacklisted_whenTokenNotInBlacklist_shouldReturnFalse() {
        // Arrange
        when(redisTemplate.hasKey("blacklist_token:" + testAccessToken)).thenReturn(Boolean.FALSE);
        // Act
        boolean result = tokenService.isAccessTokenBlacklisted(testAccessToken);
        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey("blacklist_token:" + testAccessToken);
    }


    @Test
    @DisplayName("removeFromBlacklist - Khi xóa token - Nên gọi Redis delete")
    void removeFromBlacklist_shouldCallRedisDelete() {
        // Act
        tokenService.removeFromBlacklist(testAccessToken);
        // Assert
        verify(redisTemplate, times(1)).delete("blacklist_token:" + testAccessToken);
    }


    @Test
    @DisplayName("getBlacklistedTokenOwner - Khi lấy owner - Nên trả về email")
    void getBlacklistedTokenOwner_shouldReturnEmail() {
        // Arrange
        when(valueOperations.get("blacklist_token:" + testAccessToken)).thenReturn(testEmail);
        // Act
        String result = tokenService.getBlacklistedTokenOwner(testAccessToken);
        // Assert
        assertNotNull(result);
        assertEquals(testEmail, result);
        verify(valueOperations).get("blacklist_token:" + testAccessToken);
    }
}

