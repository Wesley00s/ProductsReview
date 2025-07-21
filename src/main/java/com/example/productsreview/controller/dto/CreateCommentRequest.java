package com.example.productsreview.controller.dto;

public record CreateCommentRequest(
    String commentId,
    String customerId,
    String customerName,
    String content,
    String parentCommentId,
    String mentionedUserId,
    String mentionedUserName
) {}
