package com.example.largetable.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void seedData(int count) {
        log.info("Starting data seeding for {} records...", count);
        String sql = "INSERT INTO user_records (username, email, status, last_updated) VALUES (?, ?, ?, ?)";
        
        int batchSize = 1000;
        List<Object[]> batchArgs = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            batchArgs.add(new Object[]{
                "user" + i, 
                "user" + i + "@example.com", 
                "PENDING", 
                LocalDateTime.now()
            });

            if (batchArgs.size() == batchSize) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }
        
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
        
        log.info("Data seeding completed.");
    }
}
