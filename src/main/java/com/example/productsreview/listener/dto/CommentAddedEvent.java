package com.example.productsreview.listener.dto;

public record CommentAddedEvent(
        String reviewId,
        String commentId,
        String parentCommentId,
        String customerId,
        String customerName,
        String content,
        String mentionedUserId,
        String mentionedUserName
) {

}