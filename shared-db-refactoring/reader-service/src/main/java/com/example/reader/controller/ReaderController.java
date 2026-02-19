package com.example.reader.controller;

import com.example.reader.model.UserReplica;
import com.example.reader.repository.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reader/users")
@RequiredArgsConstructor
public class ReaderController {

    private final UserReplicaRepository repository;

    @GetMapping("/{id}")
    public UserReplica getLocalUserCopy(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found in local replica"));
    }
}
