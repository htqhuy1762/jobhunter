package vn.hoidanit.authservice.aspect;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import vn.hoidanit.authservice.annotation.RateLimit;
import vn.hoidanit.authservice.util.SecurityUtil;

/**
 * Aspect to handle @RateLimit annotation
 * Applies rate limiting to annotated methods
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("@annotation(rateLimit)")
    public Object aroundRateLimitedMethod(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String rateLimiterName = rateLimit.name();
        io.github.resilience4j.ratelimiter.RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterName);

        try {
            rateLimiter.acquirePermission();

            String userEmail = SecurityUtil.getCurrentUserLogin().orElse("anonymous");
            log.debug("Rate limit check passed for user: {} on {}", userEmail, rateLimiterName);

            return joinPoint.proceed();

        } catch (RequestNotPermitted e) {
            String userEmail = SecurityUtil.getCurrentUserLogin().orElse("anonymous");
            log.warn("Rate limit exceeded for user: {} on {}", userEmail, rateLimiterName);

            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded. Please try again later."
            );
        }
    }
}