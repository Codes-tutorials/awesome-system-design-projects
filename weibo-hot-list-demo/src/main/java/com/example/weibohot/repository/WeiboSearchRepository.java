package com.example.weibohot.repository;

import com.example.weibohot.model.WeiboPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeiboSearchRepository extends ElasticsearchRepository<WeiboPost, Long> {
    // Custom search methods can be defined here
}
