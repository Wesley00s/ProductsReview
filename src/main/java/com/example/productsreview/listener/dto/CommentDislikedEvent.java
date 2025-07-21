package com.example.productsreview.listener.dto;

public record CommentDislikedEvent(
        String reviewId,
        String commentId,
        String customerId
) {}