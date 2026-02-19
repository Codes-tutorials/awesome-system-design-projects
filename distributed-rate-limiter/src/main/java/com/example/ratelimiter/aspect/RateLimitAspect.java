package com.example.ratelimiter.aspect;

import com.example.ratelimiter.annotation.RateLimit;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key();

        if (rateLimit.isUserSpecific()) {
            // In a real app, get User ID from SecurityContext or JWT.
            // For demo, we use the "User-Id" header or IP.
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = request.getHeader("User-Id");
            if (userId == null) {
                userId = request.getRemoteAddr(); // Fallback to IP
            }
            key = key + ":" + userId;
        }

        boolean allowed = rateLimiterService.isAllowed(key, rateLimit.rate(), rateLimit.capacity());

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }

        return joinPoint.proceed();
    }
}
