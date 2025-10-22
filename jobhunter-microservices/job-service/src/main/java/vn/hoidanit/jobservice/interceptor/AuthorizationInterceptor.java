package vn.hoidanit.jobservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.hoidanit.jobservice.annotation.RequireRole;
import vn.hoidanit.jobservice.exception.ForbiddenException;
import vn.hoidanit.jobservice.util.GatewaySignatureUtil;
import vn.hoidanit.jobservice.util.SecurityUtil;

@Component
@Slf4j
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    @Value("${gateway.signature.enabled:true}")
    private boolean signatureVerificationEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // ✅ SECURITY: Verify request comes from Gateway
        if (signatureVerificationEnabled) {
            String signature = request.getHeader("X-Gateway-Signature");
            String userId = request.getHeader("X-User-Id");
            String email = request.getHeader("X-User-Email");
            String timestamp = request.getHeader("X-Gateway-Timestamp");

            if (!GatewaySignatureUtil.verifyGatewaySignature(signature, userId, email, timestamp, gatewaySignatureSecret)) {
                log.error("🚨 SECURITY ALERT: Unauthorized direct access attempt!");
                throw new ForbiddenException("Unauthorized request origin");
            }
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        RequireRole requireRole = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (requireRole == null) {
            return true;
        }

        String userRoles = SecurityUtil.getCurrentUserRoles();
        Long userId = SecurityUtil.getCurrentUserId();

        if (userId == null || userRoles == null) {
            throw new ForbiddenException("Authentication required");
        }

        String[] requiredRoles = requireRole.value();
        boolean requireAll = requireRole.requireAll();
        boolean authorized = requireAll ? hasAllRoles(userRoles, requiredRoles) : hasAnyRole(userRoles, requiredRoles);

        if (!authorized) {
            log.warn("Authorization failed for user {} (roles: {}). Required: {}", userId, userRoles, String.join(", ", requiredRoles));
            throw new ForbiddenException("Insufficient permissions");
        }

        return true;
    }

    private boolean hasAnyRole(String userRoles, String[] requiredRoles) {
        for (String role : requiredRoles) {
            if (userRoles.contains(role)) return true;
        }
        return false;
    }

    private boolean hasAllRoles(String userRoles, String[] requiredRoles) {
        for (String role : requiredRoles) {
            if (!userRoles.contains(role)) return false;
        }
        return true;
    }
}

