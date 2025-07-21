package com.example.productsreview.listener.dto;

public record ReviewCreatedEvent(
        String reviewId,
        String productId,
        String customerId,
        String customerName,
        String content,
        Double rating
) {
}
