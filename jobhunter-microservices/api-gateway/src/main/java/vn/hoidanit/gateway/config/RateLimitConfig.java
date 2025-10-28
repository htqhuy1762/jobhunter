package vn.hoidanit.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    /**
     * Rate limiting based on IP address
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }

    /**
     * Redis Rate Limiter bean
     * Default configuration: 10 requests per second with burst capacity of 20
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * Rate limiting based on user (from JWT token)
     * You can switch to this by changing the KeyResolver bean name
     */
    // @Bean
    // public KeyResolver userKeyResolver() {
    //     return exchange -> Mono.just(
    //             exchange.getRequest().getHeaders()
    //                     .getFirst("X-User-Email") != null
    //                     ? exchange.getRequest().getHeaders().getFirst("X-User-Email")
    //                     : Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
    //                             .getAddress().getHostAddress()
    //     );
    // }
}

