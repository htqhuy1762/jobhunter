package vn.hoidanit.notificationservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.hoidanit.notificationservice.util.SignatureUtil;

import java.io.IOException;

@Component
@Slf4j
public class GatewaySignatureFilter extends OncePerRequestFilter {

    private static final String HEADER_SIGNATURE = "X-Gateway-Signature";
    private static final String HEADER_TIMESTAMP = "X-Gateway-Timestamp";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ERROR_DIRECT_ACCESS = "{\"error\":\"Direct access not allowed. Must go through API Gateway.\"}";
    private static final String ERROR_TIMESTAMP_EXPIRED = "{\"error\":\"Request timestamp expired\"}";
    private static final String ERROR_INVALID_SIGNATURE = "{\"error\":\"Invalid gateway signature\"}";
    private static final String ERROR_VERIFICATION_FAILED = "{\"error\":\"Error verifying request\"}";
    private static final long MILLIS_PER_SECOND = 1000L;

    @Value("${gateway.signature.secret}")
    private String gatewaySignatureSecret;

    @Value("${gateway.signature.enabled:true}")
    private boolean signatureEnabled;

    @Value("${gateway.signature.timestamp-tolerance-seconds:60}")
    private long timestampToleranceSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!signatureEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            log.debug("Bypassing signature check for GET request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String signature = request.getHeader(HEADER_SIGNATURE);
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        String userId = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);

        if (signature == null || timestampStr == null) {
            log.warn("Request missing gateway signature. Path: {}", request.getRequestURI());
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ERROR_DIRECT_ACCESS);
            return;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();

            if (Math.abs(currentTime - timestamp) > timestampToleranceSeconds * MILLIS_PER_SECOND) {
                log.warn("Timestamp expired. Path: {}", request.getRequestURI());
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ERROR_TIMESTAMP_EXPIRED);
                return;
            }

            String signatureData = SignatureUtil.createSignatureData(userId, userEmail, timestamp);

            if (!SignatureUtil.verifySignature(signatureData, signature, gatewaySignatureSecret)) {
                log.warn("Invalid signature. Path: {}", request.getRequestURI());
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ERROR_INVALID_SIGNATURE);
                return;
            }

            log.debug("Gateway verified for user: {}", userEmail);
            filterChain.doFilter(request, response);

        } catch (NumberFormatException e) {
            log.error("Invalid timestamp format: {}", timestampStr);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, ERROR_VERIFICATION_FAILED);
        } catch (Exception e) {
            log.error("Error verifying gateway signature", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_VERIFICATION_FAILED);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || path.equals("/api/v1/subscribers");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(CONTENT_TYPE_JSON);
        response.getWriter().write(message);
    }
}

