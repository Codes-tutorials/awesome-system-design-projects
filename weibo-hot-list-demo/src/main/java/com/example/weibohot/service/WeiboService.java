package com.example.weibohot.service;

import com.example.weibohot.model.WeiboPost;
import com.example.weibohot.repository.WeiboPostRepository;
import com.example.weibohot.repository.WeiboSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeiboService {

    private final WeiboPostRepository postRepository;
    private final WeiboSearchRepository searchRepository;
    private final HotListService hotListService;

    @Transactional
    public WeiboPost createPost(String mid, String content, String author) {
        WeiboPost post = WeiboPost.builder()
                .mid(mid)
                .content(content)
                .author(author)
                .build();
        
        // Save to MySQL
        post = postRepository.save(post);
        
        // Save to ES
        searchRepository.save(post);
        
        // Initialize in Redis
        hotListService.updateScore(mid, 0);
        
        return post;
    }

    @Transactional
    public void interact(String mid, String type) {
        WeiboPost post = postRepository.findByMid(mid)
                .orElseThrow(() -> new RuntimeException("Post not found: " + mid));

        double scoreDelta = 0;
        switch (type.toLowerCase()) {
            case "read":
                post.setReadCount(post.getReadCount() + 1);
                scoreDelta = 1;
                break;
            case "like":
                post.setLikeCount(post.getLikeCount() + 1);
                scoreDelta = 20;
                break;
            case "comment":
                post.setCommentCount(post.getCommentCount() + 1);
                scoreDelta = 50;
                break;
        }

        // Update MySQL
        postRepository.save(post);
        
        // Update ES (Near Real-Time) - In production might be async
        searchRepository.save(post);
        
        // Update Redis Score
        hotListService.incrementScore(mid, scoreDelta);
    }

    public List<WeiboPost> getHotList(int topN) {
        Set<String> topMids = hotListService.getTopHotPosts(topN);
        if (topMids == null || topMids.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Fetch details from MySQL (or ES)
        // Maintaining order is important, findByMidIn might not preserve order
        List<WeiboPost> posts = postRepository.findByMidIn(new ArrayList<>(topMids));
        
        // Re-sort in memory to match Redis order
        Map<String, WeiboPost> postMap = new HashMap<>();
        posts.forEach(p -> postMap.put(p.getMid(), p));
        
        List<WeiboPost> sortedPosts = new ArrayList<>();
        for (String mid : topMids) {
            if (postMap.containsKey(mid)) {
                sortedPosts.add(postMap.get(mid));
            }
        }
        return sortedPosts;
    }
}
