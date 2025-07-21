package com.example.productsreview.listener;

import com.example.productsreview.config.RabbitMqConfig;
import com.example.productsreview.listener.dto.CommentLikedEvent;
import com.example.productsreview.service.ReviewService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CommentReactionListener {
    private final ReviewService reviewService;

    public CommentReactionListener(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RabbitListener(queues = RabbitMqConfig.COMMENT_LIKED_QUEUE)
    public void onCommentLiked(CommentLikedEvent ev) {
        reviewService.toggleCommentLike(ev.reviewId(), ev.commentId(), ev.customerId());
    }

    @RabbitListener(queues = RabbitMqConfig.COMMENT_DISLIKED_QUEUE)
    public void onCommentDisliked(CommentLikedEvent ev) {
        reviewService.toggleCommentDislike(ev.reviewId(), ev.commentId(), ev.customerId());
    }
}
