package com.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Value("${security.jwt.issuer-uri:https://auth.ecommerce.com}")
    private String issuerUri;
    
    @Value("${security.cors.allowed-origins:http://localhost:3000,https://ecommerce.com}")
    private List<String> allowedOrigins;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/orders/health", "/flash-sales/health").permitAll()
                
                // Order management endpoints
                .requestMatchers("POST", "/orders").hasAnyRole("USER", "ADMIN")
                .requestMatchers("GET", "/orders/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("PUT", "/orders/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("DELETE", "/orders/**").hasAnyRole("USER", "ADMIN")
                
                // Order status management (restricted to fulfillment and admin)
                .requestMatchers("POST", "/orders/*/confirm").hasAnyRole("ADMIN", "FULFILLMENT")
                .requestMatchers("POST", "/orders/*/ship").hasAnyRole("ADMIN", "FULFILLMENT")
                .requestMatchers("POST", "/orders/*/deliver").hasAnyRole("ADMIN", "FULFILLMENT")
                
                // Flash sale endpoints
                .requestMatchers("POST", "/flash-sales/*/orders").hasAnyRole("USER", "ADMIN")
                .requestMatchers("GET", "/flash-sales/*/stats").hasAnyRole("ADMIN", "ANALYTICS")
                .requestMatchers("GET", "/flash-sales/*/queue-status").hasRole("ADMIN")
                .requestMatchers("POST", "/flash-sales/*/start").hasRole("ADMIN")
                .requestMatchers("POST", "/flash-sales/*/stop").hasRole("ADMIN")
                .requestMatchers("GET", "/flash-sales/*/queue-position/*").hasAnyRole("USER", "ADMIN")
                
                // Admin endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure OAuth2 Resource Server with JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");
        
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        authenticationConverter.setPrincipalClaimName("sub");
        
        return authenticationConverter;
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", 
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
            "X-User-ID", "X-Trace-ID", "X-Request-ID"
        ));
        
        // Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
            "X-Total-Count", "X-Rate-Limit-Remaining", "X-Rate-Limit-Reset"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Max age for preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Custom authentication filter for API key authentication (if needed)
     */
    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        return new ApiKeyAuthenticationFilter();
    }
    
    /**
     * Rate limiting filter
     */
    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter();
    }
    
    /**
     * Custom API Key Authentication Filter
     */
    public static class ApiKeyAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
        
        private static final String API_KEY_HEADER = "X-API-Key";
        private static final String VALID_API_KEY = "your-api-key-here"; // Should be externalized
        
        @Override
        protected boolean requiresAuthentication(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response) {
            
            String apiKey = request.getHeader(API_KEY_HEADER);
            return apiKey != null && VALID_API_KEY.equals(apiKey);
        }
    }
    
    /**
     * Rate Limiting Filter
     */
    public static class RateLimitingFilter implements jakarta.servlet.Filter {
        
        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, 
                           jakarta.servlet.ServletResponse response, 
                           jakarta.servlet.FilterChain chain) 
                throws java.io.IOException, jakarta.servlet.ServletException {
            
            jakarta.servlet.http.HttpServletRequest httpRequest = 
                (jakarta.servlet.http.HttpServletRequest) request;
            jakarta.servlet.http.HttpServletResponse httpResponse = 
                (jakarta.servlet.http.HttpServletResponse) response;
            
            // Get client IP for rate limiting
            String clientIp = getClientIpAddress(httpRequest);
            
            // Add rate limiting headers
            httpResponse.setHeader("X-RateLimit-Limit", "1000");
            httpResponse.setHeader("X-RateLimit-Remaining", "999");
            httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 3600000));
            
            chain.doFilter(request, response);
        }
        
        private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        }
    }
}