package com.example.ratelimiter;

import com.example.ratelimiter.service.RateLimitingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Test
    public void testFreeTierBurstAndThrottle() throws Exception {
        // Free tier: Capacity 5.
        // We will make 5 requests quickly (Burst), all should pass.
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/tweets")
                    .content("Hello World " + i)
                    .contentType(MediaType.TEXT_PLAIN))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
        }

        // The 6th request should fail immediately
        mockMvc.perform(post("/api/tweets")
                .content("Spam Tweet")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    public void testPremiumTierHigherLimits() throws Exception {
        // Premium tier: Capacity 20.
        // We will make 10 requests quickly, all should pass.
        String apiKey = "PREMIUM-USER-123";
        
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/tweets")
                    .header("X-API-KEY", apiKey)
                    .content("Premium Content " + i)
                    .contentType(MediaType.TEXT_PLAIN))
                    .andExpect(status().isOk());
        }
        
        // Ensure we still have tokens left
        // (This test depends on Redis state, so ensure Redis is fresh or keys are unique)
    }
}
