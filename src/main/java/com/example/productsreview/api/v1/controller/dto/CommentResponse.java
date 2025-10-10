package com.example.productsreview.api.v1.controller.dto;

import com.example.productsreview.domain.entity.Comment;

import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String customerId,
        String customerName,
        String content,
        List<Comment> comments
) {
}
