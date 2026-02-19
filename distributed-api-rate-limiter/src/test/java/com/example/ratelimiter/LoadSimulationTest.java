package com.example.ratelimiter;

import com.example.ratelimiter.service.RedisRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LoadSimulationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisRateLimiter redisRateLimiter; // To clear redis if needed or verify

    @Test
    public void testDistributedLoad() throws InterruptedException {
        // Simulate 20 concurrent threads (representing 20 servers/clients)
        int threadCount = 20;
        int totalRequests = 120; // Limit is 100, so 20 should fail
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);
        
        String userId = "user-load-test";

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/resource")
                            .header("X-User-Id", userId))
                            .andDo(result -> {
                                if (result.getResponse().getStatus() == 200) {
                                    successCount.incrementAndGet();
                                } else if (result.getResponse().getStatus() == 429) {
                                    blockedCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Success: " + successCount.get());
        System.out.println("Blocked: " + blockedCount.get());

        // We expect roughly 100 successes (allow burst) and the rest blocked
        // Note: Exact count depends on race conditions in test runner, but Redis atomic ops should hold strict.
        // Capacity is 100.
        assertTrue(successCount.get() <= 101, "Should not exceed capacity significantly (allow 1 for rounding)");
        assertTrue(blockedCount.get() >= 19, "Should block excess requests");
    }
}
