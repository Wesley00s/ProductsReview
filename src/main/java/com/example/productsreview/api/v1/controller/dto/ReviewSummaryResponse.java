package com.example.productsreview.api.v1.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewSummaryResponse(
    UUID reviewId,
    Long productId,
    String productCode,
    String customerId,
    Integer likes,
    Integer dislikes,
    String customerName,
    String content,
    Double rating,
    Integer totalComments,
    Instant createdAt,
    Boolean likedByMe,
    Boolean dislikedByMe
) {}