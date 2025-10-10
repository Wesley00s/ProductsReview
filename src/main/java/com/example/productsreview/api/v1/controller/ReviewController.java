package com.example.productsreview.api.v1.controller;

import com.example.productsreview.api.v1.controller.dto.*;
import com.example.productsreview.config.RabbitMqConfig;
import com.example.productsreview.domain.enumeration.SortField;
import com.example.productsreview.listener.dto.*;
import com.example.productsreview.service.ReviewService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final RabbitTemplate rabbitTemplate;

    public ReviewController(ReviewService reviewService, RabbitTemplate rabbitTemplate) {
        this.reviewService = reviewService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/product/{productCode}")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> findSummaries(
            @PathVariable String productCode,
            Authentication authentication,
            @RequestParam(defaultValue = "createdAt") SortField sortField,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        var summaries = reviewService.findSummariesByProductCode(
                productCode,
                currentUserId,
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
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer skip,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        var comments = reviewService.findCommentsByReviewId(reviewId, currentUserId, skip, limit);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{reviewId}/comments/{commentId}/replies")
    public ResponseEntity<List<CommentSummaryResponse>> findReplies(
            @PathVariable String reviewId,
            Authentication authentication,
            @PathVariable UUID commentId
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        var replies = reviewService.findReplies(reviewId, currentUserId, commentId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody CreateReviewRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal()
                instanceof AuthenticatedUser(
                String id, String userName, String email
        ))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var event = new ReviewCreatedEvent(
                UUID.randomUUID(),
                request.productId(),
                request.productCode(),
                id,
                userName,
                request.content(),
                request.rating()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.REVIEW_CREATED_QUEUE, event);

        return ResponseEntity.created(
                URI.create("/reviews/" + event.reviewId())
        ).body(
                new ReviewResponse(
                        event.reviewId(),
                        request.productId(),
                        id,
                        userName,
                        request.content(),
                        request.rating()
                )
        );
    }

    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String reviewId,
            @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal()
                instanceof AuthenticatedUser(
                String id, String userName, String email
        ))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var event = new CommentAddedEvent(
                reviewId,
                UUID.randomUUID(),
                request.parentCommentId(),
                id,
                userName,
                request.content(),
                request.mentionedUserId(),
                request.mentionedUserName()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_ADDED_QUEUE, event);

        return ResponseEntity.created(
                URI.create("/reviews/" + reviewId + "/comments/" + event.commentId())
        ).body(
                new CommentResponse(
                        event.commentId(),
                        id,
                        userName,
                        request.content(),
                        new ArrayList<>()
                )
        );
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> like(
            @PathVariable String reviewId,
            Authentication authentication
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REVIEW_LIKED_QUEUE,
                new ReviewLikedEvent(reviewId, currentUserId)
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{reviewId}/dislike")
    public ResponseEntity<Void> dislike(
            @PathVariable String reviewId,
            Authentication authentication
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REVIEW_DISLIKED_QUEUE,
                new ReviewDislikedEvent(reviewId, currentUserId)
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{reviewId}/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(
            @PathVariable String reviewId,
            Authentication authentication,
            @PathVariable UUID commentId
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.COMMENT_LIKED_QUEUE,
                new CommentLikedEvent(reviewId, commentId, currentUserId)
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{reviewId}/comments/{commentId}/dislike")
    public ResponseEntity<Void> dislikeComment(
            @PathVariable String reviewId,
            @PathVariable UUID commentId,
            Authentication authentication
    ) {
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            currentUserId = user.id();
        }

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.COMMENT_DISLIKED_QUEUE,
                new CommentDislikedEvent(reviewId, commentId, currentUserId)
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
            @PathVariable UUID commentId
    ) {
        reviewService.deleteComment(reviewId, commentId);
        return ResponseEntity.noContent().build();
    }
}
