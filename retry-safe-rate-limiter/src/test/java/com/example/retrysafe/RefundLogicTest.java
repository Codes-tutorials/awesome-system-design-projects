package com.example.retrysafe;

import com.example.retrysafe.controller.ApiController;
import com.example.retrysafe.interceptor.RateLimitInterceptor;
import com.example.retrysafe.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
@Import(RateLimitInterceptor.class)
public class RefundLogicTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateLimiterService rateLimiterService;

    @Test
    public void testSuccessfulRequest_ShouldNotRefund() throws Exception {
        // Arrange: Allow request
        when(rateLimiterService.tryAcquire("user1"))
                .thenReturn(new RateLimiterService.RateLimitResult(true, 9, 0));

        // Act: Call /api/resource (returns 200)
        mockMvc.perform(get("/api/resource")
                        .header("X-User-Id", "user1"))
                .andExpect(status().isOk());

        // Assert: Verify acquire called, refund NOT called
        verify(rateLimiterService).tryAcquire("user1");
        verify(rateLimiterService, never()).refundToken(anyString());
    }

    @Test
    public void testFailedRequest_ShouldRefund() throws Exception {
        // Arrange: Allow request
        when(rateLimiterService.tryAcquire("user2"))
                .thenReturn(new RateLimiterService.RateLimitResult(true, 8, 0));

        // Act: Call /api/resource?fail=true (returns 500)
        mockMvc.perform(get("/api/resource?fail=true")
                        .header("X-User-Id", "user2"))
                .andExpect(status().isInternalServerError());

        // Assert: Verify acquire called AND refund called
        verify(rateLimiterService).tryAcquire("user2");
        verify(rateLimiterService, times(1)).refundToken("user2");
    }

    @Test
    public void testBlockedRequest_ShouldReturn429_AndNoRefund() throws Exception {
        // Arrange: Block request
        when(rateLimiterService.tryAcquire("user3"))
                .thenReturn(new RateLimiterService.RateLimitResult(false, 0, 10));

        // Act: Call /api/resource
        mockMvc.perform(get("/api/resource")
                        .header("X-User-Id", "user3"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Too Many Requests"));

        // Assert: Verify acquire called, refund NOT called (controller never hit)
        verify(rateLimiterService).tryAcquire("user3");
        verify(rateLimiterService, never()).refundToken(anyString());
    }
}
