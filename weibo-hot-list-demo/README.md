# High Performance Weibo Hot List System

This project is a high-performance "Hot List" system implementation based on [this Juejin article](https://juejin.cn/post/7581728606308270132), utilizing **Redis**, **Elasticsearch**, and **MySQL**.

## Architecture

1.  **MySQL**: Persistent storage for Weibo posts (`weibo_post` table).
2.  **Redis**: Real-time hot list ranking using `ZSet`.
    *   Key: `weibo:hot:today`
    *   Score: `read * 1 + like * 20 + comment * 50`
3.  **Elasticsearch**: Full-text search and keyword filtering capabilities (mapped via Spring Data Elasticsearch).

## Prerequisites

-   Java 17+
-   Docker & Docker Compose (for infrastructure)

## Getting Started

1.  **Start Infrastructure**:
    Run the following command to start MySQL, Redis, and Elasticsearch containers:
    ```bash
    docker-compose up -d
    ```

2.  **Build & Run Application**:
    ```bash
    mvn clean install
    java -jar target/weibo-hot-list-demo-1.0.0-SNAPSHOT.jar
    ```

## API Usage

### 1. Create a Post
```bash
POST /api/weibo?content=Hello World&author=User1
```

### 2. Interact (Simulate Traffic)
Generate heat for a post. Type can be `read`, `like`, or `comment`.
```bash
POST /api/weibo/{mid}/interact?type=like
```

### 3. Get Hot List
Retrieve the Top N posts sorted by calculated heat score.
```bash
GET /api/weibo/hot?topN=10
```

## Key Code Logic

-   **Real-time Ranking**: `HotListService.java` uses `RedisTemplate.opsForZSet().incrementScore()` to update scores instantly upon interaction.
-   **Data Sync**: `WeiboService.java` updates MySQL and Elasticsearch synchronously for this demo (production would use async events or CDC).
