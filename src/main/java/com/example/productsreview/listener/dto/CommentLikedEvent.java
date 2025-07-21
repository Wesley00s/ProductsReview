package com.example.productsreview.listener.dto;

public record CommentLikedEvent(
        String reviewId,
        String commentId,
        String customerId
) {}