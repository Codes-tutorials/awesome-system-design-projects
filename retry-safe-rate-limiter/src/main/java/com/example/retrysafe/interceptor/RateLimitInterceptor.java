package com.example.retrysafe.interceptor;

import com.example.retrysafe.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            return true; // Skip if no user
        }

        RateLimiterService.RateLimitResult result = rateLimiterService.tryAcquire(userId);

        if (!result.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(result.retryAfter()));
            response.getWriter().write("Too Many Requests");
            return false;
        }
        
        // Tag request with user ID for afterCompletion
        request.setAttribute("X-User-Id", userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String userId = (String) request.getAttribute("X-User-Id");
        
        // Check if request failed (5xx status) or Exception was thrown
        // Note: Spring handles exceptions by setting status, but if an exception bubbled up here, it might not be set yet?
        // Usually Spring sets status 500 if exception occurs.
        
        int status = response.getStatus();
        boolean isServerError = status >= 500 && status < 600;
        
        if (userId != null && (isServerError || ex != null)) {
            log.warn("Request failed with status {} or exception. Refunding token for user {}", status, userId);
            rateLimiterService.refundToken(userId);
        }
    }
}
