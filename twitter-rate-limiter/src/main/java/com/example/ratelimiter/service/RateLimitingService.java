package com.example.ratelimiter.service;

import com.example.ratelimiter.config.PricingPlan;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.RedisCodec;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitingService {

    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;
    private LettuceBasedProxyManager<String> proxyManager;

    @PostConstruct
    public void init() {
        redisClient = RedisClient.create("redis://" + redisHost + ":" + redisPort);
        connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        
        // Use Redis for storing bucket state
        proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(60)))
                .build();
    }
    
    @PreDestroy
    public void shutdown() {
        if (connection != null) connection.close();
        if (redisClient != null) redisClient.shutdown();
    }

    /**
     * Resolves or creates a bucket for the given key and plan.
     */
    public Bucket resolveBucket(String key, PricingPlan plan) {
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(plan.getLimit())
                .build();
        
        return proxyManager.builder().build(key, configuration);
    }
}
