package vn.hoidanit.authservice.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import vn.hoidanit.authservice.resolver.CustomPageableResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // Custom Pageable resolver for 1-based page indexing
        resolvers.add(new CustomPageableResolver());
    }
}

