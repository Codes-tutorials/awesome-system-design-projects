package com.example.ratelimiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    @PostMapping
    public ResponseEntity<String> postTweet(@RequestBody String content) {
        // Business logic to save tweet...
        return ResponseEntity.ok("Tweet posted successfully: " + content);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getTweet(@PathVariable String id) {
        return ResponseEntity.ok("Tweet content for ID: " + id);
    }
}
