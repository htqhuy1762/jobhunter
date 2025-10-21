package vn.hoidanit.resumeservice.util;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SecurityUtil {

    /**
     * Get current user login from request headers (set by API Gateway)
     * API Gateway adds X-User-Email header after JWT validation
     */
    public static Optional<String> getCurrentUserLogin() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userEmail = request.getHeader("X-User-Email");
                return Optional.ofNullable(userEmail);
            }
        } catch (Exception e) {
            // No request context available
        }
        return Optional.empty();
    }

    /**
     * Get current user ID from request headers
     */
    public static Optional<String> getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");
                return Optional.ofNullable(userId);
            }
        } catch (Exception e) {
            // No request context available
        }
        return Optional.empty();
    }

    /**
     * Get current user roles from request headers
     */
    public static Optional<String> getCurrentUserRoles() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String roles = request.getHeader("X-User-Roles");
                return Optional.ofNullable(roles);
            }
        } catch (Exception e) {
            // No request context available
        }
        return Optional.empty();
    }
}

