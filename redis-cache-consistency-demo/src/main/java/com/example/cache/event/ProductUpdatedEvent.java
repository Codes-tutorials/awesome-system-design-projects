package com.example.cache.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductUpdatedEvent {
    private Long productId;
}
