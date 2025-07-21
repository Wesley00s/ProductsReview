package com.example.productsreview.controller.dto;

import java.time.LocalDateTime;

public record ReviewSummaryResponse(
    String reviewId,
    String productId,
    String customerId,
    Integer likes,
    Integer dislikes,
    String customerName,
    String content,
    Double rating,
    Integer totalComments,
    LocalDateTime createdAt
) {}