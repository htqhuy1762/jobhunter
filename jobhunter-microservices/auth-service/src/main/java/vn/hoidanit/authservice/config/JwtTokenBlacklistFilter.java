package vn.hoidanit.authservice.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.hoidanit.authservice.service.TokenService;

/**
 * Filter để kiểm tra Access Token có trong blacklist không
 * Chạy sau khi Spring Security đã validate JWT
 * Nếu token trong blacklist -> trả về 401 Unauthorized
 */
@Component
public class JwtTokenBlacklistFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtTokenBlacklistFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lấy Authentication từ SecurityContext (đã được set bởi Spring Security)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu user đã authenticated và principal là JWT
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt) {

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String tokenValue = jwt.getTokenValue();

            // Kiểm tra token có trong blacklist không
            if (tokenService.isAccessTokenBlacklisted(tokenValue)) {
                // Token bị blacklist -> Clear security context và trả về 401
                SecurityContextHolder.clearContext();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                    "{\"statusCode\":401,\"error\":\"Unauthorized\",\"message\":\"Token has been revoked. Please login again.\"}"
                );
                return; // Dừng filter chain
            }
        }

        // Token hợp lệ hoặc không cần authentication -> tiếp tục
        filterChain.doFilter(request, response);
    }
}

