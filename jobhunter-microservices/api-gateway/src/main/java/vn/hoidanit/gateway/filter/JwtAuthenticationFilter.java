package vn.hoidanit.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.hoidanit.gateway.service.TokenBlacklistService;
import vn.hoidanit.gateway.util.SignatureUtil;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${hoidanit.jwt.base64-secret}")
    private String jwtSecret;

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(TokenBlacklistService tokenBlacklistService) {
        super(Config.class);
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                final String email = claims.getSubject();
                Object userObj = claims.get("user");

                final String userId;
                if (userObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) userObj;
                    Object idObj = userMap.get("id");
                    userId = idObj != null ? String.valueOf(idObj) : null;
                } else {
                    userId = null;
                }

                Object permissionsObj = claims.get("permission");
                final String roles;
                if (permissionsObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> permissions = (java.util.List<String>) permissionsObj;
                    roles = String.join(",", permissions);
                } else {
                    roles = "";
                }

                return tokenBlacklistService.isAccessTokenBlacklisted(token)
                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                log.warn("Token is blacklisted: {}", email);
                                return onError(exchange, "Token has been revoked. Please login again.", HttpStatus.UNAUTHORIZED);
                            }

                            long timestamp = System.currentTimeMillis();
                            String signatureData = SignatureUtil.createSignatureData(userId, email, timestamp);
                            String signature = SignatureUtil.generateSignature(signatureData, gatewaySignatureSecret);
                            
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Email", email)
                                    .header("X-User-Roles", roles)
                                    .header("X-Gateway-Signature", signature)
                                    .header("X-Gateway-Timestamp", String.valueOf(timestamp))
                                    .build();

                            log.info("JWT validated for user: {} (roles: {})", email, roles);
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        });

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Authentication error: {}", err);
        return response.setComplete();
    }

    public static class Config {
    }
}

