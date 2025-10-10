package com.example.productsreview.api.v1.controller.dto;

public record ProductRatingUpdatedEvent(
    Double newRating,
    Long totalReviews,
    String productCode
) {}