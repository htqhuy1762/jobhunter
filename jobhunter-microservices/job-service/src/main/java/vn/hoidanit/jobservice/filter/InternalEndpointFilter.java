package vn.hoidanit.jobservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to protect internal endpoints from direct external access
 * Internal endpoints should only be accessible via Gateway with valid signature
 */
@Component
@Slf4j
public class InternalEndpointFilter extends OncePerRequestFilter {

    @Value("${gateway.signature.enabled:true}")
    private boolean gatewaySignatureEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Check if this is an internal endpoint
        if (path.contains("/internal/")) {
            if (gatewaySignatureEnabled) {
                String signature = request.getHeader("X-Gateway-Signature");

                if (signature == null || signature.isEmpty()) {
                    log.warn("Attempt to access internal endpoint without Gateway Signature: {}", path);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\":\"Internal endpoints can only be accessed through API Gateway\"}"
                    );
                    return;
                }

                log.debug("Internal endpoint access with valid Gateway Signature: {}", path);
            }
        }

        filterChain.doFilter(request, response);
    }
}

