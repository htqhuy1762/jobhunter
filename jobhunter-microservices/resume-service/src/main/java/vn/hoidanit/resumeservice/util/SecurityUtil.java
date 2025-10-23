package vn.hoidanit.resumeservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Utility class to extract user information from request headers
 * Headers are set by API Gateway after JWT validation
 */
@Slf4j
public class SecurityUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    /**
     * Get current HTTP request
     */
    private static Optional<HttpServletRequest> getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return Optional.ofNullable(attributes).map(ServletRequestAttributes::getRequest);
        } catch (Exception e) {
            log.warn("Could not get current request: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get current user ID from request header (set by API Gateway)
     * @return User ID or null if not found
     */
    public static Long getCurrentUserId() {
        return getCurrentRequest()
                .map(request -> request.getHeader(HEADER_USER_ID))
                .map(userId -> {
                    try {
                        return Long.parseLong(userId);
                    } catch (NumberFormatException e) {
                        log.error("Invalid user ID format: {}", userId);
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * Get current user email from request header (set by API Gateway)
     * @return User email or null if not found
     */
    public static String getCurrentUserEmail() {
        return getCurrentRequest()
                .map(request -> request.getHeader(HEADER_USER_EMAIL))
                .orElse(null);
    }

    /**
     * Get current user roles from request header (set by API Gateway)
     * @return User roles (comma-separated) or null if not found
     */
    public static String getCurrentUserRoles() {
        return getCurrentRequest()
                .map(request -> request.getHeader(HEADER_USER_ROLES))
                .orElse(null);
    }

    /**
     * Check if current user has a specific role
     * @param role Role name to check
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        String roles = getCurrentUserRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.contains(role);
    }

    /**
     * Check if current user is authenticated (has user ID)
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    /**
     * Get current user login (email) - for compatibility with JPA @PrePersist/@PreUpdate
     * @return Optional containing user email
     */
    public static Optional<String> getCurrentUserLogin() {
        return Optional.ofNullable(getCurrentUserEmail());
    }

    /**
     * Get current user info for logging
     * @return String with user info
     */
    public static String getCurrentUserInfo() {
        Long userId = getCurrentUserId();
        String email = getCurrentUserEmail();
        String roles = getCurrentUserRoles();

        if (userId == null) {
            return "Anonymous User";
        }

        return String.format("User[id=%d, email=%s, roles=%s]", userId, email, roles);
    }
}
