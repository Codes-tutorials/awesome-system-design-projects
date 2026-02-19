package com.example.antishake.controller;

import com.example.antishake.annotation.AntiDuplicateSubmit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @PostMapping("/submit-lua")
    @AntiDuplicateSubmit(timeout = 5, type = AntiDuplicateSubmit.LockType.REDIS_LUA, message = "LUA: Too fast!")
    public String submitLua(@RequestBody String data) {
        log.info("LUA submit: {}", data);
        return "LUA success";
    }

    @PostMapping("/submit-redisson")
    @AntiDuplicateSubmit(timeout = 5, type = AntiDuplicateSubmit.LockType.REDISSON, message = "REDISSON: Too fast!")
    public String submitRedisson(@RequestBody String data) {
        log.info("REDISSON submit: {}", data);
        return "REDISSON success";
    }

    @PostMapping("/submit-local")
    @AntiDuplicateSubmit(timeout = 5, type = AntiDuplicateSubmit.LockType.LOCAL, message = "LOCAL: Too fast!")
    public String submitLocal(@RequestBody String data) {
        log.info("LOCAL submit: {}", data);
        return "LOCAL success";
    }
}
