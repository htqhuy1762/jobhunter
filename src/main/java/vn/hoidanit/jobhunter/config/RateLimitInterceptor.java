package vn.hoidanit.jobhunter.config;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.hoidanit.jobhunter.service.RateLimitService;
import vn.hoidanit.jobhunter.util.error.RateLimitException;

/**
 * Interceptor để kiểm tra rate limiting
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Chỉ xử lý các method có annotation @RateLimit
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return true;
        }

        // Lấy IP address của client
        String clientIp = getClientIP(request);
        String key = rateLimit.keyPrefix() + ":" + request.getRequestURI() + ":" + clientIp;

        // Kiểm tra rate limit
        boolean allowed = rateLimitService.allowRequest(key, rateLimit.limit(), rateLimit.duration());

        if (!allowed) {
            // Thêm headers thông tin rate limit
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit.limit()));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimitService.getTimeUntilReset(key)));

            throw new RateLimitException(rateLimit.message());
        }

        // Thêm headers thông tin rate limit
        int remaining = rateLimitService.getRemainingRequests(key, rateLimit.limit());
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimitService.getTimeUntilReset(key)));

        return true;
    }

    /**
     * Lấy IP address của client (xử lý cả trường hợp có proxy)
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // Lấy IP đầu tiên trong chuỗi X-Forwarded-For
        return xfHeader.split(",")[0].trim();
    }
}
