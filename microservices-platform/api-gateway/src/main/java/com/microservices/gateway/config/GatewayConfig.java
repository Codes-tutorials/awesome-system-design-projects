package com.microservices.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration
 */
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/users"))
                                .retry(config -> config.setRetries(3))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())))
                        .uri("lb://user-service"))
                
                // Order Service Routes
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .retry(config -> config.setRetries(3))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())))
                        .uri("lb://order-service"))
                
                // Inventory Service Routes
                .route("inventory-service", r -> r.path("/api/inventory/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("inventory-service-cb")
                                        .setFallbackUri("forward:/fallback/inventory"))
                                .retry(config -> config.setRetries(3)))
                        .uri("lb://inventory-service"))
                
                // Notification Service Routes
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notifications"))
                                .retry(config -> config.setRetries(2)))
                        .uri("lb://notification-service"))
                
                .build();
    }
    
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }
    
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> exchange.getRequest().getHeaders().getFirst("X-User-Id") != null
                ? reactor.core.publisher.Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                : reactor.core.publisher.Mono.just("anonymous");
    }
}