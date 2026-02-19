package com.example.reader.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_replica") // Explicitly named to denote it's a copy
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReplica {
    
    @Id
    private Long id; // Matches the ID from Owner Service
    
    private String name;
    private String email;
    
    private LocalDateTime lastUpdated;
}
