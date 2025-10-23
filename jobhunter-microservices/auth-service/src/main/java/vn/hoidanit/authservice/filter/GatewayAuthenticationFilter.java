package vn.hoidanit.authservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    @Value("${gateway.signature.enabled:true}")
    private boolean gatewaySignatureEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if request comes from Gateway (has X-Gateway-Signature header)
        String gatewaySignature = request.getHeader("X-Gateway-Signature");
        String gatewayTimestamp = request.getHeader("X-Gateway-Timestamp");
        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-User-Email");
        String userRoles = request.getHeader("X-User-Roles");

        // If Gateway signature exists and is enabled, validate it
        if (gatewaySignature != null && gatewayTimestamp != null && gatewaySignatureEnabled) {
            log.debug("Request from Gateway detected, validating Gateway signature");

            // Validate Gateway signature
            String signatureData = createSignatureData(userId, userEmail, gatewayTimestamp);
            String expectedSignature = generateSignature(signatureData, gatewaySignatureSecret);

            if (gatewaySignature.equals(expectedSignature)) {
                log.debug("Gateway signature validated successfully for user: {}", userEmail);

                List<SimpleGrantedAuthority> authorities = parseAuthorities(userRoles);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("User authenticated via Gateway: {} with roles: {}", userEmail, userRoles);

                // Skip remaining filters (including JWT validation) - trust Gateway
                filterChain.doFilter(request, response);
                return;
            } else {
                log.error("Invalid Gateway signature! Request rejected.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid Gateway signature\"}");
                return;
            }
        }

        // Continue filter chain for other requests (direct calls with JWT)
        filterChain.doFilter(request, response);
    }

    private String createSignatureData(String userId, String email, String timestamp) {
        return userId + ":" + email + ":" + timestamp;
    }

    private String generateSignature(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private List<SimpleGrantedAuthority> parseAuthorities(String rolesString) {
        if (rolesString == null || rolesString.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

