package com.example.largetable.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_records", indexes = {
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    
    private String email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime lastUpdated;

    public enum UserStatus {
        ACTIVE, INACTIVE, PENDING, ARCHIVED
    }
}
