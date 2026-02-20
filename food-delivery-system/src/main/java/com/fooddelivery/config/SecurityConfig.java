package com.fooddelivery.config;

import com.fooddelivery.security.JwtAuthenticationEntryPoint;
import com.fooddelivery.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for JWT authentication and authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/restaurant/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/top-rated").permitAll()
                        
                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Health check endpoints
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        
                        // Admin only endpoints
                        .requestMatchers("/api/analytics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/role/**").hasRole("ADMIN")
                        .requestMatchers("/api/reviews/reported").hasRole("ADMIN")
                        .requestMatchers("/api/reviews/*/moderate").hasRole("ADMIN")
                        
                        // Restaurant owner endpoints
                        .requestMatchers(HttpMethod.POST, "/api/restaurants").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/**").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/menu-items").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/menu-items/**").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers("/api/orders/restaurant/**").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers("/api/orders/*/confirm").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers("/api/orders/*/start-preparation").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        .requestMatchers("/api/orders/*/ready").hasAnyRole("ADMIN", "RESTAURANT_OWNER")
                        
                        // Delivery partner endpoints
                        .requestMatchers("/api/orders/*/pickup").hasAnyRole("ADMIN", "DELIVERY_PARTNER")
                        .requestMatchers("/api/orders/*/start-delivery").hasAnyRole("ADMIN", "DELIVERY_PARTNER")
                        .requestMatchers("/api/orders/*/deliver").hasAnyRole("ADMIN", "DELIVERY_PARTNER")
                        .requestMatchers("/api/delivery-partners/*/location").hasAnyRole("ADMIN", "DELIVERY_PARTNER")
                        .requestMatchers("/api/delivery-partners/*/availability").hasAnyRole("ADMIN", "DELIVERY_PARTNER")
                        .requestMatchers("/api/delivery-partners/*/emergency").hasRole("DELIVERY_PARTNER")
                        
                        // Customer endpoints
                        .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/*/rating").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("CUSTOMER")
                        .requestMatchers("/api/reviews/*/helpful").hasRole("CUSTOMER")
                        .requestMatchers("/api/reviews/*/report").hasRole("CUSTOMER")
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                );
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}