package com.example.productsreview.listener.dto;

import java.util.UUID;

public record CommentLikedEvent(
        String reviewId,
        UUID commentId,
        String customerId
) {}