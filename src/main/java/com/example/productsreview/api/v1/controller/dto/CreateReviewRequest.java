package com.example.productsreview.api.v1.controller.dto;

public record CreateReviewRequest(
    Long productId,
    String productCode,
    String content,
    Double rating
) {}
