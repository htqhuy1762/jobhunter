package vn.hoidanit.jobservice.config;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.hoidanit.jobservice.interceptor.RoleCheckInterceptor;
import vn.hoidanit.jobservice.resolver.CustomPageableResolver;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RoleCheckInterceptor roleCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Role-based access control
        registry.addInterceptor(roleCheckInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/actuator/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // Custom Pageable resolver for 1-based page indexing
        resolvers.add(new CustomPageableResolver());
    }
}

