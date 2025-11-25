package vn.hoidanit.notificationservice.util;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
public class SecurityUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    private SecurityUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Optional<String> getCurrentUserLogin() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userEmail = request.getHeader(HEADER_USER_EMAIL);
                return Optional.ofNullable(userEmail);
            }
        } catch (Exception e) {
            log.warn("Could not get current user login: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader(HEADER_USER_ID);
                return Optional.ofNullable(userId);
            }
        } catch (Exception e) {
            log.warn("Could not get current user ID: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentUserRoles() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String roles = request.getHeader(HEADER_USER_ROLES);
                return Optional.ofNullable(roles);
            }
        } catch (Exception e) {
            log.warn("Could not get current user roles: {}", e.getMessage());
        }
        return Optional.empty();
    }
}


