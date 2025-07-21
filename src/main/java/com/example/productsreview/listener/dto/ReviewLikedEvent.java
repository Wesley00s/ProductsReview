package com.example.productsreview.listener.dto;

public record ReviewLikedEvent(
    String reviewId,
    String customerId
) {}