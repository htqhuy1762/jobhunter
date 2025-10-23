package vn.hoidanit.resumeservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.hoidanit.resumeservice.util.SignatureUtil;

import java.io.IOException;

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

        if (!signatureEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String signature = request.getHeader(HEADER_SIGNATURE);
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        String userId = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);

        if (signature == null || timestampStr == null) {
            log.warn("Request missing gateway signature. Path: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Direct access not allowed. Request must go through API Gateway.\"}");
            return;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();

            if (Math.abs(currentTime - timestamp) > timestampToleranceSeconds * 1000) {
                log.warn("Gateway signature timestamp expired");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Request timestamp expired\"}");
                return;
            }

            String signatureData = SignatureUtil.createSignatureData(userId, userEmail, timestamp);
            String expectedSignature = SignatureUtil.generateSignature(signatureData, gatewaySignatureSecret);

            if (!signature.equals(expectedSignature)) {
                log.warn("Invalid gateway signature");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid gateway signature\"}");
                return;
            }

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
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }
}

