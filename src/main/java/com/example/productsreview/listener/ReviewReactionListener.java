package com.example.productsreview.listener;

import com.example.productsreview.config.RabbitMqConfig;
import com.example.productsreview.listener.dto.ReviewLikedEvent;
import com.example.productsreview.listener.dto.ReviewDislikedEvent;
import com.example.productsreview.service.ReviewService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReviewReactionListener {
    private final ReviewService reviewService;

    public ReviewReactionListener(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RabbitListener(queues = RabbitMqConfig.REVIEW_LIKED_QUEUE)
    public void onReviewLiked(ReviewLikedEvent ev) {
        reviewService.toggleLike(ev.reviewId(), ev.customerId());
    }

    @RabbitListener(queues = RabbitMqConfig.REVIEW_DISLIKED_QUEUE)
    public void onReviewDisliked(ReviewDislikedEvent ev) {
        reviewService.toggleDislike(ev.reviewId(), ev.customerId());
    }
}
