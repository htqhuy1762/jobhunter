package vn.hoidanit.fileservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.hoidanit.fileservice.annotation.RequireRole;
import vn.hoidanit.fileservice.util.SecurityUtil;

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
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied\"}");
            return false;
        }

        String[] requiredRoles = requireRole.value();
        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> userRoles.contains(role));

        if (!hasRole) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied\"}");
            return false;
        }

        return true;
    }
}


