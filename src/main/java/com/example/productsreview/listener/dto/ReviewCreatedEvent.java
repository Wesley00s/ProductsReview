package com.example.productsreview.listener.dto;

import java.util.UUID;

public record ReviewCreatedEvent(
        UUID reviewId,
        Long productId,
        String productCode,
        String customerId,
        String customerName,
        String content,
        Double rating
) {
}
