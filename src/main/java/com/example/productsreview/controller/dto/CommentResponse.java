package com.example.productsreview.controller.dto;

import com.example.productsreview.entity.Comment;
import com.example.productsreview.entity.ReviewEntity;

import java.util.List;

public record CommentResponse(
        String id,
        String customerId,
        String customerName,
        String content,
        List<Comment> comments
) {
}
