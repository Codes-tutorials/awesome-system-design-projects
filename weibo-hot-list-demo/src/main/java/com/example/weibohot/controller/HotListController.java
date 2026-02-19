package com.example.weibohot.controller;

import com.example.weibohot.model.WeiboPost;
import com.example.weibohot.service.WeiboService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/weibo")
@RequiredArgsConstructor
public class HotListController {

    private final WeiboService weiboService;

    @PostMapping
    public WeiboPost createPost(@RequestParam String content, @RequestParam String author) {
        String mid = UUID.randomUUID().toString();
        return weiboService.createPost(mid, content, author);
    }

    @PostMapping("/{mid}/interact")
    public String interact(@PathVariable String mid, @RequestParam String type) {
        // type: read, like, comment
        weiboService.interact(mid, type);
        return "Interaction recorded";
    }

    @GetMapping("/hot")
    public List<WeiboPost> getHotList(@RequestParam(defaultValue = "10") int topN) {
        return weiboService.getHotList(topN);
    }
}
