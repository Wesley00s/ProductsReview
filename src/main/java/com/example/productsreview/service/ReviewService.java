package com.example.productsreview.service;

import com.example.productsreview.controller.dto.ReviewResponse;
import com.example.productsreview.entity.ReviewEntity;
import com.example.productsreview.listener.dto.ReviewCreatedEvent;
import com.example.productsreview.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public void save(ReviewCreatedEvent event) {
        var entity = new ReviewEntity();
        entity.setReviewId(event.reviewId());
        entity.setProductId(event.productId());
        entity.setCustomerName(event.customerName());
        entity.setContent(event.content());
        entity.setCustomerId(event.customerId());
        entity.setRating(event.rating());

        reviewRepository.save(entity);
    }

    public Page<ReviewResponse> findAllByProductId(String productId, int page, int pageSize) {
        var reviews = reviewRepository.findAllByProductId(productId, PageRequest.of(page, pageSize));
        return reviews.map(ReviewResponse::from);
    }

    public Page<ReviewResponse> findAllByCustomerId(String customerId, int page, int pageSize) {
        var reviews = reviewRepository.findAllByCustomerId(customerId, PageRequest.of(page, pageSize));
        return reviews.map(ReviewResponse::from);
    }
}
