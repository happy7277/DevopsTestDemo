package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiAccess implements WebMvcConfigurer {
    private final JwtTokens tokens;
    private final RateLimiter limiter;

    ApiAccess(JwtTokens tokens, RateLimiter limiter) {
        this.tokens = tokens;
        this.limiter = limiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                boolean login = request.getServletPath().equals("/api/auth/login");
                var subject = tokens.subject(request.getHeader("Authorization"));
                String bucket = subject.orElseGet(() -> request.getRemoteAddr());
                try {
                    if (!limiter.allow(bucket)) {
                        response.setHeader("Retry-After", "60");
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
                    }
                } catch (DataAccessException error) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Rate limiter unavailable");
                }
                if (!login && subject.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid bearer token required");
                }
                subject.ifPresent(actor -> request.setAttribute("actor", actor));
                return true;
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/health");
    }
}
