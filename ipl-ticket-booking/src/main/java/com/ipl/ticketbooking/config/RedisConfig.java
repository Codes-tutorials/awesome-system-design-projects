package com.ipl.ticketbooking.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * REDIS CONFIGURATION: Critical for distributed locking and rate limiting
 * Handles high-performance caching and coordination across multiple instances
 */
@Configuration
public class RedisConfig {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;
    
    /**
     * Redis connection factory with optimized settings for high throughput
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        
        if (!redisPassword.isEmpty()) {
            factory.setPassword(redisPassword);
        }
        
        factory.setDatabase(redisDatabase);
        factory.setValidateConnection(true);
        
        return factory;
    }
    
    /**
     * Redis template with JSON serialization for complex objects
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        
        return template;
    }
    
    /**
     * Redisson client for distributed locking
     * CRITICAL: This enables seat locking across multiple application instances
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        String redisUrl = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer()
              .setAddress(redisUrl)
              .setDatabase(redisDatabase)
              .setConnectionMinimumIdleSize(10)
              .setConnectionPoolSize(20)
              .setIdleConnectionTimeout(10000)
              .setConnectTimeout(10000)
              .setTimeout(3000)
              .setRetryAttempts(3)
              .setRetryInterval(1500);
        
        if (!redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }
        
        return Redisson.create(config);
    }
    
    /**
     * Proxy manager for distributed rate limiting with Bucket4j
     * Enables rate limiting across multiple application instances
     */
    @Bean
    public ProxyManager<String> proxyManager(RedissonClient redissonClient) {
        return RedissonBasedProxyManager.builderFor(redissonClient)
                .withExpirationAfterWriteStrategy()
                .build();
    }
}