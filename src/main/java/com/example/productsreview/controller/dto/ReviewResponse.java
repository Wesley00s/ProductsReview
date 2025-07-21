package com.example.productsreview.controller.dto;

import com.example.productsreview.entity.Comment;
import com.example.productsreview.entity.ReviewEntity;

import java.util.ArrayList;
import java.util.List;

public record ReviewResponse(
        String id,
        String productId,
        String customerId,
        String customerName,
        String content,
        List<Comment> comments,
        Double rating
) {

    public static ReviewResponse from(ReviewEntity entity) {
        return new ReviewResponse(
                entity.getReviewId(),
                entity.getProductId(),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getContent(),
                entity.getComments(),
                entity.getRating()
        );
    }
}
