package vn.hoidanit.companyservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.hoidanit.companyservice.util.SignatureUtil;

import java.io.IOException;

/**
 * Filter to verify Gateway Signature
 * Ensures requests come from API Gateway, not direct bypass
 */
@Component
@Slf4j
public class GatewaySignatureFilter extends OncePerRequestFilter {

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    @Value("${gateway.signature.enabled:true}")
    private boolean signatureEnabled;

    @Value("${gateway.signature.timestamp-tolerance-seconds:60}")
    private long timestampToleranceSeconds;

    private static final String HEADER_SIGNATURE = "X-Gateway-Signature";
    private static final String HEADER_TIMESTAMP = "X-Gateway-Timestamp";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip signature check if disabled (for local development)
        if (!signatureEnabled) {
            log.debug("Gateway signature verification is disabled");
            filterChain.doFilter(request, response);
            return;
        }

        // Get signature from header
        String signature = request.getHeader(HEADER_SIGNATURE);
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        String userId = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);

        // Verify signature exists
        if (signature == null || timestampStr == null) {
            log.warn("Request missing gateway signature or timestamp. Path: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Direct access not allowed. Request must go through API Gateway.\"}");
            return;
        }

        try {
            // Parse timestamp
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();

            // Check timestamp validity (prevent replay attacks)
            if (Math.abs(currentTime - timestamp) > timestampToleranceSeconds * 1000) {
                log.warn("Gateway signature timestamp expired. Path: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Request timestamp expired\"}");
                return;
            }

            // Verify signature
            String signatureData = SignatureUtil.createSignatureData(userId, userEmail, timestamp);
            String expectedSignature = SignatureUtil.generateSignature(signatureData, gatewaySignatureSecret);

            if (!signature.equals(expectedSignature)) {
                log.warn("Invalid gateway signature. Path: {}, Expected: {}, Got: {}",
                        request.getRequestURI(), expectedSignature, signature);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid gateway signature\"}");
                return;
            }

            log.debug("Gateway signature verified successfully for user: {}", userEmail);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error verifying gateway signature", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Error verifying request\"}");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }
}