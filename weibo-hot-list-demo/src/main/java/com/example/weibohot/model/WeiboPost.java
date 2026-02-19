package com.example.weibohot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Entity
@Table(name = "weibo_post")
@Document(indexName = "weibo_index")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeiboPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @org.springframework.data.annotation.Id // For ES
    private Long id;

    @Column(unique = true, nullable = false)
    @Field(type = FieldType.Keyword)
    private String mid;

    @Column(columnDefinition = "TEXT")
    @Field(type = FieldType.Text, analyzer = "ik_max_word") // Assuming IK analyzer installed, else standard
    private String content;

    @Field(type = FieldType.Keyword)
    private String author;

    @Builder.Default
    private Integer likeCount = 0;

    @Builder.Default
    private Integer commentCount = 0;

    @Builder.Default
    private Long readCount = 0L;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Calculate hot score
    public double getScore() {
        return readCount * 1.0 + likeCount * 20.0 + commentCount * 50.0;
    }
}
