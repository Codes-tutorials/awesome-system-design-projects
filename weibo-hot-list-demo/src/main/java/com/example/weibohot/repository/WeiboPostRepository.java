package com.example.weibohot.repository;

import com.example.weibohot.model.WeiboPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeiboPostRepository extends JpaRepository<WeiboPost, Long> {
    Optional<WeiboPost> findByMid(String mid);
    List<WeiboPost> findByMidIn(List<String> mids);
}
