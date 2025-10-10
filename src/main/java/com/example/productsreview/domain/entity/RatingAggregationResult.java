package com.example.productsreview.domain.entity;

import lombok.Data;

@Data
public class RatingAggregationResult {
    private Double averageRating;
    private Long totalReviews;
}