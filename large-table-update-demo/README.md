# Efficient Large Table Updates Demo

This Spring Boot project demonstrates how to perform updates on large database tables (millions of records) without causing **Full Table Locks**, which can lead to downtime or performance degradation in production environments.

## The Problem
Running a simple `UPDATE table SET status = 'ACTIVE' WHERE status = 'PENDING'` on a table with 50M+ rows will:
1.  **Lock the entire table** (or a huge range of rows), blocking other writes.
2.  Fill up the **Undo Log / Transaction Log**, potentially crashing the DB.
3.  Time out if the transaction takes too long.

## The Solution: ID-Range Batching (Keyset Pagination)
Instead of one giant transaction, we break the work into small, manageable batches based on the Primary Key (ID).

### Algorithm
1.  Find `MIN(id)` and `MAX(id)` of the table.
2.  Iterate from `minId` to `maxId` in steps of `batchSize` (e.g., 1000).
3.  Execute update for the specific ID range:
    ```sql
    UPDATE user_records 
    SET status = ?, last_updated = NOW() 
    WHERE id >= ? AND id < ? AND status = ?
    ```
4.  **Commit** the transaction immediately after each batch.
5.  (Optional) Sleep for 10-50ms between batches to let other transactions slip in (Throttling).

## Project Structure
-   **`UserRecord`**: The entity representing a row in the large table.
-   **`DataSeeder`**: A utility to insert dummy data (default 100k rows) for testing.
-   **`BatchUpdateService`**: Implements the efficient update logic using `JdbcTemplate` and manual transaction management (`REQUIRES_NEW`).
-   **`JobController`**: REST API to trigger seeding and updates.

## How to Run
1.  Build the project:
    ```bash
    mvn clean install
    ```
2.  Run the application:
    ```bash
    java -jar target/large-table-update-demo-1.0.0-SNAPSHOT.jar
    ```
3.  **Seed Data** (Insert 100k rows):
    ```bash
    POST http://localhost:8083/api/jobs/seed?count=100000
    ```
4.  **Trigger Update** (Update 'PENDING' to 'ACTIVE' in batches of 1000):
    ```bash
    POST http://localhost:8083/api/jobs/update-status?oldStatus=PENDING&newStatus=ACTIVE&batchSize=1000
    ```
5.  Check logs to see the progress of batches.

## Key Code Highlights
See `BatchUpdateService.java`:
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
protected int processBatch(long startId, long endId, String oldStatus, String newStatus) {
    String sql = "UPDATE user_records SET status = ?, last_updated = CURRENT_TIMESTAMP " +
                 "WHERE id >= ? AND id < ? AND status = ?";
    return jdbcTemplate.update(sql, newStatus, startId, endId, oldStatus);
}
```
This ensures that locks are held only for the duration of processing 1000 rows, releasing them immediately for other processes.
