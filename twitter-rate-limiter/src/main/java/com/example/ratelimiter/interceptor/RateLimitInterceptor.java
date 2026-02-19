package com.example.ratelimiter.interceptor;

import com.example.ratelimiter.config.PricingPlan;
import com.example.ratelimiter.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. Global Rate Limit (System Protection)
        // Key: "GLOBAL_LIMIT"
        Bucket globalBucket = rateLimitingService.resolveBucket("GLOBAL_LIMIT", PricingPlan.GLOBAL_SYSTEM);
        ConsumptionProbe globalProbe = globalBucket.tryConsumeAndReturnRemaining(1);
        
        if (!globalProbe.isConsumed()) {
            log.warn("Global rate limit exceeded!");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("System is busy. Please try again later.");
            addRateLimitHeaders(response, globalProbe);
            return false;
        }

        // 2. User/IP Rate Limit
        String apiKey = request.getHeader("X-API-KEY");
        String remoteIp = request.getRemoteAddr();
        
        // Identify user: Use API Key if present, otherwise fall back to IP
        String userKey = (apiKey != null && !apiKey.isEmpty()) ? "USER:" + apiKey : "IP:" + remoteIp;
        PricingPlan plan = PricingPlan.resolvePlanFromApiKey(apiKey);
        
        Bucket userBucket = rateLimitingService.resolveBucket(userKey, plan);
        ConsumptionProbe userProbe = userBucket.tryConsumeAndReturnRemaining(1);

        addRateLimitHeaders(response, userProbe);

        if (userProbe.isConsumed()) {
            return true;
        } else {
            long waitForRefill = userProbe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for {}. Wait {}s", userKey, waitForRefill);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(waitForRefill));
            response.getWriter().write("Too many requests. Please wait " + waitForRefill + " seconds.");
            return false;
        }
    }

    private void addRateLimitHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
    }
}
