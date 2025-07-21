package com.example.productsreview.controller.dto;

import java.time.LocalDateTime;

public record CommentSummaryResponse(
        String commentId,
        String customerId,
        String customerName,
        String content,
        String parentCommentId,
        String mentionedUserId,
        String mentionedUserName,
        Integer likes,
        Integer dislikes,
        Integer totalReplies,
        LocalDateTime createdAt
) {
}
