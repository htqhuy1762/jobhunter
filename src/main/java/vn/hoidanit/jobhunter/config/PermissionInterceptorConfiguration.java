package vn.hoidanit.jobhunter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import vn.hoidanit.jobhunter.service.UserService;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer { 
    private final RateLimitInterceptor rateLimitInterceptor;
    private final UserService userService;

    public PermissionInterceptorConfiguration(RateLimitInterceptor rateLimitInterceptor, UserService userService) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.userService = userService;
    }

    @Bean
    PermissionInterceptor getPermissionInterceptor() { 
        return new PermissionInterceptor(userService);
    }
 
    @Override 
    public void addInterceptors(InterceptorRegistry registry) { 
        String[] whiteList = { 
                "/", "/api/v1/auth/**", "/storage/**", 
                "/api/v1/companies/**", "/api/v1/jobs/**", "/api/v1/skills/**", "/api/v1/files", 
                "/api/v1/resumes/**",
                "/api/v1/subscribers/**"
        }; 

        // Add rate limit interceptor (applied to all endpoints)
        registry.addInterceptor(rateLimitInterceptor);

        // Add permission interceptor (excluded for whiteList)
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList); 
    } 
}
