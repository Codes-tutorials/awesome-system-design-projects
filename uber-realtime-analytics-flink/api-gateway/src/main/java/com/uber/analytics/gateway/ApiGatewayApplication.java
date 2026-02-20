package com.uber.analytics.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway for Uber Real-Time Analytics Platform
 */
@SpringBootApplication
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ride service routes
                .route("rides", r -> r.path("/api/rides/**")
                        .uri("http://localhost:8081"))
                
                // Driver service routes
                .route("drivers", r -> r.path("/api/drivers/**")
                        .uri("http://localhost:8082"))
                
                // Order service routes (UberEats)
                .route("orders", r -> r.path("/api/orders/**")
                        .uri("http://localhost:8083"))
                
                // Analytics service routes
                .route("analytics", r -> r.path("/api/analytics/**")
                        .uri("http://localhost:8084"))
                
                // Pricing service routes
                .route("pricing", r -> r.path("/api/pricing/**")
                        .uri("http://localhost:8085"))
                
                // Real-time dashboard routes
                .route("dashboard", r -> r.path("/dashboard/**")
                        .uri("http://localhost:3000"))
                
                // Flink UI routes
                .route("flink", r -> r.path("/flink/**")
                        .uri("http://localhost:8081"))
                
                // Kafka UI routes
                .route("kafka", r -> r.path("/kafka/**")
                        .uri("http://localhost:8080"))
                
                // Pinot console routes
                .route("pinot", r -> r.path("/pinot/**")
                        .uri("http://localhost:9000"))
                
                .build();
    }
}