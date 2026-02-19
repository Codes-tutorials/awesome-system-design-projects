package com.example.largetable.service;

import com.example.largetable.repository.UserRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchUpdateService {

    private final UserRecordRepository repository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Efficiently updates records in batches using ID-Range (Keyset) Pagination.
     * This avoids full table locks by:
     * 1. Breaking the work into small ID ranges.
     * 2. Committing transaction after each batch.
     * 3. Using indexed ID column for range scans.
     */
    public void updateStatusEfficiently(String oldStatus, String newStatus, int batchSize) {
        Long minId = repository.findMinId();
        Long maxId = repository.findMaxId();

        if (minId == null || maxId == null) {
            log.info("Table is empty, skipping update.");
            return;
        }

        log.info("Starting batch update from ID {} to {} with batch size {}", minId, maxId, batchSize);

        long currentStartId = minId;
        AtomicLong totalUpdated = new AtomicLong(0);

        while (currentStartId <= maxId) {
            long currentEndId = currentStartId + batchSize;

            // Execute update in a separate transaction for this batch
            int updatedCount = processBatch(currentStartId, currentEndId, oldStatus, newStatus);
            totalUpdated.addAndGet(updatedCount);

            log.info("Processed range [{} - {}), updated {} records.", currentStartId, currentEndId, updatedCount);
            
            // Move cursor
            currentStartId = currentEndId;
            
            // Optional: Sleep to reduce DB load (Throttling)
            // try { Thread.sleep(10); } catch (InterruptedException e) { ... }
        }

        log.info("Batch update completed. Total updated: {}", totalUpdated.get());
    }

    /**
     * REQUIRES_NEW ensures a new transaction is created for each batch, 
     * releasing locks immediately after the batch is done.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected int processBatch(long startId, long endId, String oldStatus, String newStatus) {
        String sql = "UPDATE user_records SET status = ?, last_updated = CURRENT_TIMESTAMP " +
                     "WHERE id >= ? AND id < ? AND status = ?";
        return jdbcTemplate.update(sql, newStatus, startId, endId, oldStatus);
    }
}
