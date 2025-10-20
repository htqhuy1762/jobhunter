package vn.hoidanit.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    /**
     * Rate limiting based on IP address
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
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

