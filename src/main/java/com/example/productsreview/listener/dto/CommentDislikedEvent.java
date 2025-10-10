package com.example.productsreview.listener.dto;

import java.util.UUID;

public record CommentDislikedEvent(
        String reviewId,
        UUID commentId,
        String customerId
) {}