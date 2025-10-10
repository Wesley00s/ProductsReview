package com.example.productsreview.api.v1.controller.dto;

import java.util.UUID;

public record CreateCommentRequest(
    String content,
    UUID parentCommentId,
    String mentionedUserId,
    String mentionedUserName
) {}
