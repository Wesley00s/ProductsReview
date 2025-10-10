package com.example.productsreview.listener.dto;

import java.util.UUID;

public record CommentAddedEvent(
        String reviewId,
        UUID commentId,
        UUID parentCommentId,
        String customerId,
        String customerName,
        String content,
        String mentionedUserId,
        String mentionedUserName
) {

}