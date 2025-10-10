package com.example.productsreview.api.v1.controller.dto;

import java.util.UUID;

public record ReviewResponse(
    UUID reviewId,
    Long productId,
    String customerId,
    String customerName,
    String content,
    Double rating
) {
}
