package com.example.productsreview.controller.dto;

public record CreateReviewRequest(
    String reviewId,
    String productId,
    String customerId,
    String customerName,
    String content,
    Double rating
) {}
