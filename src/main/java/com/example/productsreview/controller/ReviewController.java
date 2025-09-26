package com.example.productsreview.controller;

import com.example.productsreview.config.RabbitMqConfig;
import com.example.productsreview.controller.dto.*;
import com.example.productsreview.domain.enumeration.SortField;
import com.example.productsreview.listener.dto.*;
import com.example.productsreview.service.ReviewService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final RabbitTemplate rabbitTemplate;

    public ReviewController(ReviewService reviewService, RabbitTemplate rabbitTemplate) {
        this.reviewService = reviewService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> findSummaries(
            @PathVariable String productId,
            @RequestParam(defaultValue = "createdAt") SortField sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        var summaries = reviewService.findSummariesByProductId(
                productId,
                sortField.getFieldName(),
                direction,
                page,
                size
        );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        summaries.getContent(),
                        PaginationResponse.from(summaries)
                )
        );
    }

    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<List<CommentSummaryResponse>> findComments(
            @PathVariable String reviewId,
            @RequestParam(defaultValue = "0") Integer skip,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        var comments = reviewService.findCommentsByReviewId(reviewId, skip, limit);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{reviewId}/comments/{commentId}/replies")
    public ResponseEntity<List<CommentSummaryResponse>> findReplies(
            @PathVariable String reviewId,
            @PathVariable String commentId
    ) {
        var replies = reviewService.findReplies(reviewId, commentId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody CreateReviewRequest request
    ) {
        var event = new ReviewCreatedEvent(
                request.reviewId(),
                request.productId(),
                request.customerId(),
                request.customerName(),
                request.content(),
                request.rating()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.REVIEW_CREATED_QUEUE, event);

        return ResponseEntity.created(
                URI.create("/reviews/" + request.reviewId())
        ).body(
                new ReviewResponse(
                        request.reviewId(),
                        request.productId(),
                        request.customerId(),
                        request.customerName(),
                        request.content(),
                        new ArrayList<>(),
                        request.rating()
                )
        );
    }

    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String reviewId,
            @RequestBody CreateCommentRequest request
    ) {
        var event = new CommentAddedEvent(
                reviewId,
                request.commentId(),
                request.parentCommentId(),
                request.customerId(),
                request.customerName(),
                request.content(),
                request.mentionedUserId(),
                request.mentionedUserName()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_ADDED_QUEUE, event);

        return ResponseEntity.created(
                URI.create("/reviews/" + reviewId + "/comments/" + request.commentId())
        ).body(
                new CommentResponse(
                        request.commentId(),
                        request.customerId(),
                        request.customerName(),
                        request.content(),
                        new ArrayList<>()
                )
        );
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> like(
            @PathVariable String reviewId,
            @RequestParam String customerId
    ) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REVIEW_LIKED_QUEUE,
                new ReviewLikedEvent(reviewId, customerId)
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{reviewId}/dislike")
    public ResponseEntity<Void> dislike(
            @PathVariable String reviewId,
            @RequestParam String customerId
    ) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REVIEW_DISLIKED_QUEUE,
                new ReviewDislikedEvent(reviewId, customerId)
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{reviewId}/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(
            @PathVariable String reviewId,
            @PathVariable String commentId,
            @RequestParam String customerId
    ) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.COMMENT_LIKED_QUEUE,
                new CommentLikedEvent(reviewId, commentId, customerId)
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{reviewId}/comments/{commentId}/dislike")
    public ResponseEntity<Void> dislikeComment(
            @PathVariable String reviewId,
            @PathVariable String commentId,
            @RequestParam String customerId
    ) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.COMMENT_DISLIKED_QUEUE,
                new CommentDislikedEvent(reviewId, commentId, customerId)
        );
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String reviewId,
            @PathVariable String commentId
    ) {
        reviewService.deleteComment(reviewId, commentId);
        return ResponseEntity.noContent().build();
    }
}
