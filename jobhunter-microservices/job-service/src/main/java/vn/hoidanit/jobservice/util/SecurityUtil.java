package vn.hoidanit.jobservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
public class SecurityUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    private static Optional<HttpServletRequest> getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return Optional.ofNullable(attributes).map(ServletRequestAttributes::getRequest);
        } catch (Exception e) {
            log.warn("Could not get current request: {}", e.getMessage());
            return Optional.empty();
        }
    }

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

    public static String getCurrentUserEmail() {
        return getCurrentRequest()
                .map(request -> request.getHeader(HEADER_USER_EMAIL))
                .orElse(null);
    }

    public static String getCurrentUserRoles() {
        return getCurrentRequest()
                .map(request -> request.getHeader(HEADER_USER_ROLES))
                .orElse(null);
    }

    public static boolean hasRole(String role) {
        String roles = getCurrentUserRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.contains(role);
    }

    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    public static Optional<String> getCurrentUserLogin() {
        return Optional.ofNullable(getCurrentUserEmail());
    }

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

