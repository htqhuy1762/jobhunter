package vn.hoidanit.jobservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.hoidanit.jobservice.annotation.RequireRole;
import vn.hoidanit.jobservice.util.SecurityUtil;

import java.util.Arrays;

@Component
@Slf4j
public class RoleCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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

        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("Access denied - No roles found for request: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied - No roles found\"}");
            return false;
        }

        String[] requiredRoles = requireRole.value();
        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> userRoles.contains(role));

        if (!hasRole) {
            log.warn("Access denied - User roles: {} do not match required roles: {}",
                    userRoles, Arrays.toString(requiredRoles));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Access denied - Required roles: %s\"}",
                String.join(", ", requiredRoles)
            ));
            return false;
        }

        log.debug("Access granted for: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
}
