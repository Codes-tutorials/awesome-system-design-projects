package com.example.weibohot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotListService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String HOT_KEY = "weibo:hot:today";

    /**
     * Update the score of a post in the hot list
     */
    public void updateScore(String mid, double score) {
        redisTemplate.opsForZSet().add(HOT_KEY, mid, score);
    }

    /**
     * Get Top N hot posts (mids)
     */
    public Set<String> getTopHotPosts(int topN) {
        // Reverse range to get highest scores first
        return redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, topN - 1);
    }

    /**
     * Increment score components (simplified simulation)
     * Real world would use separate keys or atomic increments and async aggregation
     */
    public void incrementScore(String mid, double delta) {
        redisTemplate.opsForZSet().incrementScore(HOT_KEY, mid, delta);
    }
}
