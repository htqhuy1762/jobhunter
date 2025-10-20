package vn.hoidanit.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Public endpoints - no authentication required
                        .pathMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .pathMatchers("/api/v1/auth/refresh").permitAll()
                        .pathMatchers("/api/v1/subscribers").permitAll()
                        .pathMatchers("/api/v1/jobs/**", "/api/v1/companies/**").permitAll()

                        // Actuator endpoints
                        .pathMatchers("/actuator/**").permitAll()

                        // Fallback endpoints
                        .pathMatchers("/fallback/**").permitAll()

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .build();
    }
}

