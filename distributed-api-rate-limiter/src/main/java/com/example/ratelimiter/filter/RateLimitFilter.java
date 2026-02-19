package com.example.ratelimiter.filter;

import com.example.ratelimiter.service.HybridRateLimiter;
import com.example.ratelimiter.service.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final HybridRateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Identify User: Header > IP
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = request.getRemoteAddr();
        }

        // Check Limit
        RedisRateLimiter.RateLimitResult result = rateLimiter.allowRequest(userId);

        // Add Headers
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.remaining()));
        
        if (result.allowed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(result.retryAfter()));
            response.getWriter().write("Too Many Requests. Retry after " + result.retryAfter() + "s");
        }
    }
}
