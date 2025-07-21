package com.example.productsreview.listener.dto;

public record ReviewDislikedEvent(
    String reviewId,
    String customerId
) {}