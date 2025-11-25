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

    private static final String HEADER_GATEWAY_SIGNATURE = "X-Gateway-Signature";
    private static final String HEADER_GATEWAY_TIMESTAMP = "X-Gateway-Timestamp";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    @Value("${gateway.signature.enabled:true}")
    private boolean gatewaySignatureEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String gatewaySignature = request.getHeader(HEADER_GATEWAY_SIGNATURE);
        String gatewayTimestamp = request.getHeader(HEADER_GATEWAY_TIMESTAMP);
        String userId = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);
        String userRoles = request.getHeader(HEADER_USER_ROLES);

        if (gatewaySignature != null && gatewayTimestamp != null && gatewaySignatureEnabled) {
            log.debug("Validating Gateway signature for user: {}", userEmail);

            String signatureData = createSignatureData(userId, userEmail, gatewayTimestamp);
            String expectedSignature = generateSignature(signatureData, gatewaySignatureSecret);

            if (gatewaySignature.equals(expectedSignature)) {
                log.debug("Gateway signature validated for user: {}", userEmail);

                List<SimpleGrantedAuthority> authorities = parseAuthorities(userRoles);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
                return;
            } else {
                log.error("Invalid Gateway signature - request rejected");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid Gateway signature\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String createSignatureData(String userId, String email, String timestamp) {
        return userId + ":" + email + ":" + timestamp;
    }

    private String generateSignature(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
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

