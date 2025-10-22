package vn.hoidanit.companyservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.hoidanit.companyservice.annotation.RequireRole;
import vn.hoidanit.companyservice.exception.ForbiddenException;
import vn.hoidanit.companyservice.util.GatewaySignatureUtil;
import vn.hoidanit.companyservice.util.SecurityUtil;

/**
 * Interceptor to check role-based authorization before controller execution
 */
@Component
@Slf4j
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    @Value("${gateway.signature.enabled:true}")
    private boolean signatureVerificationEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // ✅ SECURITY LAYER 1: Verify request comes from Gateway (not direct bypass)
        if (signatureVerificationEnabled) {
            String signature = request.getHeader("X-Gateway-Signature");
            String userId = request.getHeader("X-User-Id");
            String email = request.getHeader("X-User-Email");
            String timestamp = request.getHeader("X-Gateway-Timestamp");

            if (!GatewaySignatureUtil.verifyGatewaySignature(signature, userId, email, timestamp, gatewaySignatureSecret)) {
                log.error("🚨 SECURITY ALERT: Request did NOT come from Gateway! " +
                        "Potential bypass attack from IP: {}, Path: {}, Method: {}",
                        request.getRemoteAddr(), request.getRequestURI(), request.getMethod());
                throw new ForbiddenException("Unauthorized request origin. Requests must go through API Gateway.");
            }
            log.debug("✅ Gateway signature verified - request is legitimate");
        }

        // ✅ SECURITY LAYER 2: Check role-based authorization
        // Only check for controller methods
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check method-level annotation first
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);

        RequireRole requireRole = methodAnnotation != null ? methodAnnotation : classAnnotation;

        // No authorization required
        if (requireRole == null) {
            return true;
        }

        // Get user info from headers (set by API Gateway)
        String userRoles = SecurityUtil.getCurrentUserRoles();
        Long userId = SecurityUtil.getCurrentUserId();

        if (userId == null || userRoles == null) {
            log.warn("Authorization check failed: No user context found");
            throw new ForbiddenException("Authentication required");
        }

        String[] requiredRoles = requireRole.value();
        boolean requireAll = requireRole.requireAll();

        // Check authorization
        boolean authorized;
        if (requireAll) {
            // AND logic - must have ALL roles
            authorized = hasAllRoles(userRoles, requiredRoles);
        } else {
            // OR logic - must have at least ONE role
            authorized = hasAnyRole(userRoles, requiredRoles);
        }

        if (!authorized) {
            log.warn("Authorization failed for user {} (roles: {}). Required roles: {} (requireAll: {})",
                    userId, userRoles, String.join(", ", requiredRoles), requireAll);
            throw new ForbiddenException("Insufficient permissions. Required roles: " + String.join(" or ", requiredRoles));
        }

        log.debug("Authorization successful for user {} with roles: {}", userId, userRoles);
        return true;
    }

    /**
     * Check if user has ANY of the required roles (OR logic)
     */
    private boolean hasAnyRole(String userRoles, String[] requiredRoles) {
        for (String requiredRole : requiredRoles) {
            if (userRoles.contains(requiredRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has ALL of the required roles (AND logic)
     */
    private boolean hasAllRoles(String userRoles, String[] requiredRoles) {
        for (String requiredRole : requiredRoles) {
            if (!userRoles.contains(requiredRole)) {
                return false;
            }
        }
        return true;
    }
}

