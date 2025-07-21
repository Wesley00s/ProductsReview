package com.example.productsreview.controller.dto;

import com.example.productsreview.domain.entity.Comment;

import java.util.List;

public record ReviewResponse(
        String id,
        String productId,
        String customerId,
        String customerName,
        String content,
        List<Comment> comments,
        Double rating
) {}
