package com.example.largetable.repository;

import com.example.largetable.model.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {
    
    @Query("SELECT MIN(u.id) FROM UserRecord u")
    Long findMinId();

    @Query("SELECT MAX(u.id) FROM UserRecord u")
    Long findMaxId();
}
