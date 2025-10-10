package com.example.productsreview.api.v1.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentSummaryResponse(
        UUID commentId,
        String customerId,
        String customerName,
        String content,
        UUID parentCommentId,
        String mentionedUserId,
        String mentionedUserName,
        Integer likes,
        Integer dislikes,
        Integer totalReplies,
        Instant createdAt,
        Boolean likedByMe,
        Boolean dislikedByMe
) {
}
